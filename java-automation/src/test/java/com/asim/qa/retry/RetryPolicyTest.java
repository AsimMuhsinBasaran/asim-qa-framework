package com.asim.qa.retry;

import org.testng.Assert;
import org.testng.annotations.Test;

public class RetryPolicyTest {

    @Test
    public void should_expose_default_retry_policy_values() {

        RetryPolicy policy = new RetryPolicy();

        Assert.assertEquals(policy.getMaxRetryCount(), 2);
        Assert.assertEquals(policy.getRetryDelayMillis(), 1000L);
    }

    @Test
    public void should_accept_custom_retry_policy_values() {

        RetryPolicy policy = new RetryPolicy(5, 250L);

        Assert.assertEquals(policy.getMaxRetryCount(), 5);
        Assert.assertEquals(policy.getRetryDelayMillis(), 250L);
    }

    @Test
    public void should_report_retry_remaining_correctly() {

        RetryPolicy policy = new RetryPolicy(2, 1000L);

        Assert.assertTrue(policy.hasRetryRemaining(0));
        Assert.assertTrue(policy.hasRetryRemaining(1));
        Assert.assertFalse(policy.hasRetryRemaining(2));
    }
}
