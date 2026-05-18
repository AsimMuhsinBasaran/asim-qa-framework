package com.asim.qa.retry;

public final class RetryDecision {

    private final boolean retryable;
    private final String reason;

    private RetryDecision(boolean retryable, String reason) {

        this.retryable = retryable;
        this.reason = reason;
    }

    public static RetryDecision retryable(String reason) {

        return new RetryDecision(true, reason);
    }

    public static RetryDecision nonRetryable(String reason) {

        return new RetryDecision(false, reason);
    }

    public boolean isRetryable() {

        return retryable;
    }

    public String getReason() {

        return reason;
    }
}
