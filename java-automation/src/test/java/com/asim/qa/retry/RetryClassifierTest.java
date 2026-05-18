package com.asim.qa.retry;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.net.ssl.SSLHandshakeException;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class RetryClassifierTest {

    private final RetryClassifier classifier = new RetryClassifier();

    @Test(dataProvider = "retryableStatuses")
    public void should_classify_retryable_http_statuses(int statusCode) {

        RetryDecision decision = classifier.classifyStatus(statusCode);

        Assert.assertTrue(decision.isRetryable());
    }

    @DataProvider
    public Object[][] retryableStatuses() {

        return new Object[][]{
                {429},
                {502},
                {503},
                {504}
        };
    }

    @Test
    public void should_classify_socket_timeout_as_retryable() {

        RetryDecision decision = classifier.classify(new SocketTimeoutException("timeout"));

        Assert.assertTrue(decision.isRetryable());
        Assert.assertEquals(decision.getReason(), "Socket timeout");
    }

    @Test
    public void should_classify_connect_exception_as_retryable() {

        RetryDecision decision = classifier.classify(new ConnectException("connection refused"));

        Assert.assertTrue(decision.isRetryable());
        Assert.assertEquals(decision.getReason(), "Connect exception");
    }

    @Test
    public void should_classify_connection_reset_as_retryable() {

        RetryDecision decision = classifier.classify(new SocketException("Connection reset by peer"));

        Assert.assertTrue(decision.isRetryable());
        Assert.assertEquals(decision.getReason(), "Connection reset");
    }

    @Test
    public void should_classify_ssl_handshake_as_non_retryable() {

        RetryDecision decision = classifier.classify(new SSLHandshakeException("bad certificate"));

        Assert.assertFalse(decision.isRetryable());
        Assert.assertEquals(decision.getReason(), "SSL handshake exception");
    }

    @Test
    public void should_classify_unknown_host_as_non_retryable() {

        RetryDecision decision = classifier.classify(new UnknownHostException("host not found"));

        Assert.assertFalse(decision.isRetryable());
        Assert.assertEquals(decision.getReason(), "Unknown host");
    }

    @Test
    public void should_classify_assertion_error_as_non_retryable() {

        RetryDecision decision = classifier.classify(new AssertionError("assertion failed"));

        Assert.assertFalse(decision.isRetryable());
        Assert.assertEquals(decision.getReason(), "Assertion failure");
    }
}
