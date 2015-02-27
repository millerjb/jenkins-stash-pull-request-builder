package com.millerjb.jenkins.plugin.event;

import org.eclipse.jgit.transport.URIish;

/**
 * Represents a notification from Stash that a repository has changes that require action.
 */
public class PullRequestNotifyEvent {

    URIish uri;
    Long id;
    String project;
    String repository;

    /**
     * @param uri {@link URIish} representing the git clone URL of the Stash repository
     * @param id  Identifier of the Pull Request that has triggered a change or <tt>null</tt>
     */
    public PullRequestNotifyEvent(URIish uri, Long id) {
        this.uri = uri;
        this.id = id;
    }

    public URIish getUri() {
        return uri;
    }

    /**
     * @return Identifier of the Pull Request or <tt>null</tt>
     */
    public Long getId() {
        return id;
    }

    /**
     * Whether this event has a Pull Request identifier or not
     *
     * @return
     */
    public boolean hasId() {
        return id != null;
    }

    /**
     * Get the project name from the {@link URIish}
     *
     * @return
     */
    public String getProject() {
        if (project == null) {
            String[] parts = uri.getPath().split("\\/");
            if (parts.length > 1) {
                project = parts[parts.length - 2];
            }
        }
        return project;
    }

    /**
     * Get the repository name from the {@link URIish}
     *
     * @return
     */
    public String getRepository() {
        if (repository == null) {
            String[] parts = uri.getPath().split("\\/");
            if (parts.length > 0) {
                repository = parts[parts.length - 1].split("\\.")[0];
            }
        }
        return repository;
    }
}
