package com.asim.qa.retry;

import com.asim.qa.config.ConfigReader;

public final class RetryPolicy {

    private static final int DEFAULT_MAX_RETRY_COUNT = 2;
    private static final long DEFAULT_RETRY_DELAY_MILLIS = 1000L;

    private final int maxRetryCount;
    private final long retryDelayMillis;

    public RetryPolicy() {

        this(DEFAULT_MAX_RETRY_COUNT, DEFAULT_RETRY_DELAY_MILLIS);
    }

    public RetryPolicy(int maxRetryCount, long retryDelayMillis) {

        if (maxRetryCount < 0) {
            throw new IllegalArgumentException("Retry max count must be zero or greater: " + maxRetryCount);
        }

        if (retryDelayMillis < 0) {
            throw new IllegalArgumentException("Retry delay must be zero or greater: " + retryDelayMillis);
        }

        this.maxRetryCount = maxRetryCount;
        this.retryDelayMillis = retryDelayMillis;
    }

    public static RetryPolicy defaultPolicy() {

        int maxAttempts = ConfigReader.getApiRetryMaxAttempts();
        int maxRetryCount = maxAttempts - 1;

        return new RetryPolicy(maxRetryCount, ConfigReader.getApiRetryDelayMs());
    }

    public int getMaxRetryCount() {

        return maxRetryCount;
    }

    public long getRetryDelayMillis() {

        return retryDelayMillis;
    }

    public boolean hasRetryRemaining(int retryAttemptIndex) {

        return retryAttemptIndex < maxRetryCount;
    }
}
