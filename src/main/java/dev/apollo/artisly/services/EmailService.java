package dev.apollo.artisly.services;

import dev.apollo.artisly.Artisly;
import redis.clients.jedis.Jedis;

import java.util.Base64;

public class EmailService {

    // Sends up to 10 emails per batch
    // This is to prevent us from DoSing our own SMTP server
    // TODO: Move this to configuration
    private static final int MAX_EMAILS_PER_BATCH = 10;

    public static void queueEmail(String to, String subject, String body) {
        try (Jedis jedis = Artisly.instance.getRedis().getJedis().getResource()) {
            String encodedBody = Base64.getEncoder().encodeToString(body.getBytes());
            String encodedSubject = Base64.getEncoder().encodeToString(subject.getBytes());
            String encodedTo = Base64.getEncoder().encodeToString(to.getBytes());

            jedis.rpush("email-queue", encodedTo + ":" + encodedSubject + ":" + encodedBody);
        }
    }

    public static void queueEmail(String to, String subject, String body, String pdfName, byte[] pdf) {
        try (Jedis jedis = Artisly.instance.getRedis().getJedis().getResource()) {
            String encodedBody = Base64.getEncoder().encodeToString(body.getBytes());
            String encodedSubject = Base64.getEncoder().encodeToString(subject.getBytes());
            String encodedTo = Base64.getEncoder().encodeToString(to.getBytes());
            String encodedPdfName = Base64.getEncoder().encodeToString(pdfName.getBytes());
            String encodedPdf = Base64.getEncoder().encodeToString(pdf);

            jedis.rpush("email-queue", encodedTo + ":" + encodedSubject + ":" + encodedBody + ":" + encodedPdfName + ":" + encodedPdf);
        }
    }

    /**
     * Process the email queue, sending emails to the SMTP server
     * We process up to MAX_EMAILS_PER_BATCH emails per call, provided that 5 seconds have not passed and the thread is not interrupted
     */
    public static void processEmailQueue() {
        try (Jedis jedis = Artisly.instance.getRedis().getJedis().getResource()) {
            long startTime = System.currentTimeMillis();
            int emailsSent = 0;
            while (startTime + 5000 > System.currentTimeMillis() && !Thread.currentThread().isInterrupted() && emailsSent <= MAX_EMAILS_PER_BATCH) {
                emailsSent++;
                String email = jedis.brpoplpush("email-queue", "email-processing", 5);

                if (email == null) {
                    break;
                }

                boolean success = false;

                try {
                    String[] parts = email.split(":");

                    if (parts.length == 3) {
                        String to = new String(Base64.getDecoder().decode(parts[0]));
                        String subject = new String(Base64.getDecoder().decode(parts[1]));
                        String body = new String(Base64.getDecoder().decode(parts[2]));

                        success = Artisly.instance.getSmtpClient().sendEmail(to, subject, body);
                    } else if (parts.length == 5) {
                        String to = new String(Base64.getDecoder().decode(parts[0]));
                        String subject = new String(Base64.getDecoder().decode(parts[1]));
                        String body = new String(Base64.getDecoder().decode(parts[2]));
                        String pdfName = new String(Base64.getDecoder().decode(parts[3]));
                        byte[] pdf = Base64.getDecoder().decode(parts[4]);

                        success = Artisly.instance.getSmtpClient().sendEmail(to, subject, body, pdfName, pdf);
                    } else {
                        Artisly.instance.getLogger().warn("Email in unknown format: " + email);
                    }
                } catch (Exception e) {
                    Artisly.instance.getLogger().error("Error processing email: " + email, e);
                }

                if (success) {
                    // On success, remove the email from the processing queue
                    jedis.lrem("email-processing", 1, email);
                } else {
                    // On failure, remove from processing queue and re-queue it for a retry
                    jedis.lrem("email-processing", 1, email);
                    jedis.rpush("email-queue", email);
                    Artisly.instance.getLogger().warn("Failed to process email, re-queued: " + email);
                }
            }
        }
    }

}
