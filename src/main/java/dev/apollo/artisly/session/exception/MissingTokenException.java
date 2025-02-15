package dev.apollo.artisly.session.exception;

public class MissingTokenException extends Exception {

    public MissingTokenException() {
        super("TOKEN_MISSING");
    }

}
