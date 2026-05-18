package com.asim.qa.runtime;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class RuntimeContextWriterTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void should_write_allowed_exports_to_valid_json_file() throws IOException {

        Path tempDir = Files.createTempDirectory("runtime-context-writer-test");
        RuntimeContextWriter writer = new RuntimeContextWriter(tempDir);
        RuntimeContext context = baseContext();
        Map<String, String> exports = new LinkedHashMap<>();
        exports.put("userId", "1001");
        exports.put("orderId", "501");

        Path writtenFile = writer.write(context, exports);

        Assert.assertTrue(Files.exists(writtenFile));
        Assert.assertTrue(writtenFile.toString().contains("user-places-an-order-successfully"));
        Assert.assertTrue(writtenFile.toString().contains("REQ-000028"));

        JsonNode root = objectMapper.readTree(Files.readString(writtenFile));
        Assert.assertEquals(root.get("runId").asText(), "20260518-173200-acde12");
        Assert.assertEquals(root.get("env").asText(), "mock");
        Assert.assertEquals(root.get("scenarioName").asText(), "User places an order successfully");
        Assert.assertEquals(root.get("requestId").asText(), "REQ-000028");
        Assert.assertEquals(root.get("exports").get("userId").asText(), "1001");
        Assert.assertEquals(root.get("exports").get("orderId").asText(), "501");
        Assert.assertEquals(root.get("meta").get("layer").asText(), "api");
        Assert.assertEquals(root.get("meta").get("source").asText(), "java-automation");
    }

    @Test
    public void should_reject_disallowed_export_key() throws IOException {

        Path tempDir = Files.createTempDirectory("runtime-context-writer-test");
        RuntimeContextWriter writer = new RuntimeContextWriter(tempDir);

        RuntimeException exception = Assert.expectThrows(RuntimeException.class, () ->
                writer.write(baseContext(), Map.of("statusCode", "200"))
        );

        Assert.assertTrue(exception.getMessage().contains("Export key is not allowed"));
    }

    @Test
    public void should_reject_sensitive_export_key() throws IOException {

        Path tempDir = Files.createTempDirectory("runtime-context-writer-test");
        RuntimeContextWriter writer = new RuntimeContextWriter(tempDir);

        RuntimeException exception = Assert.expectThrows(RuntimeException.class, () ->
                writer.write(baseContext(), Map.of("token", "abc123"))
        );

        Assert.assertTrue(exception.getMessage().contains("Sensitive export key is not allowed"));
    }

    @Test
    public void should_reject_sensitive_looking_export_value() throws IOException {

        Path tempDir = Files.createTempDirectory("runtime-context-writer-test");
        RuntimeContextWriter writer = new RuntimeContextWriter(tempDir);

        RuntimeException exception = Assert.expectThrows(RuntimeException.class, () ->
                writer.write(baseContext(), Map.of("userId", "Bearer abc123xyz"))
        );

        Assert.assertTrue(exception.getMessage().contains("Sensitive-looking export value is not allowed"));
    }

    @Test
    public void should_reject_null_or_blank_export_key() throws IOException {

        Path tempDir = Files.createTempDirectory("runtime-context-writer-test");
        RuntimeContextWriter writer = new RuntimeContextWriter(tempDir);

        RuntimeException nullKeyException = Assert.expectThrows(RuntimeException.class, () -> {
            Map<String, String> exports = new LinkedHashMap<>();
            exports.put(null, "1001");
            writer.write(baseContext(), exports);
        });

        Assert.assertTrue(nullKeyException.getMessage().contains("export key must not be null or blank"));

        RuntimeException blankKeyException = Assert.expectThrows(RuntimeException.class, () -> {
            Map<String, String> exports = new LinkedHashMap<>();
            exports.put("   ", "1001");
            writer.write(baseContext(), exports);
        });

        Assert.assertTrue(blankKeyException.getMessage().contains("export key must not be null or blank"));
    }

    @Test
    public void should_reject_null_value() throws IOException {

        Path tempDir = Files.createTempDirectory("runtime-context-writer-test");
        RuntimeContextWriter writer = new RuntimeContextWriter(tempDir);

        RuntimeException exception = Assert.expectThrows(RuntimeException.class, () -> {
            Map<String, String> exports = new LinkedHashMap<>();
            exports.put("userId", null);
            writer.write(baseContext(), exports);
        });

        Assert.assertTrue(exception.getMessage().contains("Export value must not be null or blank"));
    }

    @Test
    public void should_prevent_overwrite_when_file_already_exists() throws IOException {

        Path tempDir = Files.createTempDirectory("runtime-context-writer-test");
        RuntimeContextWriter writer = new RuntimeContextWriter(tempDir);
        RuntimeContext context = baseContext();
        Map<String, String> exports = Map.of("userId", "1001");

        Path writtenFile = writer.write(context, exports);

        RuntimeException exception = Assert.expectThrows(RuntimeException.class, () -> writer.write(context, exports));

        Assert.assertTrue(exception.getMessage().contains("Runtime context already exists"));
        Assert.assertTrue(Files.exists(writtenFile));
    }

    private RuntimeContext baseContext() {

        return new RuntimeContext(
                "20260518-173200-acde12",
                "mock",
                "User places an order successfully",
                "REQ-000028",
                "2026-05-18T17:32:00+03:00",
                "api",
                "java-automation",
                Map.of()
        );
    }
}
