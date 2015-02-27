package com.millerjb.jenkins.plugin;

import com.millerjb.jenkins.plugin.event.PullRequestNotifyEvent;
import hudson.Extension;
import hudson.ExtensionPoint;
import hudson.model.AbstractModelObject;
import hudson.model.AbstractProject;
import hudson.model.Cause;
import hudson.model.UnprotectedRootAction;
import jenkins.model.Jenkins;
import org.apache.http.HttpStatus;
import org.eclipse.jgit.transport.URIish;
import org.kohsuke.stapler.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

@Extension
public class PullRequestStatus extends AbstractModelObject implements UnprotectedRootAction {
    private static final Logger logger = LoggerFactory.getLogger(PullRequestStatus.class);

    public String getIconFileName() {
        return null;
    }

    public String getUrlName() {
        return "stash-pull-request";
    }

    public String getDisplayName() {
        return "Stash Pull Request";
    }

    public String getSearchUrl() {
        return getUrlName();
    }

    public HttpResponse doNotify(@QueryParameter(required = true) String url,
                                 @QueryParameter(required = false) Long id) throws ServletException, IOException {
        URIish uri;
        try {
            uri = new URIish(url);
        } catch (URISyntaxException e) {
            logger.error("Illegal url", e);
            return HttpResponses.error(HttpStatus.SC_BAD_REQUEST, new Exception("Illegal url: " + url, e));
        }
        final List<Response> responses = new ArrayList<>();
        for (Listener listener : Jenkins.getInstance().getExtensionList(Listener.class)) {
            responses.addAll(listener.onNotifyPullRequest(uri, id));
        }
        return new HttpResponse() {
            public void generateResponse(StaplerRequest req, StaplerResponse rsp, Object node) throws IOException, ServletException {
                rsp.setStatus(HttpStatus.SC_OK);
                rsp.setContentType("text.plain");
                PrintWriter writer = rsp.getWriter();
                for (Response resp : responses) {
                    resp.writeBody(writer);
                }
            }
        };
    }

    public static abstract class Listener implements ExtensionPoint {
        public abstract List<Response> onNotifyPullRequest(URIish url, Long id);
    }

    public static interface Response {
        public void writeBody(PrintWriter writer);
    }

    public static class MessageResponse implements Response {

        String message;

        public MessageResponse(String message) {
            this.message = message;
        }

        public void writeBody(PrintWriter writer) {
            writer.println(message);
        }
    }

    @Extension
    public static class AbstractProjectListener extends Listener {
        private static final Logger logger = LoggerFactory.getLogger(AbstractProjectListener.class);

        @Override
        public List<Response> onNotifyPullRequest(URIish url, Long id) {
            logger.trace("Triggering PullRequestNotifyEvent on projects with a PullRequestTrigger trigger configured");
            List<Response> responses = new ArrayList<>();
            List<AbstractProject> projects = Jenkins.getInstance().getAllItems(AbstractProject.class);
            for (AbstractProject ap : projects) {
                PullRequestTrigger trigger = (PullRequestTrigger) ap.getTrigger(PullRequestTrigger.class);
                if (trigger != null) {
                    logger.trace("Found PullRequestTrigger on {}", ap.getDisplayName());
                    List<Cause> causes = trigger.pullRequestNotifyEvent(new PullRequestNotifyEvent(url, id));
                    for (Cause cause : causes) {
                        responses.add(new MessageResponse(cause.getShortDescription()));
                    }
                }
            }
            return responses;
        }
    }


}
