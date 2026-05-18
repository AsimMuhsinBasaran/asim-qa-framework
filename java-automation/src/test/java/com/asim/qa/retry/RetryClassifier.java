package com.asim.qa.retry;

import io.restassured.response.Response;

import javax.net.ssl.SSLHandshakeException;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public final class RetryClassifier {

    public RetryDecision classify(Response response) {

        if (response == null) {
            return RetryDecision.nonRetryable("Response is null");
        }

        return classifyStatus(response.statusCode());
    }

    public RetryDecision classifyStatus(int statusCode) {

        return switch (statusCode) {
            case 429 -> RetryDecision.retryable("HTTP status 429");
            case 502, 503, 504 -> RetryDecision.retryable("HTTP status " + statusCode);
            default -> RetryDecision.nonRetryable("HTTP status " + statusCode);
        };
    }

    public RetryDecision classify(Throwable throwable) {

        if (throwable == null) {
            return RetryDecision.nonRetryable("Throwable is null");
        }

        Throwable current = throwable;

        while (current != null) {
            if (current instanceof AssertionError) {
                return RetryDecision.nonRetryable("Assertion failure");
            }

            if (current instanceof SSLHandshakeException) {
                return RetryDecision.nonRetryable("SSL handshake exception");
            }

            if (current instanceof SocketTimeoutException) {
                return RetryDecision.retryable("Socket timeout");
            }

            if (current instanceof ConnectException) {
                return RetryDecision.retryable("Connect exception");
            }

            if (current instanceof UnknownHostException) {
                return RetryDecision.nonRetryable("Unknown host");
            }

            if (isConnectionReset(current)) {
                return RetryDecision.retryable("Connection reset");
            }

            current = current.getCause();
        }

        return RetryDecision.nonRetryable(throwable.getClass().getSimpleName());
    }

    private boolean isConnectionReset(Throwable throwable) {

        String message = throwable.getMessage();

        return throwable instanceof SocketException
                && message != null
                && message.toLowerCase().contains("connection reset");
    }
}
