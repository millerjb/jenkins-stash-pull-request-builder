package com.millerjb.stash.gson;

public class ParseResponseException extends Exception {

    public ParseResponseException(String message) {
        super(message);
    }

    public ParseResponseException(String message, Throwable cause) {
        super(message, cause);
    }

    public ParseResponseException(Throwable cause) {
        super(cause);
    }

    public ParseResponseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
