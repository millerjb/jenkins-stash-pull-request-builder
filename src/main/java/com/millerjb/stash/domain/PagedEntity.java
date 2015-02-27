package com.millerjb.stash.domain;

public abstract class PagedEntity<T> extends ErrorsEntity {

    T[] values;
    int size;
    long start;
    long limit;
    boolean isLastPage;

    public T[] getValues() {
        return values;
    }

    public void setValues(T[] values) {
        this.values = values;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getLimit() {
        return limit;
    }

    public void setLimit(long limit) {
        this.limit = limit;
    }

    public boolean isLastPage() {
        return isLastPage;
    }

    public void setLastPage(boolean lastPage) {
        isLastPage = lastPage;
    }
}
