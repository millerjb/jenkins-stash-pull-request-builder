package com.millerjb.stash.api;

import com.millerjb.stash.domain.PullRequest;
import com.millerjb.stash.domain.PullRequestActivityList;
import com.millerjb.stash.domain.PullRequestList;
import com.millerjb.stash.response.StashResponse;
import com.millerjb.stash.response.StashResponseBuilder;
import com.millerjb.stash.response.StashResponseException;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class StashHttpConnection {
    private final AuthCache cache;
    private final HttpContext context;
    private final AuthScheme scheme;
    private static final Logger logger = LoggerFactory.getLogger(StashHttpConnection.class);

    public StashHttpConnection() {
        cache = new BasicAuthCache(); // cache
        context = new BasicHttpContext(); // cache
        context.setAttribute(HttpClientContext.AUTH_CACHE, cache);
        scheme = new BasicScheme(); // cache
    }

    Credentials credentials;

    public void setCredentials(String username, String password) {
        credentials = new UsernamePasswordCredentials(username, password);
    }

    HttpClient client;

    public HttpClient getHttpClient() {
        if (client != null) {
            return client;
        }
        if (credentials != null) {
            HttpHost host = new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme());
            cache.put(host, scheme);
            CredentialsProvider provider = new BasicCredentialsProvider();
            provider.setCredentials(new AuthScope(host), credentials);
            return HttpClients.custom().setDefaultCredentialsProvider(provider).build();
        } else {
            return HttpClients.createDefault();
        }
    }

    private static final String PROJECTSPATH = "rest/api/1.0/projects";
    private static final String USERPATH = "rest/api/1.0/users/%s";
    private static final String OPENPULLREQUESTSPATH = "rest/api/1.0/projects/%s/repos/%s/pull-requests?state=open&order=newest&start=%s";
    private static final String PULLREQUESTPATH = "rest/api/1.0/projects/%s/repos/%s/pull-requests/%s";
    private static final String PULLREQUESTACTIVITIESPATH = "rest/api/1.0/projects/%s/repos/%s/pull-requests/%s/activities";

    URI uri;

    public void setBaseUrl(String url) throws URISyntaxException {
        if (url == null) {
            throw new NullPointerException("url cannot be null");
        }
        // ensure ending slash
        if (!url.endsWith("/")) {
            url += "/";
        }
        this.uri = new URI(url);
    }

    public StashResponse<String> getUser(String user) throws StashHttpConnectionException, StashResponseException {
        HttpGet httpGet = new HttpGet(uri.resolve(String.format(USERPATH, user)));
        HttpResponse response;
        try {
            response = getHttpClient().execute(httpGet, context);
        } catch (IOException e) {
            logger.error("Unable to connect to API", e);
            throw new StashHttpConnectionException(e.getMessage(), e);
        }
        return new StashResponseBuilder<>().buildString(response);
    }

    public StashResponse<String> getProjects() throws StashHttpConnectionException, StashResponseException {
        HttpGet httpGet = new HttpGet(uri.resolve(PROJECTSPATH));
        HttpResponse response;
        try {
            response = getHttpClient().execute(httpGet, context);
        } catch (IOException e) {
            logger.error("Unable to connect to API", e);
            throw new StashHttpConnectionException(e.getMessage(), e);
        }
        return new StashResponseBuilder<>().buildString(response);
    }

    public StashResponse<PullRequest> getPullRequest(String project, String repository, Long id) throws StashHttpConnectionException, StashResponseException {
        HttpGet httpGet = new HttpGet(uri.resolve(String.format(PULLREQUESTPATH, project, repository, id)));
        HttpResponse response;
        try {
            response = getHttpClient().execute(httpGet, context);
        } catch (IOException e) {
            logger.error("Unable to connect to API", e);
            throw new StashHttpConnectionException(e.getMessage(), e);
        }
        return new StashResponseBuilder<PullRequest>().build(PullRequest.class, response);
    }

    public StashResponse<PullRequestList> getOpenPullRequests(String project, String repository, long start) throws StashHttpConnectionException, StashResponseException {
        HttpGet httpGet = new HttpGet(uri.resolve(String.format(OPENPULLREQUESTSPATH, project, repository, start)));
        HttpResponse response;
        try {
            response = getHttpClient().execute(httpGet, context);
        } catch (IOException e) {
            logger.error("Unable to connect to API", e);
            throw new StashHttpConnectionException(e.getMessage(), e);
        }
        return new StashResponseBuilder<PullRequestList>().build(PullRequestList.class, response);
    }

    public StashResponse<PullRequestActivityList> getPullRequestActivities(String project, String repository, Long id) throws StashHttpConnectionException, StashResponseException {
        HttpGet httpGet = new HttpGet(uri.resolve(String.format(PULLREQUESTACTIVITIESPATH, project, repository, id)));
        HttpResponse response;
        try {
            response = getHttpClient().execute(httpGet, context);
        } catch (IOException e) {
            logger.error("Unable to connect to API", e);
            throw new StashHttpConnectionException(e.getMessage(), e);
        }
        return new StashResponseBuilder<PullRequestActivityList>().build(PullRequestActivityList.class, response);
    }

}
