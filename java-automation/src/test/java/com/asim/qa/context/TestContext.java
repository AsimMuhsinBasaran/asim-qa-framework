package com.asim.qa.context;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestContext {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{([^{}]+)}");

    private Response response;
    private JsonPath jsonPath;
    private String requestBody;
    private LastRequest lastRequest;
    private final Map<String, String> scenarioVariables = new HashMap<>();

    public static class LastRequest {

        private final String method;
        private final String endpoint;
        private final String body;
        private final Map<String, String> headers;
        private final Map<String, String> pathParams;
        private final Map<String, String> queryParams;

        public LastRequest(String method,
                           String endpoint,
                           String body,
                           Map<String, String> headers,
                           Map<String, String> pathParams,
                           Map<String, String> queryParams) {
            this.method = method;
            this.endpoint = endpoint;
            this.body = body;
            this.headers = new HashMap<>(headers);
            this.pathParams = new HashMap<>(pathParams);
            this.queryParams = new HashMap<>(queryParams);
        }

        public String getMethod() {
            return method;
        }

        public String getEndpoint() {
            return endpoint;
        }

        public String getBody() {
            return body;
        }

        public Map<String, String> getHeaders() {
            return new HashMap<>(headers);
        }

        public Map<String, String> getPathParams() {
            return new HashMap<>(pathParams);
        }

        public Map<String, String> getQueryParams() {
            return new HashMap<>(queryParams);
        }
    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    public JsonPath getJsonPath() {
        return jsonPath;
    }

    public void setJsonPath(JsonPath jsonPath) {
        this.jsonPath = jsonPath;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }

    public LastRequest getLastRequest() {
        return lastRequest;
    }

    public void setLastRequest(LastRequest lastRequest) {
        this.lastRequest = lastRequest;
    }

    public void saveScenarioVariable(String key, String value) {
        scenarioVariables.put(key, value);
    }

    public String getScenarioVariable(String key) {
        return scenarioVariables.get(key);
    }

    public String requireScenarioVariable(String key) {
        String value = scenarioVariables.get(key);

        if (value == null) {
            throw new RuntimeException("Scenario variable not found: " + key);
        }

        return value;
    }

    public String resolve(String value) {
        if (value == null || !value.contains("{")) return value;

        Matcher matcher = PLACEHOLDER_PATTERN.matcher(value);
        StringBuffer resolved = new StringBuffer();

        while (matcher.find()) {
            String variableName = matcher.group(1);
            String variableValue = requireScenarioVariable(variableName);

            matcher.appendReplacement(resolved, Matcher.quoteReplacement(variableValue));
        }

        matcher.appendTail(resolved);

        return resolved.toString();
    }

    public void clearScenarioVariables() {
        scenarioVariables.clear();
    }
}
