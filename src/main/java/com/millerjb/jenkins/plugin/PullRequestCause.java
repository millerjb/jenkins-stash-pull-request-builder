package com.millerjb.jenkins.plugin;

import hudson.model.Cause;

public class PullRequestCause extends Cause {

    Long id;
    String project;
    String repository;

    public PullRequestCause(String project, String repository, Long id) {
        this.project = project;
        this.repository = repository;
        this.id = id;
    }

    @Override
    public String getShortDescription() {
        return "Building pull request #" + getId() + " for " + getProject() + "/" + getRepository();
    }

    public String getProject() {
        return project;
    }

    public String getRepository() {
        return repository;
    }

    public Long getId() {
        return id;
    }
}
