package com.asim.qa.runtime;

import java.util.LinkedHashMap;
import java.util.Map;

public final class RuntimeContext {

    private final String runId;
    private final String env;
    private final String scenarioName;
    private final String requestId;
    private final String exportedAt;
    private final String layer;
    private final String source;
    private final Map<String, String> exports;

    public RuntimeContext(String runId,
                          String env,
                          String scenarioName,
                          String requestId,
                          String exportedAt,
                          String layer,
                          String source,
                          Map<String, String> exports) {

        this.runId = runId;
        this.env = env;
        this.scenarioName = scenarioName;
        this.requestId = requestId;
        this.exportedAt = exportedAt;
        this.layer = layer;
        this.source = source;
        this.exports = new LinkedHashMap<>(exports == null ? Map.of() : exports);
    }

    public String getRunId() {
        return runId;
    }

    public String getEnv() {
        return env;
    }

    public String getScenarioName() {
        return scenarioName;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getExportedAt() {
        return exportedAt;
    }

    public String getLayer() {
        return layer;
    }

    public String getSource() {
        return source;
    }

    public Map<String, String> getExports() {
        return new LinkedHashMap<>(exports);
    }

    public RuntimeContext withExports(Map<String, String> exports) {
        return new RuntimeContext(
                runId,
                env,
                scenarioName,
                requestId,
                exportedAt,
                layer,
                source,
                exports
        );
    }
}
