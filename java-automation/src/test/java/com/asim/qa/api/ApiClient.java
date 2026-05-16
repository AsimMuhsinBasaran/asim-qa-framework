package com.asim.qa.api;

import com.asim.qa.config.ConfigReader;
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
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static io.restassured.config.HttpClientConfig.httpClientConfig;
import static io.restassured.config.RestAssuredConfig.config;

public class ApiClient {

    private final Map<String, String> headers = new HashMap<>();
    private final Map<String, String> pathParams = new LinkedHashMap<>();
    private final Map<String, String> queryParams = new LinkedHashMap<>();

    public Map<String, String> getHeaders() {

        return headers;
    }

    public Map<String, String> getHeadersSnapshot() {

        return new HashMap<>(headers);
    }

    public Map<String, String> getPathParamsSnapshot() {

        return new LinkedHashMap<>(pathParams);
    }

    public Map<String, String> getQueryParamsSnapshot() {

        return new LinkedHashMap<>(queryParams);
    }

    public ApiClient() {

        headers.put("Content-Type", "application/json");
    }

    public void addHeader(String key, String value) {

        headers.put(key, value);
    }

    public void addPathParam(String key, String value) {

        pathParams.put(key, value);
    }

    public void addQueryParam(String key, String value) {

        queryParams.put(key, value);
    }

    public boolean hasPathParam(String key) {

        return pathParams.containsKey(key);
    }

    public void clearHeaders() {

        headers.clear();

        headers.put("Content-Type", "application/json");
    }

    public void clearParams() {

        pathParams.clear();
        queryParams.clear();
    }

    private RequestSpecification requestSpec() {

        int timeoutMs = ConfigReader.getApiTimeoutMs();

        return given()
                .config(config().httpClient(
                        httpClientConfig()
                                .setParam("http.connection.timeout", timeoutMs)
                                .setParam("http.socket.timeout", timeoutMs)
                                .setParam("http.connection-manager.timeout", (long) timeoutMs)
                ))
                .headers(headers);
    }

    public Response get(String endpoint) {

        return sendRequest("GET", endpoint, null);
    }

    public Response post(String endpoint, String body) {

        return sendRequest("POST", endpoint, body);
    }

    public Response sendRequest(String method, String endpoint, String body) {

        RequestSpecification spec = requestSpec();

        if (!pathParams.isEmpty()) {
            spec.pathParams(pathParams);
        }

        if (!queryParams.isEmpty()) {
            spec.queryParams(queryParams);
        }

        if (body != null && !body.isBlank()) {
            spec.body(body);
        }

        String normalizedMethod = method.toUpperCase();

        try {
            return switch (normalizedMethod) {
                case "GET", "POST", "PUT", "PATCH", "DELETE" -> executeRequest(spec, normalizedMethod, endpoint);
                default -> throw new IllegalArgumentException("Unsupported HTTP method: " + method);
            };
        } finally {
            clearParams();
        }
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

    @SuppressWarnings("unchecked")
    private static <T extends Throwable, R> R rethrow(Throwable throwable) throws T {

        throw (T) throwable;
    }
}
