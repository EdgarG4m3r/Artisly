package dev.apollo.artisly.exceptions;

public class EmailAlreadyVerified extends Exception {
    public EmailAlreadyVerified(String message) {
        super(message);
    }
}
