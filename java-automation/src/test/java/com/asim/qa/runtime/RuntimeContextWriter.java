package com.asim.qa.runtime;

import com.asim.qa.utils.SensitiveDataMasker;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class RuntimeContextWriter {

    private static final Set<String> DEFAULT_ALLOWED_EXPORT_KEYS = Set.of(
            "userId",
            "orderId",
            "productId",
            "email",
            "customerId",
            "accountId",
            "resourceId",
            "referenceId",
            "sessionId",
            "status"
    );

    private static final Pattern INVALID_FILE_CHARS = Pattern.compile("[^a-z0-9-]+");
    private static final Pattern DUPLICATE_DASHES = Pattern.compile("-{2,}");

    private final Path rootDirectory;
    private final ObjectMapper objectMapper;
    private final Set<String> allowedExportKeys;

    public RuntimeContextWriter() {
        this(Path.of("test-data/runtime"));
    }

    public RuntimeContextWriter(Path rootDirectory) {
        this(rootDirectory, DEFAULT_ALLOWED_EXPORT_KEYS);
    }

    public RuntimeContextWriter(Path rootDirectory, Set<String> allowedExportKeys) {
        this.rootDirectory = rootDirectory;
        this.allowedExportKeys = allowedExportKeys;
        this.objectMapper = new ObjectMapper();
    }

    public Path write(RuntimeContext context, Map<String, String> requestedExports) {

        validateContext(context);

        Map<String, String> sanitizedExports = validateAndFilterExports(requestedExports);
        RuntimeContext runtimeContext = context.withExports(sanitizedExports);

        Path runDirectory = rootDirectory.resolve(runtimeContext.getRunId());
        Path targetFile = runDirectory.resolve(buildFileName(runtimeContext));

        try {
            Files.createDirectories(runDirectory);
            if (Files.exists(targetFile)) {
                throw new RuntimeException("Runtime context already exists: " + targetFile);
            }

            Files.writeString(targetFile, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(toJson(runtimeContext)),
                    StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Runtime context could not be written: " + targetFile, e);
        }

        return targetFile;
    }

    private Map<String, String> validateAndFilterExports(Map<String, String> requestedExports) {

        Map<String, String> sanitizedExports = new LinkedHashMap<>();

        if (requestedExports == null || requestedExports.isEmpty()) {
            return sanitizedExports;
        }

        for (Map.Entry<String, String> entry : requestedExports.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            validateKeyAndValue(key, value);

            if (SensitiveDataMasker.isSensitiveKey(key)) {
                throw new RuntimeException("Sensitive export key is not allowed: " + key);
            }

            if (!isAllowedExportKey(key)) {
                throw new RuntimeException("Export key is not allowed: " + key);
            }

            String maskedValue = SensitiveDataMasker.mask(key, value);
            if (maskedValue == null || !maskedValue.equals(value)) {
                throw new RuntimeException("Sensitive-looking export value is not allowed for key: " + key);
            }

            sanitizedExports.put(key, value);
        }

        return sanitizedExports;
    }

    private void validateContext(RuntimeContext context) {

        if (context == null) {
            throw new RuntimeException("RuntimeContext must not be null.");
        }

        requireText(context.getRunId(), "runId");
        requireText(context.getEnv(), "env");
        requireText(context.getScenarioName(), "scenarioName");
        requireText(context.getRequestId(), "requestId");
        requireText(context.getExportedAt(), "exportedAt");
        requireText(context.getLayer(), "layer");
        requireText(context.getSource(), "source");
    }

    private void validateKeyAndValue(String key, String value) {

        requireText(key, "export key");

        if (value == null || value.isBlank()) {
            throw new RuntimeException("Export value must not be null or blank for key: " + key);
        }
    }

    private boolean isAllowedExportKey(String key) {

        String normalizedKey = key.trim().toLowerCase(Locale.ROOT);
        return allowedExportKeys.stream()
                .map(value -> value.toLowerCase(Locale.ROOT))
                .anyMatch(normalizedKey::equals);
    }

    private ObjectNode toJson(RuntimeContext runtimeContext) {

        ObjectNode root = objectMapper.createObjectNode();
        root.put("runId", runtimeContext.getRunId());
        root.put("env", runtimeContext.getEnv());
        root.put("scenarioName", runtimeContext.getScenarioName());
        root.put("requestId", runtimeContext.getRequestId());
        root.put("exportedAt", runtimeContext.getExportedAt());

        ObjectNode exportsNode = root.putObject("exports");
        runtimeContext.getExports().forEach(exportsNode::put);

        ObjectNode metaNode = root.putObject("meta");
        metaNode.put("layer", runtimeContext.getLayer());
        metaNode.put("source", runtimeContext.getSource());

        return root;
    }

    private String buildFileName(RuntimeContext context) {

        return toScenarioSlug(context.getScenarioName()) + "-" + context.getRequestId() + ".json";
    }

    private String toScenarioSlug(String scenarioName) {

        String slug = scenarioName.toLowerCase(Locale.ROOT).trim();
        slug = INVALID_FILE_CHARS.matcher(slug).replaceAll("-");
        slug = DUPLICATE_DASHES.matcher(slug).replaceAll("-");
        slug = slug.replaceAll("^-+", "");
        slug = slug.replaceAll("-+$", "");

        if (slug.isBlank()) {
            return "scenario";
        }

        return slug;
    }

    private void requireText(String value, String fieldName) {

        if (value == null || value.isBlank()) {
            throw new RuntimeException(fieldName + " must not be null or blank.");
        }
    }
}
