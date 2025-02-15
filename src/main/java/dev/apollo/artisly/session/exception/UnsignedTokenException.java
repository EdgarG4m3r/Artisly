package dev.apollo.artisly.session.exception;

public class UnsignedTokenException extends Exception {

    public UnsignedTokenException() {
        super("TOKEN_UNSIGNED");
    }

}
