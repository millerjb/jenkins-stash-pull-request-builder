package com.millerjb.stash.response;

import com.millerjb.stash.domain.PullRequest;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.message.BasicStatusLine;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.easymock.EasyMock.*;

public class StashResponseBuilderTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void shouldNotParseErrorList() throws Exception {
        // given a builder and mock response
        StashResponseBuilder<PullRequest> builder = new StashResponseBuilder<>();
        HttpEntity mockEntity = createNiceMock(HttpEntity.class);
        expect(mockEntity.getContent()).andReturn(getClass().getResourceAsStream("/errors.json"));
        StatusLine statusLine = new BasicStatusLine(HttpVersion.HTTP_1_1, 403, "");
        HttpResponse mockResponse = createNiceMock(HttpResponse.class);
        expect(mockResponse.getEntity()).andReturn(mockEntity);
        expect(mockResponse.getStatusLine()).andReturn(statusLine);
        replay(mockResponse, mockEntity);

        // then expect exception
        expectedException.expect(StashResponseException.class);

        // when parsing
        builder.build(PullRequest.class, mockResponse);
    }
}
