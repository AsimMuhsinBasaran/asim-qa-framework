package com.asim.qa.context;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

public class TestContext {

    private Response response;
    private JsonPath jsonPath;
    private String requestBody;

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
}