package com.millerjb.stash.domain;

/**
 * Represents a list of errors.
 */
public class ErrorsEntity {

    Error[] errors;

    public Error[] getErrors() {
        return errors;
    }

    public void setErrors(Error[] errors) {
        this.errors = errors;
    }

    public boolean hasErrors() {
        return errors != null && errors.length > 0;
    }
}
