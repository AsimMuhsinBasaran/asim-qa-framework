package com.asim.qa.runtime.bridge;

import com.asim.qa.runtime.RuntimeContext;
import com.asim.qa.runtime.RuntimeContextWriter;
import com.asim.qa.utils.JsonUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static io.restassured.RestAssured.given;

public class ApiRuntimeBridgeExportTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Path RUNTIME_ROOT_DIRECTORY = Path.of("..", "test-data", "runtime").normalize();
    private static final String RUN_ID = "api-runtime-bridge";
    private static final String REQUEST_ID = "REQ-API-BRIDGE-001";
    private static final String SCENARIO_NAME = "API runtime bridge export from order creation";
    private static final Path EXPECTED_FILE = RUNTIME_ROOT_DIRECTORY
            .resolve(RUN_ID)
            .resolve("api-runtime-bridge-export-from-order-creation-REQ-API-BRIDGE-001.json");

    @Test
    public void should_export_runtime_context_from_actual_mock_api_response() throws IOException {

        Files.deleteIfExists(EXPECTED_FILE);

        WireMockServer server = new WireMockServer(
                options()
                        .dynamicPort()
                        .usingFilesUnderDirectory("../mock-server")
        );

        server.start();
        try {
            Response response = given()
                    .baseUri(server.baseUrl())
                    .contentType(ContentType.JSON)
                    .accept(ContentType.JSON)
                    .header("Authorization", "Bearer valid-test-token")
                    .body(JsonUtils.readJson("order/create-order.json"))
                    .when()
                    .post("/orders")
                    .then()
                    .statusCode(201)
                    .extract()
                    .response();

            String orderId = response.jsonPath().getString("orderId");
            String userId = response.jsonPath().getString("userId");
            String status = response.jsonPath().getString("status");

            Assert.assertEquals(orderId, "501");
            Assert.assertEquals(userId, "1001");
            Assert.assertEquals(status, "PENDING");

            RuntimeContextWriter writer = new RuntimeContextWriter(RUNTIME_ROOT_DIRECTORY);
            RuntimeContext runtimeContext = new RuntimeContext(
                    RUN_ID,
                    "mock",
                    SCENARIO_NAME,
                    REQUEST_ID,
                    Instant.now().toString(),
                    "api",
                    "java-automation",
                    Map.of()
            );

            Map<String, String> exports = new LinkedHashMap<>();
            exports.put("userId", userId);
            exports.put("orderId", orderId);
            exports.put("status", status);

            Path writtenFile = writer.write(runtimeContext, exports);

            Assert.assertEquals(writtenFile, EXPECTED_FILE);
            Assert.assertTrue(Files.exists(writtenFile));

            JsonNode root = OBJECT_MAPPER.readTree(Files.readString(writtenFile));
            Assert.assertEquals(root.get("runId").asText(), RUN_ID);
            Assert.assertEquals(root.get("requestId").asText(), REQUEST_ID);
            Assert.assertEquals(root.get("scenarioName").asText(), SCENARIO_NAME);
            Assert.assertEquals(root.get("exports").get("userId").asText(), "1001");
            Assert.assertEquals(root.get("exports").get("orderId").asText(), "501");
            Assert.assertEquals(root.get("exports").get("status").asText(), "PENDING");

            System.out.println("Runtime context written to: " + writtenFile.toAbsolutePath());
            System.out.println("Extracted response fields: userId=" + userId + ", orderId=" + orderId + ", status=" + status);
        } finally {
            server.stop();
        }
    }
}
