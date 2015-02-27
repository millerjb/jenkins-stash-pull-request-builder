package com.millerjb.jenkins.plugin;

import com.millerjb.jenkins.plugin.event.PullRequestBuilder;
import com.millerjb.jenkins.plugin.event.PullRequestNotifyEvent;
import com.millerjb.stash.api.StashHttpConnection;
import com.millerjb.stash.api.StashHttpConnectionException;
import com.millerjb.stash.domain.PullRequest;
import com.millerjb.stash.response.StashResponse;
import com.millerjb.stash.response.StashResponseException;
import edu.umd.cs.findbugs.annotations.Nullable;
import hudson.Extension;
import hudson.model.*;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.GitStatus;
import hudson.plugins.git.UserRemoteConfig;
import hudson.scm.SCM;
import hudson.triggers.Trigger;
import hudson.triggers.TriggerDescriptor;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;
import org.apache.http.HttpStatus;
import org.eclipse.jgit.transport.URIish;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class PullRequestTrigger extends Trigger<AbstractProject> implements PullRequestNotifyListener {

    private static final Logger logger = LoggerFactory.getLogger(PullRequestTrigger.class);

    String baseUrl;
    String username;
    String password;
    private transient AbstractProject myProject;
    private transient PullRequestBuilder eventBuilder;

    @DataBoundConstructor
    public PullRequestTrigger(String baseUrl, String username, String password) {
        super();
        this.baseUrl = baseUrl;
        this.username = username;
        this.password = password;
    }

    @Override
    public void start(AbstractProject project, boolean newInstance) {
        super.start(project, newInstance);
        myProject = project;
        eventBuilder = new PullRequestBuilder(createConnection());
    }

    @Override
    public void stop() {
        super.stop();
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    private StashHttpConnection createConnection() {
        StashHttpConnection conn = new StashHttpConnection();
        try {
            conn.setBaseUrl(baseUrl);
        } catch (URISyntaxException e) {
            logger.error("BaseUrl is invalid", e);
            return null;
        }
        conn.setCredentials(username, password);
        return conn;
    }

    @Override
    public List<Cause> pullRequestNotifyEvent(PullRequestNotifyEvent event) {
        List<Cause> causes = new ArrayList<>();
        if (isInteresting(event)) {
            List<PullRequest> pullRequests = eventBuilder.generateEvents(event, getLastBuildInMillis());
            for (PullRequest pullRequest : pullRequests) {
                causes.add(scheduleBuild(pullRequest));
            }
        }
        return causes;
    }

    /**
     * @return the timestamp of the last build (in millis) or <tt>0</tt>
     */
    private long getLastBuildInMillis() {
        return myProject.getLastBuild() != null ? myProject.getLastBuild().getTimeInMillis() : 0;
    }

    /**
     * Schedules a build with environment variables for the pull request.
     *
     * @return The {@link Cause} given to the scheduling engine.
     */
    private Cause scheduleBuild(PullRequest request) {
        List<ParameterValue> values = new ArrayList<>();
        values.add(new StringParameterValue("STASH_PULL_REQUEST_ID", String.valueOf(request.getId())));
        values.add(new StringParameterValue("STASH_PULL_REQUEST_TO_REF", request.getToRef().getDisplayId()));
        values.add(new StringParameterValue("STASH_PULL_REQUEST_FROM_CHANGESET", request.getFromRef().getLatestChangeset()));
        Cause cause = new PullRequestCause(request.getFromRef().getRepository().getProject().getKey(), request.getFromRef().getRepository().getSlug(), request.getId());
        myProject.scheduleBuild2(0, cause, new ParametersAction(values));
        return cause;
    }

    /**
     * Determine if a {@link PullRequestNotifyEvent} is interesting to this trigger. An event is considered "interesting"
     * if the following conditions are met:
     * - the trigger is enabled
     * - the trigger contains a {@link GitSCM} with a git URL that matches {@link PullRequestNotifyEvent#getUri()}
     */
    private boolean isInteresting(PullRequestNotifyEvent event) {
        if (!myProject.isBuildable()) {
            logger.trace("Disabled.");
            return false;
        }
        SCM scm = myProject.getScm();
        if (scm == null) {
            logger.trace("No SCM configured");
            return false;
        }
        if (scm instanceof GitSCM) {
            GitSCM gitScm = (GitSCM) scm;
            for (UserRemoteConfig userRemoteConfig : gitScm.getUserRemoteConfigs()) {
                if (userRemoteConfig.getUrl().equals(event.getUri().toString())) {
                    return true;
                }
            }
        }
        logger.trace("No matching GitSCM configured");
        return false;
    }

    /**
     * Add support for unverified SSL certificates
     */
    @Extension
    public static final class DescriptorImpl extends TriggerDescriptor {

        private static final Logger logger = LoggerFactory.getLogger(PullRequestTrigger.class);

        public DescriptorImpl() {
            super(PullRequestTrigger.class);
        }

        @Override
        public boolean isApplicable(Item item) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Stash Pull Request";
        }

        public FormValidation doCheckBaseUrl(@QueryParameter("baseUrl") String value) {
            if (value.length() == 0) {
                return FormValidation.error("You must specify a base URL");
            }
            StashHttpConnection conn = new StashHttpConnection();
            try {
                conn.setBaseUrl(value);
            } catch (URISyntaxException e) {
                logger.error("Unable to parse base URL", e);
                return FormValidation.error("Not a valid URL");
            }
            try {
                StashResponse projects = conn.getProjects();
                if (projects.getStatusCode() != HttpStatus.SC_OK) {
                    logger.warn("API returned an unexpected response {}", projects.getEntity());
                    return FormValidation.error("API returned unexpected response %s", projects.getStatusCode());
                }
            } catch (StashHttpConnectionException e) {
                logger.error("Error accessing API", e);
                return FormValidation.error("Unable to parse response from Stash API: %s", e.getMessage());
            } catch (StashResponseException e) {
                logger.error("Error parsing response", e);
                return FormValidation.error("Could not access API. Response: %s - %s", e.getStatusCode(), e.getMessage());
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckUsername(@QueryParameter("username") String value,
                                              @QueryParameter("baseUrl") String baseUrl) {
            if (value.length() == 0) {
                return FormValidation.error("You must specify a username");
            }
            StashHttpConnection conn = new StashHttpConnection();
            try {
                conn.setBaseUrl(baseUrl);
            } catch (URISyntaxException e) {
                logger.error("Unable to parse base URL", e);
                return FormValidation.error("Could not find user");
            }
            try {
                StashResponse projects = conn.getUser(value);
                if (projects.getStatusCode() != HttpStatus.SC_OK) {
                    logger.warn("API returned an unexpected response {}", projects.getEntity());
                    return FormValidation.error("Could not find user");
                }
            } catch (StashHttpConnectionException e) {
                logger.error("Error accessing API", e);
                return FormValidation.error("Could not find user: %s", e.getMessage());
            } catch (StashResponseException e) {
                logger.error("Error parsing response", e);
                return FormValidation.error("Could not find user. Response: %s - %s", e.getStatusCode(), e.getMessage());
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckPassword(@QueryParameter("password") String value,
                                              @QueryParameter("baseUrl") String baseUrl,
                                              @QueryParameter("username") String username) {
            if (value.length() == 0) {
                return FormValidation.error("You must specify a password");
            }
            StashHttpConnection conn = new StashHttpConnection();
            try {
                conn.setBaseUrl(baseUrl);
            } catch (URISyntaxException e) {
                logger.error("Unable to parse base URL", e);
                return FormValidation.error("Could not authenticate");
            }
            conn.setCredentials(username, value);
            try {
                StashResponse projects = conn.getUser(username);
                if (projects.getStatusCode() != HttpStatus.SC_OK) {
                    // TODO: see if it is an error, and grab the error message
                    logger.warn("API returned an unexpected response {}", projects.getEntity());
                    return FormValidation.error("Could not authenticate");
                }
            } catch (StashHttpConnectionException e) {
                logger.error("Error accessing API", e);
                return FormValidation.error("Could not authenticate: %s", e.getMessage());
            } catch (StashResponseException e) {
                logger.error("Error parsing response", e);
                return FormValidation.error("Could not authenticate. Response: %s - %s", e.getStatusCode(), e.getMessage());
            }
            return FormValidation.ok();
        }
    }

    @Extension
    public static class ListenerImpl extends GitStatus.Listener {
        private static final Logger logger = LoggerFactory.getLogger(ListenerImpl.class);

        @Override
        public List<GitStatus.ResponseContributor> onNotifyCommit(URIish uri, @Nullable String sha1, String... branches) {
            logger.trace("Triggering PullRequestNotifyEvent on projects with a PullRequestTrigger scheduleBuild configured");
            List<GitStatus.ResponseContributor> responses = new ArrayList<>();
            List<AbstractProject> projects = Jenkins.getInstance().getAllItems(AbstractProject.class);
            for (AbstractProject ap : projects) {
                PullRequestTrigger trigger = (PullRequestTrigger) ap.getTrigger(PullRequestTrigger.class);
                if (trigger != null) {
                    logger.trace("Found PullRequestTrigger on {}", ap.getDisplayName());
                    List<Cause> causes = trigger.pullRequestNotifyEvent(new PullRequestNotifyEvent(uri, null));
                    for (Cause cause : causes) {
                        responses.add(new GitStatus.MessageResponseContributor(cause.getShortDescription()));
                    }
                }
            }
            return responses;
        }
    }
}
