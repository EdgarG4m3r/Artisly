package dev.apollo.artisly.exceptions;

public class InvalidVerificationCodeException extends Exception{
    public InvalidVerificationCodeException(String message) {
        super(message);
    }
}
