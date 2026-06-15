package com.asim.qa.runtime.bridge;

import com.asim.qa.runtime.RuntimeContext;
import com.asim.qa.runtime.RuntimeContextWriter;
import com.asim.qa.utils.ConsoleLogger;
import com.asim.qa.utils.JsonUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import io.qameta.allure.Allure;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static io.restassured.RestAssured.given;

@Epic("Runtime Bridge")
@Feature("API Runtime Context Export")
public class ApiRuntimeBridgeExportTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Path RUNTIME_ROOT_DIRECTORY = Path.of("target", "runtime-test").normalize();
    private static final String RUN_ID = "api-runtime-bridge";
    private static final String REQUEST_ID = "REQ-API-BRIDGE-001";
    private static final String SCENARIO_NAME = "API runtime bridge export from order creation";
    private static final Path EXPECTED_FILE = RUNTIME_ROOT_DIRECTORY
            .resolve(RUN_ID)
            .resolve("api-runtime-bridge-export-from-order-creation-REQ-API-BRIDGE-001.json");

    @BeforeMethod
    public void cleanRuntimeBridgeOutput() throws IOException {

        deleteRecursively(RUNTIME_ROOT_DIRECTORY.resolve(RUN_ID));
    }

    @Test
    @Story("Export API response fields for Cypress consumption")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verifies the Java-side runtime export contract for Cypress consumption. This is not a full Cypress E2E validation.")
    public void should_export_runtime_context_from_actual_mock_api_response() throws IOException {

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

            String artifactPath = toRepoRelativePath(writtenFile);
            Allure.addAttachment("Runtime bridge export summary", "text/plain", buildExportSummary(runtimeContext, artifactPath, exports));
            ConsoleLogger.info("Runtime Bridge Export", "artifact=" + artifactPath + ", exportedKeys=" + exports.keySet());
        } finally {
            server.stop();
        }
    }

    private static String buildExportSummary(RuntimeContext runtimeContext,
                                             String artifactPath,
                                             Map<String, String> exports) {

        return "scenarioName=" + runtimeContext.getScenarioName() + System.lineSeparator() +
                "runId=" + runtimeContext.getRunId() + System.lineSeparator() +
                "requestId=" + runtimeContext.getRequestId() + System.lineSeparator() +
                "env=" + runtimeContext.getEnv() + System.lineSeparator() +
                "layer=" + runtimeContext.getLayer() + System.lineSeparator() +
                "source=" + runtimeContext.getSource() + System.lineSeparator() +
                "artifactPath=" + artifactPath + System.lineSeparator() +
                "exportedKeys=" + exports.keySet() + System.lineSeparator() +
                "exportedValueCount=" + exports.size();
    }

    private static String toRepoRelativePath(Path writtenFile) {

        Path normalizedPath = writtenFile.normalize();
        if (normalizedPath.getNameCount() > 1 && "..".equals(normalizedPath.getName(0).toString())) {
            return normalizedPath.subpath(1, normalizedPath.getNameCount()).toString();
        }

        return normalizedPath.toString();
    }

    private static void deleteRecursively(Path path) throws IOException {

        if (!Files.exists(path)) {
            return;
        }

        try (var paths = Files.walk(path)) {
            paths.sorted(Comparator.reverseOrder())
                    .forEach(ApiRuntimeBridgeExportTest::deletePath);
        }
    }

    private static void deletePath(Path path) {

        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new RuntimeException("Runtime bridge test artifact could not be deleted: " + path, e);
        }
    }
}
