package com.millerjb.stash.response;

public class StashResponseException extends Exception {

    int statusCode;

    public StashResponseException(int statusCode) {
        this.statusCode = statusCode;
    }

    public StashResponseException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public StashResponseException(String message, Throwable cause, int statusCode) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public StashResponseException(Throwable cause, int statusCode) {
        super(cause);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
