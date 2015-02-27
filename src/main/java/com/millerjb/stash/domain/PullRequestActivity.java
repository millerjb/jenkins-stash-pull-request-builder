package com.millerjb.stash.domain;

public class PullRequestActivity extends ErrorsEntity {

    private long createdDate;
    private String action;

    public PullRequestActivity(long createdInMillis) {
        this.createdDate = createdInMillis;
    }

    public long getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(long createdDate) {
        this.createdDate = createdDate;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
