package com.asim.qa.api;

import com.asim.qa.auth.AuthManager;
import com.asim.qa.context.TestContext;
import com.asim.qa.config.ConfigReader;
import com.asim.qa.retry.RetryClassifier;
import com.asim.qa.retry.RetryDecision;
import com.asim.qa.retry.RetryPolicy;
import com.asim.qa.utils.ConsoleLogger;
import io.restassured.builder.ResponseBuilder;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.internal.http.HttpResponseDecorator;
import io.restassured.internal.http.HttpResponseException;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static io.restassured.config.HttpClientConfig.httpClientConfig;
import static io.restassured.config.RestAssuredConfig.config;

public class ApiClient {

    private final Map<String, String> headers = new HashMap<>();
    private final RequestParamManager requestParamManager = new RequestParamManager();
    private final AuthManager authManager = new AuthManager();
    private final RetryClassifier retryClassifier = new RetryClassifier();
    private final RetryPolicy retryPolicy = RetryPolicy.defaultPolicy();
    private String baseUrl;

    public Map<String, String> getHeaders() {

        return authManager.applyToHeaders(headers);
    }

    public Map<String, String> getHeadersSnapshot() {

        return getHeaders();
    }

    public Map<String, String> getPathParamsSnapshot() {

        return requestParamManager.getPathParamsSnapshot();
    }

    public Map<String, String> getQueryParamsSnapshot() {

        return requestParamManager.getQueryParamsSnapshot();
    }

    public ApiClient() {

        headers.put("Content-Type", "application/json");
    }

    public void addHeader(String key, String value) {

        headers.put(key, value);
    }

    public void clearAuth() {

        authManager.clear();
    }

    public void useBearerAuth(String token) {

        authManager.useBearerAuth(token);
    }

    public void useBearerAuth(TestContext context, String token) {

        authManager.useBearerAuth(context, token);
    }

    public void useBasicAuth(String username, String password) {

        authManager.useBasicAuth(username, password);
    }

    public void useBasicAuth(TestContext context, String username, String password) {

        authManager.useBasicAuth(context, username, password);
    }

    public void useApiKeyAuth(String headerName, String apiKey) {

        authManager.useApiKeyAuth(headerName, apiKey);
    }

    public void useApiKeyAuth(TestContext context, String headerName, String apiKey) {

        authManager.useApiKeyAuth(context, headerName, apiKey);
    }

    public void setBaseUrl(String baseUrl) {

        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("Base URL must not be blank.");
        }

