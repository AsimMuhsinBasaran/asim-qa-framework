package com.asim.qa.auth;

import com.asim.qa.context.TestContext;
import com.asim.qa.utils.AllureUtils;
import com.asim.qa.utils.ConsoleLogger;
import com.asim.qa.utils.ScenarioContextUtils;
import io.restassured.specification.RequestSpecification;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

public class AuthManager {

    private static final String AUTHORIZATION_HEADER = "Authorization";

    private AuthType authType = AuthType.NONE;
    private String bearerToken;
    private String basicUsername;
    private String basicPassword;
    private String apiKeyHeaderName;
    private String apiKeyValue;

    public synchronized void clear() {

        authType = AuthType.NONE;
        bearerToken = null;
        basicUsername = null;
        basicPassword = null;
        apiKeyHeaderName = null;
        apiKeyValue = null;
    }

    public synchronized void useBearerAuth(String token) {

        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Bearer token must not be blank.");
        }

        clear();
        authType = AuthType.BEARER;
        bearerToken = token.trim();
        logConfigured();
    }

    public synchronized void useBearerAuth(TestContext context, String token) {

        useBearerAuth(ScenarioContextUtils.resolveText(context, token));
    }

    public synchronized void useBasicAuth(String username, String password) {

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Basic auth username must not be blank.");
        }

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Basic auth password must not be blank.");
        }

        clear();
        authType = AuthType.BASIC;
        basicUsername = username.trim();
        basicPassword = password;
        logConfigured();
    }

    public synchronized void useBasicAuth(TestContext context, String username, String password) {

        useBasicAuth(
                ScenarioContextUtils.resolveText(context, username),
                ScenarioContextUtils.resolveText(context, password)
        );
    }

    public synchronized void useApiKeyAuth(String headerName, String apiKey) {

        if (headerName == null || headerName.isBlank()) {
            throw new IllegalArgumentException("API key header name must not be blank.");
        }

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException("API key must not be blank.");
        }

        clear();
        authType = AuthType.API_KEY;
        apiKeyHeaderName = headerName.trim();
        apiKeyValue = apiKey.trim();
        logConfigured();
    }

    public synchronized void useApiKeyAuth(TestContext context, String headerName, String apiKey) {

        useApiKeyAuth(
                ScenarioContextUtils.resolveText(context, headerName),
                ScenarioContextUtils.resolveText(context, apiKey)
        );
    }

    public synchronized RequestSpecification applyAuth(RequestSpecification requestSpec) {

        Map<String, String> authHeaders = buildAuthHeaders();
        if (authHeaders.isEmpty()) {
            return requestSpec;
        }

        return requestSpec.headers(authHeaders);
    }

    public synchronized RequestSpecification applyBearerAuth(RequestSpecification requestSpec, String token) {

        useBearerAuth(token);
        return applyAuth(requestSpec);
    }

    public synchronized RequestSpecification applyBasicAuth(RequestSpecification requestSpec,
                                                            String username,
                                                            String password) {

        useBasicAuth(username, password);
        return applyAuth(requestSpec);
    }

    public synchronized RequestSpecification applyApiKeyAuth(RequestSpecification requestSpec,
                                                             String headerName,
                                                             String apiKey) {

        useApiKeyAuth(headerName, apiKey);
        return applyAuth(requestSpec);
    }

    public synchronized Map<String, String> applyToHeaders(Map<String, String> requestHeaders) {

        Map<String, String> effectiveHeaders = new LinkedHashMap<>();
        if (requestHeaders != null) {
            effectiveHeaders.putAll(requestHeaders);
        }

        Map<String, String> authHeaders = buildAuthHeaders();

        if (authHeaders.isEmpty()) {
            return effectiveHeaders;
        }

        if (authType == AuthType.BEARER || authType == AuthType.BASIC) {
            effectiveHeaders.remove(AUTHORIZATION_HEADER);
        } else if (authType == AuthType.API_KEY && apiKeyHeaderName != null) {
            effectiveHeaders.remove(apiKeyHeaderName);
        }

        effectiveHeaders.putAll(authHeaders);
        return effectiveHeaders;
    }

    public synchronized String describeAuth() {

        return switch (authType) {
            case NONE -> "NONE";
            case BEARER -> "BEARER token=***MASKED***";
            case BASIC -> "BASIC username=***MASKED*** password=***MASKED***";
            case API_KEY -> "API_KEY header=" + apiKeyHeaderName +
                    " value=***MASKED***";
        };
    }

    private Map<String, String> buildAuthHeaders() {

        Map<String, String> authHeaders = new LinkedHashMap<>();

        switch (authType) {
            case NONE -> {
                return authHeaders;
            }
            case BEARER -> authHeaders.put(AUTHORIZATION_HEADER, "Bearer " + bearerToken);
            case BASIC -> authHeaders.put(AUTHORIZATION_HEADER, "Basic " + encodeBasicCredentials());
            case API_KEY -> authHeaders.put(apiKeyHeaderName, apiKeyValue);
        }

        return authHeaders;
    }

    private String encodeBasicCredentials() {

        String credentials = basicUsername + ":" + basicPassword;
        return Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }

    private void logConfigured() {

        String summary = describeAuth();
        ConsoleLogger.info("Auth Configured", summary);
        AllureUtils.attachRequest("Auth Configured", summary);
    }
}
