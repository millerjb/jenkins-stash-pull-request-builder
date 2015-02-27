package com.millerjb.jenkins.plugin.event;

import org.eclipse.jgit.transport.URIish;
import org.junit.Test;

import java.net.URISyntaxException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;

public class PullRequestNotifyEventTest {

    @Test
    public void shouldParseUserUri() throws URISyntaxException {
        String uri = "ssh://git@host:7999/~user1/repo.git";
        PullRequestNotifyEvent event = new PullRequestNotifyEvent(new URIish(uri), null);
        assertEquals("~user1", event.getProject());
        assertEquals("repo", event.getRepository());
    }

    @Test
    public void shouldParseProjectUri() throws URISyntaxException {
        String uri = "ssh://git@host:7999/project/repo.git";
        PullRequestNotifyEvent event = new PullRequestNotifyEvent(new URIish(uri), null);
        assertEquals("project", event.getProject());
        assertEquals("repo", event.getRepository());
    }

    @Test
    public void shouldParseHttpUri() throws URISyntaxException {
        String uri = "http://user1%40host.com@host:7990/scm/project/repo.git";
        PullRequestNotifyEvent event = new PullRequestNotifyEvent(new URIish(uri), null);
        assertEquals("project", event.getProject());
        assertEquals("repo", event.getRepository());
    }

    @Test
    public void shouldNotHaveId() throws URISyntaxException {
        String uri = "ssh://git@host:7999/~user1/repo.git";
        PullRequestNotifyEvent event = new PullRequestNotifyEvent(new URIish(uri), null);
        assertFalse(event.hasId());
    }
}