        this.baseUrl = baseUrl;
    }

    public void clearBaseUrl() {

        baseUrl = null;
    }

    public void addPathParam(String key, String value) {

        requestParamManager.addPathParam(key, value);
    }

    public void addPathParam(TestContext context, String key, String value) {

        requestParamManager.addPathParam(context, key, value);
    }

    public void addQueryParam(String key, String value) {

        requestParamManager.addQueryParam(key, value);
    }

    public void addQueryParam(TestContext context, String key, String value) {

        requestParamManager.addQueryParam(context, key, value);
    }

    public boolean hasPathParam(String key) {

        return requestParamManager.hasPathParam(key);
    }

    public void clearHeaders() {

        headers.clear();

        headers.put("Content-Type", "application/json");
    }

    public void clearParams() {

        requestParamManager.clear();
    }

    private RequestSpecification requestSpec() {

        return requestSpec(headers);
    }

    private RequestSpecification requestSpec(Map<String, String> requestHeaders) {

        int timeoutMs = ConfigReader.getApiTimeoutMs();
        Map<String, String> effectiveHeaders = authManager.applyToHeaders(requestHeaders);

        return given()
                .baseUri(requireBaseUrl())
                .config(config().httpClient(
                        httpClientConfig()
                                .setParam("http.connection.timeout", timeoutMs)
                                .setParam("http.socket.timeout", timeoutMs)
                                .setParam("http.connection-manager.timeout", (long) timeoutMs)
                ))
                .headers(effectiveHeaders);
    }

    public String resolveEndpoint(TestContext context, String endpoint) {

        return requestParamManager.resolveEndpoint(context, endpoint);
    }

    private String requireBaseUrl() {

        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalStateException("Base URL is not configured. Call 'Given base url is configured' before sending a request.");
        }

        return baseUrl;
    }

    public Response get(String endpoint) {

        return sendRequest("GET", endpoint, null);
    }

    public Response post(String endpoint, String body) {

        return sendRequest("POST", endpoint, body);
    }

    public Response sendRequest(String method, String endpoint, String body) {

        RequestSpecification spec = requestSpec();
        requestParamManager.applyTo(spec);

        if (body != null && !body.isBlank()) {
            spec.body(body);
        }

        String normalizedMethod = method.toUpperCase();

        try {
            return switch (normalizedMethod) {
                case "GET", "POST", "PUT", "PATCH", "DELETE" -> executeRequestWithRetry(spec, normalizedMethod, endpoint);
                default -> throw new IllegalArgumentException("Unsupported HTTP method: " + method);
            };
        } finally {
            clearParams();
        }
    }

    public Response sendRequest(String method,
                                String endpoint,
                                String body,
                                Map<String, String> requestHeaders,
                                Map<String, String> requestPathParams,
                                Map<String, String> requestQueryParams) {

        RequestSpecification spec = requestSpec(requestHeaders);
        requestParamManager.applyTo(spec, requestPathParams, requestQueryParams);

        if (body != null && !body.isBlank()) {
            spec.body(body);
        }

        String normalizedMethod = method.toUpperCase();

        return switch (normalizedMethod) {
            case "GET", "POST", "PUT", "PATCH", "DELETE" -> executeRequestWithRetry(spec, normalizedMethod, endpoint);
            default -> throw new IllegalArgumentException("Unsupported HTTP method: " + method);
        };
    }

    private Response executeRequestWithRetry(RequestSpecification spec, String method, String endpoint) {

        if (!"GET".equals(method)) {
            return executeRequest(spec, method, endpoint);
        }

        for (int attempt = 0; attempt <= retryPolicy.getMaxRetryCount(); attempt++) {
            try {
                Response response = executeRequest(spec, method, endpoint);

                RetryDecision decision = retryClassifier.classify(response);

                if (!decision.isRetryable() || attempt == retryPolicy.getMaxRetryCount()) {
                    return response;
                }

                logRetry(attempt + 1, decision);
            } catch (Exception e) {
                RetryDecision decision = retryClassifier.classify(e);

                if (!decision.isRetryable() || attempt == retryPolicy.getMaxRetryCount()) {
                    return rethrow(e);
                }

                logRetry(attempt + 1, decision);
            }

            waitBeforeRetry();
        }

        throw new IllegalStateException("GET retry flow ended without a response.");
    }

    private Response executeRequest(RequestSpecification spec, String method, String endpoint) {

        try {

            return spec.when().request(method, endpoint);

        } catch (Exception e) {

            if (!(e instanceof HttpResponseException)) {
                return rethrow(e);
            }

            return buildResponse((HttpResponseException) e);
        }
    }

    private Response buildResponse(HttpResponseException exception) {

        HttpResponseDecorator response = exception.getResponse();
        ResponseBuilder builder = new ResponseBuilder()
                .setStatusCode(response.getStatusLine().getStatusCode())
                .setStatusLine(response.getStatusLine().toString());

        String contentType = response.getContentType();
        if (contentType != null) {
            builder.setContentType(contentType);
        }

        List<Header> responseHeaders = java.util.Arrays.stream(response.getAllHeaders())
                .map(header -> new Header(header.getName(), header.getValue()))
                .toList();
        builder.setHeaders(new Headers(responseHeaders));

        HttpEntity entity = response.getEntity();
        if (entity != null) {
            try {
                builder.setBody(EntityUtils.toString(entity, StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw new RuntimeException("Failed to read error response body", e);
            }
        } else {
            builder.setBody("");
        }

        return builder.build();
    }

    private void logRetry(int retryAttempt, RetryDecision decision) {

        ConsoleLogger.info("Retry Attempt", retryAttempt + "/" + retryPolicy.getMaxRetryCount());
        ConsoleLogger.info("Retry Reason", decision.getReason());
        ConsoleLogger.info("Retryable", decision.isRetryable());
    }

    private void waitBeforeRetry() {

        try {
            Thread.sleep(retryPolicy.getRetryDelayMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("GET retry interrupted while waiting for next attempt.", e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable, R> R rethrow(Throwable throwable) throws T {

        throw (T) throwable;
    }
}
