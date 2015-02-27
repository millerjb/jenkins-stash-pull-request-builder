package com.millerjb.stash.response;

import com.millerjb.stash.domain.ErrorsEntity;

public class StashErrorException extends StashResponseException {

    ErrorsEntity errors;
    String messages;

    public StashErrorException(int statusCode, ErrorsEntity errors) {
        super(statusCode);
        this.errors = errors;
    }

    public ErrorsEntity getErrors() {
        return errors;
    }

    @Override
    public String getMessage() {
        if (messages == null) {
            messages = "";
            for (com.millerjb.stash.domain.Error error : errors.getErrors()) {
                messages += "\n" + error.getMessage();
            }
        }
        return messages;
    }

}
