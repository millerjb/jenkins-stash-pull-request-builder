package com.millerjb.stash.domain;

public class PullRequest extends ErrorsEntity {

    private long id;
    private long updatedDate;
    private long createdDate;
    private PullRequestRef toRef;
    private PullRequestRef fromRef;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(long updatedDate) {
        this.updatedDate = updatedDate;
    }

    public long getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(long createdDate) {
        this.createdDate = createdDate;
    }

    public PullRequestRef getToRef() {
        return toRef;
    }

    public void setToRef(PullRequestRef toRef) {
        this.toRef = toRef;
    }

    public PullRequestRef getFromRef() {
        return fromRef;
    }

    public void setFromRef(PullRequestRef fromRef) {
        this.fromRef = fromRef;
    }
}
