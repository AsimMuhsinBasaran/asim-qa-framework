package com.asim.qa.stepdefinitions;

import com.asim.qa.api.ApiClient;
import com.asim.qa.config.ConfigReader;
import com.asim.qa.utils.*;
import io.cucumber.java.Before;
import io.cucumber.java.en.*;
import io.restassured.RestAssured;
import com.asim.qa.context.TestContext;
import io.restassured.path.json.JsonPath;

import java.util.List;

public class ApiSteps {

    String baseUrl;
    private final TestContext context;

    public ApiSteps(TestContext context) {
        this.context = context;
    }
    ApiClient apiClient = new ApiClient();

    @Before
    public void reset_request_state() {

        apiClient.clearHeaders();
        context.setRequestBody(null);
        context.setResponse(null);
        context.setJsonPath(null);

        ConsoleLogger.info("Request State", "cleared before scenario");
    }

    @Given("base url is configured")
    public void base_url_is_configured() {

        baseUrl = ConfigReader.getBaseUrl();

        RestAssured.baseURI = baseUrl;

        ConsoleLogger.section("Base URL");
        ConsoleLogger.info("URL", baseUrl);
    }

    @And("user adds header {string} as {string}")
    public void user_adds_header_as(String key, String value) {

        String resolvedValue = E2ESteps.resolve(value);

        apiClient.addHeader(key, resolvedValue);

        ConsoleLogger.info("Header Added", key + " = " + resolvedValue);

        AllureUtils.attachRequest("Header Added", key + " = " + resolvedValue);
    }

    // Deprecated: Use user_sends_request_to instead
    @When("user sends GET request to {string}")
    public void user_sends_get_request_to(String endpoint) {

        String resolvedEndpoint = E2ESteps.resolve(endpoint);

        context.setResponse(apiClient.get(resolvedEndpoint));

        context.setJsonPath(new JsonPath(context.getResponse().asString()));

        AllureUtils.attachResponse("GET Response Body", context.getResponse().asPrettyString());

        RequestLogger.logRequest(
                "GET Request",
                resolvedEndpoint,
                apiClient.getHeaders(),
                context.getResponse()
        );
    }

    // Deprecated: Use user_sends_request_to instead
    @When("user sends POST request to {string} with body")
    public void user_sends_post_request_to_with_body(String endpoint, String requestBody) {

        String resolvedEndpoint = E2ESteps.resolve(endpoint);

        context.setResponse(apiClient.post(resolvedEndpoint, requestBody));

        context.setJsonPath(
                new JsonPath(context.getResponse().asString())
        );

        AllureUtils.attachRequest("POST Request Body", requestBody);
        AllureUtils.attachResponse("POST Response Body", context.getResponse().asPrettyString());

        RequestLogger.logRequest(
                "POST Request",
                resolvedEndpoint,
                apiClient.getHeaders(),
                context.getResponse()
        );
    }

    // Deprecated: Use user_sends_request_to instead
    @When("user sends POST request to {string} with json file {string}")
    public void user_sends_post_request_to_with_json_file(String endpoint, String filePath) {

        String resolvedEndpoint = E2ESteps.resolve(endpoint);

        context.setRequestBody(JsonUtils.readJson(filePath));

        context.setResponse(
                apiClient.post(resolvedEndpoint, context.getRequestBody())
        );

        context.setJsonPath(
                new JsonPath(context.getResponse().asString())
        );

        AllureUtils.attachRequest("POST Request Body", context.getRequestBody());
        AllureUtils.attachResponse("POST Response Body", context.getResponse().asPrettyString());

        RequestLogger.logRequest(
                "POST Request From File",
                resolvedEndpoint,
                apiClient.getHeaders(),
                context.getResponse()
        );
    }

    @When("user loads json file {string}")
    public void user_loads_json_file(String filePath) {

        context.setRequestBody(JsonUtils.readJson(filePath));

        AllureUtils.attachRequest("Loaded Request Body", context.getRequestBody());

        ConsoleLogger.section("JSON Loaded");
        ConsoleLogger.info("File", filePath);
    }

    @And("user updates request field {string} as {string}")
    public void user_updates_request_field_as(String key, String value) {

        String resolvedValue = E2ESteps.resolve(value);

        context.setRequestBody(
                JsonUtils.updateJsonField(context.getRequestBody(), key, resolvedValue)
        );

        ConsoleLogger.info("Updated Field", key);
        ConsoleLogger.info("New Value", resolvedValue);

        AllureUtils.attachRequest("Updated Request Body", context.getRequestBody());
    }

    // Deprecated: Use user_sends_request_to instead
    @When("user sends POST request to {string} with loaded body")
    public void user_sends_post_request_to_with_loaded_body(String endpoint) {

        String resolvedEndpoint = E2ESteps.resolve(endpoint);

        context.setResponse(
                apiClient.post(resolvedEndpoint, context.getRequestBody())
        );

        context.setJsonPath(
                new JsonPath(context.getResponse().asString())
        );

        AllureUtils.attachRequest("Final Request Body", context.getRequestBody());
        AllureUtils.attachResponse("POST Response Body", context.getResponse().asPrettyString());

        RequestLogger.logRequest(
                "POST Request",
                resolvedEndpoint,
                apiClient.getHeaders(),
                context.getResponse()
        );
    }

