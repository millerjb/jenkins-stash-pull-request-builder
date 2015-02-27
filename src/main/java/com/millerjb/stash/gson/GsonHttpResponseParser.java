package com.millerjb.stash.gson;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;
import org.apache.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;

public class GsonHttpResponseParser<T> {
    private static final Logger logger = LoggerFactory.getLogger(GsonHttpResponseParser.class);

    public T getAs(Class<T> clazz, JsonReader reader) throws ParseResponseException {
        try {
            return new Gson().fromJson(reader, clazz);
        } catch (JsonParseException e) {
            logger.error("Could not parse response as {}", clazz.getName(), e);
            throw new ParseResponseException(e.getMessage(), e);
        }
    }

    public T getAs(Class<T> clazz, HttpResponse response) throws ParseResponseException {
        try {
            return getAs(clazz, new JsonReader(new InputStreamReader(response.getEntity().getContent())));
        } catch (IOException e) {
            throw new ParseResponseException(e.getMessage(), e);
        }
    }


}
