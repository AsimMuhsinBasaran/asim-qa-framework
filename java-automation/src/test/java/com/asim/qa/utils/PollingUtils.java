package com.asim.qa.utils;

import com.asim.qa.api.ApiClient;
import com.asim.qa.context.TestContext;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class PollingUtils {

    private PollingUtils() {
    }

    public static void pollLastRequestUntilStatusCode(TestContext context,
                                                      ApiClient apiClient,
                                                      int expectedStatusCode,
                                                      long timeoutSeconds,
                                                      long intervalSeconds) {

        poll(
                context,
                apiClient,
                timeoutSeconds,
                intervalSeconds,
                "response status code should be " + expectedStatusCode,
                () -> context.getResponse().getStatusCode(),
                () -> context.getResponse().getStatusCode() == expectedStatusCode
        );
    }

    public static void pollLastRequestUntilFieldEquals(TestContext context,
                                                       ApiClient apiClient,
                                                       String field,
                                                       String expectedValue,
                                                       long timeoutSeconds,
                                                       long intervalSeconds) {

        poll(
                context,
                apiClient,
                timeoutSeconds,
                intervalSeconds,
                "response field \"" + field + "\" should be \"" + expectedValue + "\"",
                () -> context.getJsonPath().get(field),
                () -> Objects.equals(String.valueOf((Object) context.getJsonPath().get(field)), expectedValue)
        );
    }

    public static void pollLastRequestUntilFieldNotNull(TestContext context,
                                                        ApiClient apiClient,
                                                        String field,
                                                        long timeoutSeconds,
                                                        long intervalSeconds) {

        poll(
                context,
                apiClient,
                timeoutSeconds,
                intervalSeconds,
                "response field \"" + field + "\" should not be null",
                () -> context.getJsonPath().get(field),
                () -> context.getJsonPath().get(field) != null
        );
    }

    private static void poll(TestContext context,
                             ApiClient apiClient,
                             long timeoutSeconds,
                             long intervalSeconds,
                             String conditionDescription,
                             Supplier<Object> actualValueSupplier,
                             Supplier<Boolean> condition) {

        TestContext.LastRequest lastRequest = requireGetLastRequest(context);

        if (timeoutSeconds < 0) {
            throw new IllegalArgumentException("Polling timeout must be zero or greater: " + timeoutSeconds);
        }

        if (intervalSeconds <= 0) {
            throw new IllegalArgumentException("Polling interval must be greater than zero: " + intervalSeconds);
        }

        long deadlineNanos = System.nanoTime() + TimeUnit.SECONDS.toNanos(timeoutSeconds);
        int attempts = 0;
        Object lastActualValue = null;
        List<String> summaryLines = new ArrayList<>();

        while (true) {
            RequestCorrelationContext.startRequest();

            try {
                attempts++;
                updateContextWithPollAttempt(context, apiClient, lastRequest);
                lastActualValue = actualValueSupplier.get();
                summaryLines.add(formatAttemptSummary(attempts, conditionDescription, lastActualValue));

                ConsoleLogger.info("Poll Attempt", attempts);
                ConsoleLogger.info("Poll Condition", conditionDescription);
                ConsoleLogger.info("Poll Actual", lastActualValue);

                if (Boolean.TRUE.equals(condition.get())) {
                    AllureUtils.attachPollingSummary(buildPollingSummary(conditionDescription, summaryLines, true, attempts, lastActualValue));
                    ConsoleLogger.pass("Polling condition sağlandı");
                    return;
                }

                long remainingNanos = deadlineNanos - System.nanoTime();

                if (remainingNanos <= 0) {
                    AllureUtils.attachPollingSummary(buildPollingSummary(conditionDescription, summaryLines, false, attempts, lastActualValue));
                    throw new AssertionError(
                            "Polling timed out after " + timeoutSeconds + " seconds and " + attempts +
                                    " attempts. Condition: " + conditionDescription +
                                    ". Last actual value: " + lastActualValue +
                                    ". Last request: GET " + lastRequest.getEndpoint()
                    );
                }

                sleep(Math.min(TimeUnit.SECONDS.toNanos(intervalSeconds), remainingNanos));
            } finally {
                RequestCorrelationContext.clear();
            }
        }
    }

    private static String buildPollingSummary(String conditionDescription,
                                              List<String> summaryLines,
                                              boolean success,
                                              int attempts,
                                              Object lastActualValue) {

        StringBuilder builder = new StringBuilder();
        builder.append("Condition: ").append(conditionDescription).append(System.lineSeparator());
        builder.append("Result: ").append(success ? "SUCCESS" : "TIMEOUT").append(System.lineSeparator());
        builder.append("Attempts: ").append(attempts).append(System.lineSeparator());
        if (lastActualValue != null) {
            builder.append("LastActualValue: ").append(lastActualValue).append(System.lineSeparator());
        }
        builder.append(System.lineSeparator());
        builder.append("Attempt Details:").append(System.lineSeparator());

        if (summaryLines.isEmpty()) {
            builder.append("[no attempts]").append(System.lineSeparator());
            return builder.toString();
        }

        for (String summaryLine : summaryLines) {
            builder.append(summaryLine).append(System.lineSeparator());
        }

        return builder.toString();
    }

    private static String formatAttemptSummary(int attemptNumber,
                                               String conditionDescription,
                                               Object actualValue) {

        return "Attempt " + attemptNumber +
                " | Condition: " + conditionDescription +
                " | Actual: " + actualValue;
    }

    private static TestContext.LastRequest requireGetLastRequest(TestContext context) {

        TestContext.LastRequest lastRequest = context.getLastRequest();

        if (lastRequest == null) {
            throw new IllegalStateException("Cannot poll last request because no request has been sent yet.");
        }

        if (!"GET".equalsIgnoreCase(lastRequest.getMethod())) {
            throw new IllegalStateException(
                    "Polling supports only GET requests. Last request method was: " + lastRequest.getMethod()
            );
        }

        return lastRequest;
    }

    private static void updateContextWithPollAttempt(TestContext context,
                                                     ApiClient apiClient,
                                                     TestContext.LastRequest lastRequest) {

        Response response = apiClient.sendRequest(
                lastRequest.getMethod(),
                lastRequest.getEndpoint(),
                lastRequest.getBody(),
                lastRequest.getHeaders(),
                lastRequest.getPathParams(),
                lastRequest.getQueryParams()
        );

        context.setResponse(response);
        context.setJsonPath(new JsonPath(jsonBodyOrEmptyObject(response)));
    }

    private static String jsonBodyOrEmptyObject(Response response) {

        String body = response.asString();

        return body == null || body.isBlank() ? "{}" : body;
    }

    private static void sleep(long intervalNanos) {

        try {
            TimeUnit.NANOSECONDS.sleep(intervalNanos);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Polling interrupted while waiting for next attempt.", e);
        }
    }
}
