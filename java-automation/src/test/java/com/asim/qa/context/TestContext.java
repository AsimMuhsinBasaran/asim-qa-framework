package com.asim.qa.context;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import com.asim.qa.utils.ScenarioContextUtils;

public class TestContext {

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
            this.headers = new LinkedHashMap<>(headers);
            this.pathParams = new LinkedHashMap<>(pathParams);
            this.queryParams = new LinkedHashMap<>(queryParams);
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
            return new LinkedHashMap<>(headers);
        }

        public Map<String, String> getPathParams() {
            return new LinkedHashMap<>(pathParams);
        }

        public Map<String, String> getQueryParams() {
            return new LinkedHashMap<>(queryParams);
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

    public boolean contains(String key) {

        return scenarioVariables.containsKey(key);
    }

    public Map<String, String> getScenarioVariablesSnapshot() {
        return new LinkedHashMap<>(scenarioVariables);
    }

    public String requireScenarioVariable(String key) {
        return ScenarioContextUtils.requireVariable(this, key);
    }

    public String resolve(String value) {
        return ScenarioContextUtils.resolveText(this, value);
    }

    public void clearScenarioVariables() {
        scenarioVariables.clear();
    }
}
