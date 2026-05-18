package com.asim.qa.utils;

import io.restassured.response.Response;

import java.util.Map;

public class RequestLogger {

    public static void logRequest(
            String title,
            String endpoint,
            Map<String, String> headers,
            Response response
    ) {

        ConsoleLogger.block(buildBlock(title, endpoint, headers, response));
    }

    private static String buildBlock(
            String title,
            String endpoint,
            Map<String, String> headers,
            Response response
    ) {

        StringBuilder builder = new StringBuilder();
        builder.append("\n");
        builder.append("==================================================\n");
        builder.append("🚀 ").append(title).append("\n");
        builder.append("==================================================\n");
        builder.append(String.format("🔹 %-20s : %s%n", "Request ID", requestIdOrNA()));
        builder.append(String.format("🔹 %-20s : %s%n", "Endpoint", endpoint));
        builder.append(String.format("🔹 %-20s : %s%n", "Headers", maskedHeaders(headers)));
        builder.append(String.format("🔹 %-20s : %s%n", "Status Code", response.statusCode()));
        builder.append(String.format("🔹 %-20s : %s%n", "Response Time", normalizedResponseTime(response)));
        builder.append("--------------------------------------------------\n");
        builder.append(maskedBody(response));
        builder.append("\n");
        builder.append("--------------------------------------------------");
        return builder.toString();
    }

    private static String requestIdOrNA() {

        String requestId = RequestCorrelationContext.getRequestId();
        return requestId == null || requestId.isBlank() ? "n/a" : requestId;
    }

    private static String normalizedResponseTime(Response response) {

        long responseTime = response.time();
        if (responseTime < 0) {
            return "N/A";
        }

        return responseTime + " ms";
    }

    private static String maskedBody(Response response) {

        String maskedBody = SensitiveDataMasker.mask(response.asPrettyString());
        return maskedBody == null || maskedBody.isBlank() ? "[no body]" : maskedBody;
    }

    private static String maskedHeaders(Map<String, String> headers) {

        return SensitiveDataMasker.mask(headers);
    }
}