    @Then("response status code should be {int}")
    public void response_status_code_should_be(Integer statusCode) {

        AssertionUtils.assertStatusCode(
                context.getResponse().getStatusCode(),
                statusCode
        );

        ConsoleLogger.pass("Status code doğrulandı");
    }

    @And("response field {string} should be {string}")
    public void response_field_should_be(String key, String expectedValue) {

        Object actualValue = context.getJsonPath().get(key);

        AssertionUtils.assertField(actualValue, expectedValue, key);

        ConsoleLogger.pass(key + " doğrulandı");
    }

    @And("response field {string} should not be null")
    public void response_field_should_not_be_null(String key) {

        Object actualValue = context.getJsonPath().get(key);

        AssertionUtils.assertFieldNotNull(key, actualValue);

        ConsoleLogger.pass(key + " null değil");
    }

    @And("response field {string} should be null")
    public void response_field_should_be_null(String key) {

        Object actualValue = context.getJsonPath().get(key);

        AssertionUtils.assertFieldNull(key, actualValue);

        ConsoleLogger.pass(key + " null doğrulandı");
    }

    @And("response field {string} should contain {string}")
    public void response_field_should_contain(String key, String expectedValue) {

        Object actualValue = context.getJsonPath().get(key);

        AssertionUtils.assertFieldContains(key, actualValue, expectedValue);

        ConsoleLogger.pass(key + " contains doğrulandı");
    }

    @And("response time should be less than {int} ms")
    public void response_time_should_be_less_than_ms(Integer expectedMaxTime) {

        long actualTime = context.getResponse().time();

        AssertionUtils.assertResponseTimeLessThan(actualTime, expectedMaxTime);

        ConsoleLogger.pass("Response time doğrulandı");
    }

    @And("response array {string} size should be {int}")
    public void response_array_size_should_be(String key, Integer expectedSize) {

        List<Object> array = context.getJsonPath().getList(key);

        int actualSize = array.size();

        AssertionUtils.assertArraySize(key, actualSize, expectedSize);

        ConsoleLogger.pass(key + " size doğrulandı");
    }

    @Then("response field {string} should exist")
    public void response_field_should_exist(String key) {

        Object actualValue = context.getJsonPath().get(key);

        AssertionUtils.assertFieldExists(key, actualValue);

        ConsoleLogger.pass(key + " exists doğrulandı");
    }

    @Then("response field {string} should not exist")
    public void response_field_should_not_exist(String key) {

        Object actualValue = context.getJsonPath().get(key);

        AssertionUtils.assertFieldNotExists(key, actualValue);

        ConsoleLogger.pass(key + " does not exist doğrulandı");
    }

    @Then("response array {string} should contain {string}")
    public void response_array_should_contain(String key, String expectedValue) {

        List<?> values = context.getJsonPath().getList(key);

        AssertionUtils.assertArrayContains(key, values, expectedValue);

        ConsoleLogger.pass(key + " array contains doğrulandı");
    }

    @Then("response field {string} should be greater than {string}")
    public void response_field_should_be_greater_than(String key, String expectedValue) {

        int actualValue = context.getJsonPath().getInt(key);
        int expected = Integer.parseInt(expectedValue);

        AssertionUtils.assertGreaterThan(key, actualValue, expected);

        ConsoleLogger.pass("Greater than doğrulandı");
    }

    @Then("response field {string} should be less than {string}")
    public void response_field_should_be_less_than(String key, String expectedValue) {

        int actualValue = context.getJsonPath().getInt(key);
        int expected = Integer.parseInt(expectedValue);

        AssertionUtils.assertLessThan(key, actualValue, expected);

        ConsoleLogger.pass("Less than doğrulandı");
    }

    @And("response should match json schema {string}")
    public void response_should_match_json_schema(String schemaPath) {

        AssertionUtils.assertJsonSchema(context.getResponse(), schemaPath);
    }

    @When("user sends {string} request to {string}")
    public void user_sends_request_to(String method, String endpoint) {

        String resolvedEndpoint = E2ESteps.resolve(endpoint);

        context.setResponse(
                apiClient.sendRequest(method, resolvedEndpoint, null)
        );

        context.setJsonPath(
                new JsonPath(context.getResponse().asString())
        );

        AllureUtils.attachResponse(
                method + " Response Body",
                context.getResponse().asPrettyString()
        );

        RequestLogger.logRequest(
                method + " Request",
                resolvedEndpoint,
                apiClient.getHeaders(),
                context.getResponse()
        );
    }

    @When("user sends {string} request to {string} with loaded body")
    public void user_sends_request_to_with_loaded_body(String method, String endpoint) {

        String resolvedEndpoint = E2ESteps.resolve(endpoint);

        context.setResponse(
                apiClient.sendRequest(
                        method,
                        resolvedEndpoint,
                        context.getRequestBody()
                )
        );

        context.setJsonPath(
                new JsonPath(context.getResponse().asString())
        );

        AllureUtils.attachRequest(
                method + " Request Body",
                context.getRequestBody()
        );

        AllureUtils.attachResponse(
                method + " Response Body",
                context.getResponse().asPrettyString()
        );

        RequestLogger.logRequest(
                method + " Request",
                resolvedEndpoint,
                apiClient.getHeaders(),
                context.getResponse()
        );
    }
}
