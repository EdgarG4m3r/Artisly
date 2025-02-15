package dev.apollo.artisly.tasks;

import dev.apollo.artisly.services.EmailService;

public class EmailSenderTask implements Runnable {

    @Override
    public void run() {
        EmailService.processEmailQueue();
    }
}
