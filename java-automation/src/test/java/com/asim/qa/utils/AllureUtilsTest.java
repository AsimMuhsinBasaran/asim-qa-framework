package com.asim.qa.utils;

import io.restassured.builder.ResponseBuilder;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

public class AllureUtilsTest {

    @Test
    public void should_format_api_request_with_standard_sections() {

        String diagnostic = AllureUtils.formatApiRequest(
                "POST",
                "/orders/{orderId}",
                Map.of("Authorization", "Bearer secret-token"),
                Map.of("orderId", "123"),
                Map.of("expand", "items"),
                "{\"password\":\"secret-password\",\"token\":\"abc\"}"
        );

        Assert.assertTrue(diagnostic.contains("Request"));
        Assert.assertTrue(diagnostic.contains("Method: POST"));
        Assert.assertTrue(diagnostic.contains("Endpoint: /orders/{orderId}"));
        Assert.assertTrue(diagnostic.contains("Headers:"));
        Assert.assertTrue(diagnostic.contains("Authorization=Bearer secret-token"));
        Assert.assertTrue(diagnostic.contains("Path Params:"));
        Assert.assertTrue(diagnostic.contains("orderId=123"));
        Assert.assertTrue(diagnostic.contains("Query Params:"));
        Assert.assertTrue(diagnostic.contains("expand=items"));
        Assert.assertTrue(diagnostic.contains("Body:"));
    }

    @Test
    public void should_mask_sensitive_values_after_formatting_api_request() {

        String diagnostic = AllureUtils.formatApiRequest(
                "POST",
                "/login",
                Map.of("Authorization", "Bearer secret-token"),
                Map.of(),
                Map.of(),
                "{\"password\":\"secret-password\",\"token\":\"abc\"}"
        );

        String maskedDiagnostic = SensitiveDataMasker.mask(diagnostic);

        Assert.assertFalse(maskedDiagnostic.contains("secret-token"));
        Assert.assertFalse(maskedDiagnostic.contains("secret-password"));
        Assert.assertFalse(maskedDiagnostic.contains("\"abc\""));
        Assert.assertTrue(maskedDiagnostic.contains("***MASKED***"));
    }

    @Test
    public void should_format_api_response_with_status_headers_and_body() {

        Response response = new ResponseBuilder()
                .setStatusCode(201)
                .setHeaders(new Headers(new Header("Content-Type", "application/json")))
                .setBody("{\"id\":101}")
                .build();

        String diagnostic = AllureUtils.formatApiResponse(response);

        Assert.assertTrue(diagnostic.contains("Response"));
        Assert.assertTrue(diagnostic.contains("Status Code: 201"));
        Assert.assertTrue(diagnostic.contains("Headers:"));
        Assert.assertTrue(diagnostic.contains("Content-Type: application/json"));
        Assert.assertTrue(diagnostic.contains("Body:"));
        Assert.assertTrue(diagnostic.contains("\"id\""));
    }
}
