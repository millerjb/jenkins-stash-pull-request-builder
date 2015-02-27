package com.millerjb.stash.api;

public class StashHttpConnectionException extends Exception {

    public StashHttpConnectionException(String message) {
        super(message);
    }

    public StashHttpConnectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public StashHttpConnectionException(Throwable cause) {
        super(cause);
    }

    public StashHttpConnectionException() {
    }
}
