package com.millerjb.stash.api;

import com.google.gson.stream.JsonReader;
import com.millerjb.stash.domain.PullRequestActivity;
import com.millerjb.stash.domain.PullRequestActivityList;
import com.millerjb.stash.gson.GsonHttpResponseParser;
import org.junit.Test;

import java.io.InputStreamReader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PullRequestActivityTest {

    @Test
    public void shouldParseToPullRequestList() throws Exception {
        GsonHttpResponseParser<PullRequestActivityList> parser = new GsonHttpResponseParser<>();
        JsonReader reader = new JsonReader(new InputStreamReader(getClass().getResourceAsStream("/pull-request-activity-list.json")));
        PullRequestActivityList list = parser.getAs(PullRequestActivityList.class, reader);
        assertNotNull(list);
        assertEquals(0, list.getStart());
        assertEquals(true, list.isLastPage());
        assertEquals(2, list.getSize());
        assertEquals(2, list.getValues().length);
        PullRequestActivity activity1 = list.getValues()[0];
        assertEquals(1394215702000l, activity1.getCreatedDate());
        assertEquals("MERGED", activity1.getAction());
    }
}
