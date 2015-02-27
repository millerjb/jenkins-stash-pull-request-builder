package com.millerjb.jenkins.plugin;

import com.millerjb.jenkins.plugin.event.PullRequestNotifyEvent;
import hudson.model.Cause;

import java.util.List;

public interface PullRequestNotifyListener {

    public List<Cause> pullRequestNotifyEvent(PullRequestNotifyEvent event);
}
