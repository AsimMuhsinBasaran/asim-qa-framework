package com.asim.qa.stepdefinitions;

import com.asim.qa.context.TestContext;
import com.asim.qa.utils.AllureUtils;
import com.asim.qa.utils.AssertionUtils;
import com.asim.qa.utils.ConsoleLogger;
import com.asim.qa.utils.JsonUtils;
import com.asim.qa.utils.ScenarioContextUtils;
import com.asim.qa.utils.SensitiveDataMasker;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;

import java.util.Map;

/**
 * E2ESteps — Extends the existing ApiSteps with scenario-level state sharing.
 *
 * Key additions:
 *  - Save a response field to a named variable:  "user saves response field "token" as "authToken""
 *  - Inject a saved variable into request fields: "user updates request field "userId" as "{userId}""
 *  - Delete steps:                                "user sends "DELETE" request to "/products/1""
 *
 * NOTE: The DELETE step without body is already handled by ApiSteps#user_sends_request_to
 *       via ApiClient#sendRequest (body=null branch). No extra step needed.
 *
 * HOW VARIABLE INJECTION WORKS
 * ─────────────────────────────
 * 1. After a successful response, call:
 *      And user saves response field "token" as "authToken"
 *    This stores the value in the scenario-scoped TestContext.
 *
 * 2. In subsequent steps, reference it inside curly braces:
 *      And user adds header "Authorization" as "Bearer {authToken}"
 *      And user updates request field "userId" as "{userId}"
 *    The step resolver checks TestContext and replaces {key} with the saved value.
 */
public class E2ESteps {

    private final TestContext context;

    public E2ESteps(TestContext context) {
        this.context = context;
    }

    // ────────────────────────────────────────────────────────────────────────────
    // Context: save & resolve
    // ────────────────────────────────────────────────────────────────────────────

    /**
     * Saves a value from the last response into a named context variable.
     *
     * Gherkin: And user saves response field "token" as "authToken"
     */
    @And("user saves response field {string} as {string}")
    public void user_saves_response_field_as(String fieldKey, String variableName) {

        ScenarioContextUtils.saveResponseFieldAsVariable(context, fieldKey, variableName);
    }

    // ────────────────────────────────────────────────────────────────────────────
    // Extra assertions useful in E2E flows
    // ────────────────────────────────────────────────────────────────────────────

    /**
     * Asserts that a saved context variable equals an expected value.
     *
     * Gherkin: Then saved value "authToken" should not be null
     */
    @Then("saved value {string} should not be null")
    public void saved_value_should_not_be_null(String variableName) {

        String value = context.getScenarioVariable(variableName);

        AssertionUtils.assertFieldNotNull(variableName, value);

        ConsoleLogger.pass("Saved value '" + variableName + "' is not null: " + value);
    }

    /**
     * Asserts that the response status stored in context matches expected.
     * Useful for chained E2E checks without re-running the request.
     *
     * Gherkin: Then saved value "userId" should be "1001"
     */
    @Then("saved value {string} should be {string}")
    public void saved_value_should_be(String variableName, String expectedValue) {

        String actualValue = context.getScenarioVariable(variableName);

        AssertionUtils.assertField(actualValue, expectedValue, variableName);

        ConsoleLogger.pass("Saved value '" + variableName + "' = " + actualValue);
    }

    // ────────────────────────────────────────────────────────────────────────────
    // Dynamic JSON field update with context variable resolution
    // ────────────────────────────────────────────────────────────────────────────

    /**
     * Updates a request JSON field; resolves {variable} placeholders automatically.
     * Gherkin: And user updates request field "userId" as "{userId}"
     *  * Updates a request JSON field after resolving scenario variables.
     *  * Example:
     *    And user updates request field "userId" as "{userId}"
     *   Variable placeholders are resolved through TestContext before the request
     *  body is updated.

     */
    @And("user updates request field {string} with saved value {string}")
    public void user_updates_request_field_with_saved_value(String fieldKey, String variableName) {

        String resolvedValue = ScenarioContextUtils.requireVariable(context, variableName);

        context.setRequestBody(
                JsonUtils.updateJsonField(context.getRequestBody(), fieldKey, resolvedValue)
        );

        ConsoleLogger.info("Updated Field (from context)", fieldKey + " = " + resolvedValue);
        AllureUtils.attachRequest("Updated Request Body", context.getRequestBody());
    }

    @And("user prints saved scenario variables")
    public void user_prints_saved_scenario_variables() {

        Map<String, String> scenarioVariables = context.getScenarioVariablesSnapshot();

        if (scenarioVariables.isEmpty()) {
            String message = "No scenario variables saved.";
            ConsoleLogger.info("Scenario Variables", message);
            AllureUtils.attachRequest("Scenario Variables", message);
            return;
        }

        StringBuilder output = new StringBuilder();
        scenarioVariables.forEach((key, value) -> {
            String maskedValue = SensitiveDataMasker.mask(key, value);
            output.append(key).append("=").append(maskedValue).append(System.lineSeparator());
        });

        String maskedOutput = output.toString().trim();
        ConsoleLogger.info("Scenario Variables", maskedOutput);
        AllureUtils.attachRequest("Scenario Variables", maskedOutput);
    }
}
