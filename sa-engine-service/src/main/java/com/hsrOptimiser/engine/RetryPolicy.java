package com.hsrOptimiser.engine;

import lombok.extern.slf4j.Slf4j;

/**
 * Encapsulates retry/backoff logic for resilient API calls.
 */
@Slf4j
public class RetryPolicy {

    public static final int MAX_CONSECUTIVE_FAILURES = 100;

    private int consecutiveFailures;

    /**
     * Records a successful call, resetting the failure counter.
     */
    public void recordSuccess() {
        consecutiveFailures = 0;
    }

    /**
     * Records a failure and returns the backoff time in milliseconds.
     */
    public long recordFailure() {
        consecutiveFailures++;
        return calculateBackoff();
    }

    /**
     * Returns the current number of consecutive failures.
     */
    public int getConsecutiveFailures() {
        return consecutiveFailures;
    }

    /**
     * Checks whether the maximum consecutive failures have been exceeded.
     */
    public boolean hasExceededMaxFailures() {
        return consecutiveFailures >= MAX_CONSECUTIVE_FAILURES;
    }

    /**
     * Applies the current backoff by sleeping.
     */
    public void applyBackoff() {
        long backoffTime = calculateBackoff();
        try {
            Thread.sleep(backoffTime);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    private long calculateBackoff() {
        return Math.min(2000, 200L * consecutiveFailures);
    }
}
