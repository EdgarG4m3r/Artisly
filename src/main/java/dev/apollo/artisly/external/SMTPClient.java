package dev.apollo.artisly.external;

import org.simplejavamail.api.email.Email;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.api.mailer.config.TransportStrategy;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.MailerBuilder;

public class SMTPClient {

    private final String username;
    private final Mailer mailer;

    public SMTPClient(String host, int port, String username, String password)
    {
        this.username = username;

        this.mailer = MailerBuilder
                .withSMTPServer(host, port, username, password)
                .withTransportStrategy(TransportStrategy.SMTP_TLS)
                .buildMailer();
    }

    /**
     * Syncronously sends an email.
     * @param to The recipient's email address.
     * @param subject The subject of the email.
     * @param body The body of the email.
     * @return True if the email was sent successfully, false otherwise.
     */
    public boolean sendEmail(String to, String subject, String body) {
        Email email = EmailBuilder
                .startingBlank()
                .from("Artisly", this.username)
                .to(to)
                .withSubject(subject)
                .withReplyTo("support@artisly.net")
                .withPlainText(body)
                .buildEmail();

        try
        {
            this.mailer.sendMail(email).get();
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Syncronously sends an email with an attachment.
     * Useful for sending invoices.
     * @param to
     * @param subject
     * @param body
     * @param pdfName
     * @param pdf
     * @return
     */
    public boolean sendEmail(String to, String subject, String body, String pdfName, byte[] pdf) {
        Email email = EmailBuilder
                .startingBlank()
                .from("Artisly", this.username)
                .to(to)
                .withSubject(subject)
                .withReplyTo("support@artisly.net")
                .withPlainText(body)
                .withAttachment(pdfName, pdf, "application/pdf")
                .buildEmail();

        try
        {
            this.mailer.sendMail(email).get();
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

}
