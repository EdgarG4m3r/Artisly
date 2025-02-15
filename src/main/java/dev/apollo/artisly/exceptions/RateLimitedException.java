package dev.apollo.artisly.exceptions;

public class RateLimitedException extends Exception {
    public RateLimitedException(String message) {
        super(message);
    }
}
