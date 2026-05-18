package com.asim.qa.api;

import com.asim.qa.context.TestContext;
import com.asim.qa.utils.ScenarioContextUtils;
import io.restassured.specification.RequestSpecification;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RequestParamManager {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{([^{}]+)}");

    private final Map<String, String> pathParams = new LinkedHashMap<>();
    private final Map<String, String> queryParams = new LinkedHashMap<>();

    public synchronized void addPathParam(String key, String value) {

        addParam(pathParams, "path", key, value, null);
    }

    public synchronized void addPathParam(TestContext context, String key, String value) {

        addParam(pathParams, "path", key, value, context);
    }

    public synchronized void addQueryParam(String key, String value) {

        addParam(queryParams, "query", key, value, null);
    }

    public synchronized void addQueryParam(TestContext context, String key, String value) {

        addParam(queryParams, "query", key, value, context);
    }

    public synchronized boolean hasPathParam(String key) {

        return pathParams.containsKey(key);
    }

    public synchronized Map<String, String> getPathParamsSnapshot() {

        return new LinkedHashMap<>(pathParams);
    }

    public synchronized Map<String, String> getQueryParamsSnapshot() {

        return new LinkedHashMap<>(queryParams);
    }

    public synchronized void clear() {

        pathParams.clear();
        queryParams.clear();
    }

    public synchronized RequestSpecification applyTo(RequestSpecification requestSpecification) {

        if (!pathParams.isEmpty()) {
            requestSpecification.pathParams(pathParams);
        }

        if (!queryParams.isEmpty()) {
            requestSpecification.queryParams(queryParams);
        }

        return requestSpecification;
    }

    public synchronized RequestSpecification applyTo(RequestSpecification requestSpecification,
                                                     Map<String, String> requestPathParams,
                                                     Map<String, String> requestQueryParams) {

        if (requestPathParams != null && !requestPathParams.isEmpty()) {
            requestSpecification.pathParams(new LinkedHashMap<>(requestPathParams));
        }

        if (requestQueryParams != null && !requestQueryParams.isEmpty()) {
            requestSpecification.queryParams(new LinkedHashMap<>(requestQueryParams));
        }

        return requestSpecification;
    }

    public synchronized String resolveEndpoint(TestContext context, String endpoint) {

        if (endpoint == null || !endpoint.contains("{")) {
            return endpoint;
        }

        Matcher matcher = PLACEHOLDER_PATTERN.matcher(endpoint);
        StringBuffer resolvedEndpoint = new StringBuffer();

        while (matcher.find()) {
            String placeholderName = matcher.group(1);

            if (pathParams.containsKey(placeholderName)) {
                matcher.appendReplacement(resolvedEndpoint, Matcher.quoteReplacement(matcher.group()));
                continue;
            }

            if (context != null && context.contains(placeholderName)) {
                String resolvedValue = ScenarioContextUtils.requireVariable(context, placeholderName);
                matcher.appendReplacement(resolvedEndpoint, Matcher.quoteReplacement(resolvedValue));
                continue;
            }

            throw new RuntimeException(
                    "Unresolved endpoint placeholder: " + placeholderName +
                            ". Define a path param or scenario variable before sending the request."
            );
        }

        matcher.appendTail(resolvedEndpoint);
        return resolvedEndpoint.toString();
    }

    private void addParam(Map<String, String> target,
                          String paramType,
                          String key,
                          String value,
                          TestContext context) {

        validateKey(paramType, key);
        validateValue(paramType, key, value);

        String resolvedValue = resolveValue(context, paramType, key, value);
        target.put(key.trim(), resolvedValue);
    }

    private String resolveValue(TestContext context, String paramType, String key, String value) {

        if (value == null) {
            throw new IllegalArgumentException(
                    capitalize(paramType) + " param value must not be null for key: " + key.trim()
            );
        }

        if (!value.contains("{")) {
            return value.trim();
        }

        if (context == null) {
            throw new RuntimeException(
                    "Scenario placeholder cannot be resolved for " + paramType + " param \"" +
                            key.trim() + "\" without scenario context."
            );
        }

        return ScenarioContextUtils.resolveText(context, value).trim();
    }

    private void validateKey(String paramType, String key) {

        if (key == null) {
            throw new IllegalArgumentException(capitalize(paramType) + " param key must not be null.");
        }

        if (key.isBlank()) {
            throw new IllegalArgumentException(capitalize(paramType) + " param key must not be blank.");
        }
    }

    private void validateValue(String paramType, String key, String value) {

        if (value == null) {
            throw new IllegalArgumentException(
                    capitalize(paramType) + " param value must not be null for key: " + key.trim()
            );
        }

        if (value.isBlank()) {
            throw new IllegalArgumentException(
                    capitalize(paramType) + " param value must not be blank for key: " + key.trim()
            );
        }
    }

    private String capitalize(String value) {

        if (value == null || value.isBlank()) {
            return "";
        }

        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }
}
