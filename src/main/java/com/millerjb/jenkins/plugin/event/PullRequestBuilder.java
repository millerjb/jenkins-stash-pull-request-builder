package com.millerjb.jenkins.plugin.event;

import com.millerjb.stash.api.StashHttpConnection;
import com.millerjb.stash.api.StashHttpConnectionException;
import com.millerjb.stash.domain.PullRequest;
import com.millerjb.stash.domain.PullRequestActivity;
import com.millerjb.stash.domain.PullRequestActivityList;
import com.millerjb.stash.domain.PullRequestList;
import com.millerjb.stash.filter.PullRequestActivityIsRescoped;
import com.millerjb.stash.response.StashResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class PullRequestBuilder {
    private static final Logger logger = LoggerFactory.getLogger(PullRequestBuilder.class);

    StashHttpConnection conn;

    public PullRequestBuilder(StashHttpConnection connection) {
        this.conn = connection;
    }

    private StashHttpConnection getConnection() {
        return conn;
    }

    private PullRequest getPullRequest(PullRequestNotifyEvent event) {
        try {
            return getConnection().getPullRequest(event.getProject(), event.getRepository(), event.getId()).getEntity();
        } catch (StashHttpConnectionException e) {
            logger.error("Unable to fetch pull request {}", event.getId(), e);
            return null;
        } catch (StashResponseException e) {
            logger.error("Unexpected response {} {}", e, e.getStatusCode(), e.getMessage());
            return null;
        }
    }

    public List<PullRequest> generateEvents(PullRequestNotifyEvent event, long lastBuildInMillis) {
        List<PullRequest> events = new ArrayList<>();
        if (event.hasId()) {
            PullRequest request = getPullRequest(event);
            if (request != null) {
                events.add(request);
            }
        } else {
            events.addAll(generateEventsForRepository(event.getProject(), event.getRepository(), lastBuildInMillis, 0));
        }
        return events;
    }

    /**
     * Is there a more reliable way to see if we should build a PR than checking for its new commits?
     */
    private List<PullRequest> generateEventsForRepository(String project, String repository, long lastBuildInMillis, long start) {
        List<PullRequest> pullRequests = new ArrayList<>();
        // get list of open PRs from Stash
        PullRequestList openPullRequests;
        try {
            openPullRequests = getConnection().getOpenPullRequests(project, repository, start).getEntity();
        } catch (StashHttpConnectionException e) {
            logger.error("Unable to fetch open pull requests", e);
            return pullRequests;
        } catch (StashResponseException e) {
            logger.error("Unexpected response {} {}", e, e.getStatusCode(), e.getMessage());
            return pullRequests;
        }
        prs:
        for (PullRequest request : openPullRequests.getValues()) {
            // pull requests are sorted by updated date desc, so if it is old, then stop iterating
            if (request.getUpdatedDate() < lastBuildInMillis) {
                break;
            }
            // check if the pull request is newer than the last build
            if (lastBuildInMillis < request.getCreatedDate()) {
                pullRequests.add(request);
            }
            // check if the pull request has been updated with new commits since the last time we built
            else {
                PullRequestActivityList rescopeActivities;
                try {
                    rescopeActivities = getConnection().getPullRequestActivities(project, repository, request.getId()).getEntity();
                } catch (StashHttpConnectionException e) {
                    logger.error("Uable to fetch activities for pull request #" + request.getId(), e);
                    continue;
                } catch (StashResponseException e) {
                    logger.error("Unexpected response {} {}", e, e.getStatusCode(), e.getMessage());
                    continue;
                }
                for (PullRequestActivity activity : rescopeActivities.getValues()) {
                    // activities should be ordered by date desc, so stop iterating if we find one older than the last build
                    if (lastBuildInMillis > activity.getCreatedDate()) {
                        continue prs;
                    }
                    // if a RESCOPED is found, add this pull request to the list
                    if (new PullRequestActivityIsRescoped().apply(activity)) {
                        pullRequests.add(request);
                        continue prs;
                    }
                }
            }
        }
        // if there are additional pages
        if (!openPullRequests.isLastPage()
                // and if the last pull request in the page is newer than the last build, fetch another page
                && lastBuildInMillis < openPullRequests.getValues()[openPullRequests.getValues().length - 1].getUpdatedDate()) {
            long newStart = openPullRequests.getStart() + openPullRequests.getLimit();
            pullRequests.addAll(generateEventsForRepository(project, repository, lastBuildInMillis, newStart));
        }
        return pullRequests;
    }
}
