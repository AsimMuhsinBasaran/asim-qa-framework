package com.asim.qa.utils;

import com.asim.qa.config.ConfigReader;
import com.asim.qa.context.TestContext;
import io.qameta.allure.Allure;
import io.restassured.http.Header;
import io.restassured.response.Response;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AllureUtils {

    public static void attachRequest(String title, String body) {

        Allure.addAttachment(titleWithContext(title), body == null ? "[no body]" : SensitiveDataMasker.mask(body));
    }

    public static void attachResponse(String title, String body) {

        Allure.addAttachment(titleWithContext(title), body == null ? "[no body]" : SensitiveDataMasker.mask(body));
    }

    public static void attachPollingSummary(String body) {

        Allure.addAttachment(titleWithContext("Polling Summary"), body == null ? "[no summary]" : SensitiveDataMasker.mask(body));
    }

    public static void attachApiRequest(String title,
                                        String method,
                                        String endpoint,
                                        Map<String, String> headers,
                                        Map<String, String> pathParams,
                                        Map<String, String> queryParams,
                                        String body) {

        attachRequest(title, formatApiRequest(method, endpoint, headers, pathParams, queryParams, body));
    }

    public static void attachApiResponse(String title, Response response) {

        attachResponse(title, formatApiResponse(response));
    }

    public static void attachApiFailureDiagnostics(TestContext context) {

        TestContext.LastRequest lastRequest = context.getLastRequest();

        if (lastRequest == null) {
            attachRequest("Failure - API Request", "[no request captured]");
        } else {
            attachApiRequest(
                    "Failure - API Request",
                    lastRequest.getMethod(),
                    lastRequest.getEndpoint(),
                    lastRequest.getHeaders(),
                    lastRequest.getPathParams(),
                    lastRequest.getQueryParams(),
                    lastRequest.getBody()
            );
        }

        attachApiResponse("Failure - API Response", context.getResponse());
        attachRequest("Failure - Scenario Context", formatScenarioVariables(context.getScenarioVariablesSnapshot()));
    }

    public static void writeEnvironmentProperties(Path allureResultsDir) {

        List<String> lines = new ArrayList<>();
        lines.add("Environment=" + ConfigReader.getActiveEnv());
        lines.add("JavaVersion=" + System.getProperty("java.version", "unknown"));
        lines.add("OS=" + System.getProperty("os.name", "unknown"));
        lines.add("RunnerClass=" + System.getProperty("runner.class", "ApiTestRunner"));

        String cucumberTags = System.getProperty("cucumber.filter.tags");
        if (cucumberTags != null && !cucumberTags.isBlank()) {
            lines.add("CucumberTags=" + cucumberTags.trim());
        }

        Path environmentFile = allureResultsDir.resolve("environment.properties");

        try {
            Files.write(environmentFile, lines);
        } catch (IOException e) {
            throw new RuntimeException("Allure environment.properties yazılamadı: " + environmentFile, e);
        }
    }

    private static String titleWithContext(String title) {

        StringBuilder builder = new StringBuilder();
        builder.append("[thread=").append(Thread.currentThread().getName()).append("]");
        builder.append("[scenario=").append(resolveScenarioName()).append("]");

        String requestId = RequestCorrelationContext.getRequestId();
        if (requestId != null && !requestId.isBlank()) {
            builder.append("[requestId=").append(requestId).append("]");
        }

        builder.append(" ").append(title);
        return builder.toString();
    }

    static String formatApiRequest(String method,
                                   String endpoint,
                                   Map<String, String> headers,
                                   Map<String, String> pathParams,
                                   Map<String, String> queryParams,
                                   String body) {

        StringBuilder builder = new StringBuilder();
        builder.append("Request").append(System.lineSeparator());
        builder.append("Method: ").append(valueOrUnknown(method)).append(System.lineSeparator());
        builder.append("Endpoint: ").append(valueOrUnknown(endpoint)).append(System.lineSeparator());
        appendMap(builder, "Headers", headers);
        appendMap(builder, "Path Params", pathParams);
        appendMap(builder, "Query Params", queryParams);
        builder.append("Body:").append(System.lineSeparator());
        builder.append(body == null || body.isBlank() ? "[no body]" : body);

        return builder.toString();
    }

    static String formatApiResponse(Response response) {

        if (response == null) {
            return "[no response captured]";
        }

        StringBuilder builder = new StringBuilder();
        builder.append("Response").append(System.lineSeparator());
        builder.append("Status Code: ").append(response.getStatusCode()).append(System.lineSeparator());
        builder.append("Headers:").append(System.lineSeparator());

        for (Header header : response.getHeaders().asList()) {
            builder.append(header.getName()).append(": ").append(header.getValue()).append(System.lineSeparator());
        }

        builder.append("Body:").append(System.lineSeparator());
        builder.append(readResponseBody(response));

        return builder.toString();
    }

    static String formatScenarioVariables(Map<String, String> variables) {

        if (variables == null || variables.isEmpty()) {
            return "[no scenario variables]";
        }

        StringBuilder builder = new StringBuilder();
        builder.append("Scenario Variables").append(System.lineSeparator());

        for (Map.Entry<String, String> variable : variables.entrySet()) {
            builder.append(variable.getKey()).append("=").append(variable.getValue()).append(System.lineSeparator());
        }

        return builder.toString();
    }

    private static void appendMap(StringBuilder builder, String title, Map<String, String> values) {

        builder.append(title).append(":").append(System.lineSeparator());

        if (values == null || values.isEmpty()) {
            builder.append("[empty]").append(System.lineSeparator());
            return;
        }

        for (Map.Entry<String, String> entry : values.entrySet()) {
            builder.append(entry.getKey()).append("=").append(entry.getValue()).append(System.lineSeparator());
        }
    }

    private static String readResponseBody(Response response) {

        try {
            return response.asPrettyString();
        } catch (RuntimeException e) {
            return response.asString();
        }
    }

    private static String valueOrUnknown(String value) {

        return value == null || value.isBlank() ? "[unknown]" : value;
    }

    private static String resolveScenarioName() {

        String scenarioName = ConsoleLogger.getScenarioName();

        if (scenarioName == null || scenarioName.isBlank()) {
            return "unknown";
        }

        return scenarioName;
    }
}
