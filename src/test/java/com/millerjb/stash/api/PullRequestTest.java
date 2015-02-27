package com.millerjb.stash.api;

import com.google.gson.stream.JsonReader;
import com.millerjb.stash.domain.PullRequest;
import com.millerjb.stash.domain.PullRequestList;
import com.millerjb.stash.gson.GsonHttpResponseParser;
import org.junit.Test;

import java.io.InputStreamReader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PullRequestTest {

    @Test
    public void shouldParseToPullRequest() throws Exception {
        GsonHttpResponseParser<PullRequest> parser = new GsonHttpResponseParser<>();
        JsonReader reader = new JsonReader(new InputStreamReader(getClass().getResourceAsStream("/pull-request-1.json")));
        PullRequest pullRequest = parser.getAs(PullRequest.class, reader);
        assertNotNull(pullRequest);
        assertEquals(1l, pullRequest.getId());
        assertEquals(1394130846000l, pullRequest.getCreatedDate());
        assertEquals(1394212625000l, pullRequest.getUpdatedDate());
        assertNotNull(pullRequest.getToRef());
        assertEquals("develop", pullRequest.getToRef().getDisplayId());
    }

    @Test
    public void shouldParseToPullRequestList() throws Exception {
        GsonHttpResponseParser<PullRequestList> parser = new GsonHttpResponseParser<>();
        JsonReader reader = new JsonReader(new InputStreamReader(getClass().getResourceAsStream("/pull-request-list.json")));
        PullRequestList list = parser.getAs(PullRequestList.class, reader);
        assertNotNull(list);
        assertEquals(0, list.getStart());
        assertEquals(true, list.isLastPage());
        assertEquals(2, list.getSize());
        assertEquals(2, list.getValues().length);
    }
}
