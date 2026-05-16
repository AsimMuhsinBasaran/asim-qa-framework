package com.asim.qa.api;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class ApiClient {

    private final Map<String, String> headers = new HashMap<>();

    public Map<String, String> getHeaders() {

        return headers;
    }

    public ApiClient() {

        headers.put("Content-Type", "application/json");
    }

    public void addHeader(String key, String value) {

        headers.put(key, value);
    }

    public void clearHeaders() {

        headers.clear();

        headers.put("Content-Type", "application/json");
    }

    private RequestSpecification requestSpec() {

        return given()
                .headers(headers);
    }

    public Response get(String endpoint) {

        return requestSpec()
                .when()
                .get(endpoint);
    }

    public Response post(String endpoint, String body) {

        return requestSpec()
                .body(body)
                .when()
                .post(endpoint);
    }

    public Response sendRequest(String method, String endpoint, String body) {

        RequestSpecification spec = requestSpec();

        if (body != null && !body.isBlank()) {
            spec.body(body);
        }

        return switch (method.toUpperCase()) {
            case "GET" -> spec.when().get(endpoint);
            case "POST" -> spec.when().post(endpoint);
            case "PUT" -> spec.when().put(endpoint);
            case "PATCH" -> spec.when().patch(endpoint);
            case "DELETE" -> spec.when().delete(endpoint);
            default -> throw new IllegalArgumentException("Unsupported HTTP method: " + method);
        };
    }
}