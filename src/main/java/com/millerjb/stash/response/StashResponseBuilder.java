package com.millerjb.stash.response;

import com.millerjb.stash.domain.ErrorsEntity;
import com.millerjb.stash.gson.GsonHttpResponseParser;
import com.millerjb.stash.gson.ParseResponseException;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/**
 * Because Gson doesn't provide support for marking "required" fields, every top-level
 * object in the API should extend {@link ErrorsEntity} in order to provide error handling.
 */
public class StashResponseBuilder<T extends ErrorsEntity> {

    public StashResponse<T> build(Class<T> clazz, HttpResponse response) throws StashResponseException {
        GsonHttpResponseParser<T> parser = new GsonHttpResponseParser<>();
        T entity;
        try {
            entity = parser.getAs(clazz, response);
        } catch (ParseResponseException e) {
            throw new StashResponseException(e, response.getStatusLine().getStatusCode());
        }
        if (entity.hasErrors()) {
            throw new StashErrorException(response.getStatusLine().getStatusCode(), entity);
        }
        return new StashResponse<>(response.getStatusLine().getStatusCode(), entity);
    }

    public StashResponse<String> buildString(HttpResponse response) throws StashResponseException {
        String entity;
        try {
            entity = EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            throw new StashResponseException(e.getMessage(), e, response.getStatusLine().getStatusCode());
        }
        return new StashResponse<>(response.getStatusLine().getStatusCode(), entity);
    }
}
