package com.millerjb.stash.filter;

import com.google.common.base.Predicate;
import com.millerjb.stash.domain.PullRequestActivity;

public class PullRequestActivityIsRescoped implements Predicate<PullRequestActivity> {

    @Override
    public boolean apply(PullRequestActivity input) {
        return "RESCOPED".equals(input.getAction());
    }
}
