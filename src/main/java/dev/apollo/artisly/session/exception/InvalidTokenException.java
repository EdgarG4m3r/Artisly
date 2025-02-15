package dev.apollo.artisly.session.exception;

public class InvalidTokenException extends Exception {

    public InvalidTokenException() {
        super("TOKEN_INVALID");
    }

}
