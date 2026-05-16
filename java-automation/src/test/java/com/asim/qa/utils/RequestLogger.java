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

        ConsoleLogger.section(title);

        ConsoleLogger.info("Endpoint", endpoint);

        ConsoleLogger.info("Headers", headers);

        ConsoleLogger.info("Status Code", response.statusCode());

        ConsoleLogger.info("Response Time", response.time() + " ms");

        ConsoleLogger.line();

        ConsoleLogger.body(response.asPrettyString());

        ConsoleLogger.line();
    }
}
