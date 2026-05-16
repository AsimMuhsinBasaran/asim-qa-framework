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
    private final Map<String, String> scenarioVariables = new HashMap<>();

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
