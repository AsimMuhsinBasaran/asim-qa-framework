package com.asim.qa.utils;

import com.asim.qa.context.TestContext;
import io.restassured.builder.ResponseBuilder;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ScenarioContextUtilsTest {

    @Test
    public void should_resolve_exact_placeholder() {

        TestContext context = new TestContext();
        context.saveScenarioVariable("token", "abc123");

        String resolved = ScenarioContextUtils.resolveText(context, "{token}");

        Assert.assertEquals(resolved, "abc123");
    }

    @Test
    public void should_resolve_mixed_string_interpolation() {

        TestContext context = new TestContext();
        context.saveScenarioVariable("token", "abc123");
        context.saveScenarioVariable("id", "42");

        String resolved = ScenarioContextUtils.resolveText(context, "prefix-{token}-user-{id}-active");

        Assert.assertEquals(resolved, "prefix-abc123-user-42-active");
    }

    @Test
    public void should_throw_clear_error_when_variable_is_missing() {

        TestContext context = new TestContext();

        assertThrowsWithMessage(
                RuntimeException.class,
                "missingToken",
                () -> ScenarioContextUtils.resolveText(context, "Bearer {missingToken}")
        );
    }

    @Test
    public void should_extract_response_field_and_save_variable() {

        TestContext context = new TestContext();
        Response response = response("{\"token\":\"secret-token-123\",\"id\":1001}");
        context.setResponse(response);
        context.setJsonPath(new JsonPath(response.asString()));

        String extracted = ScenarioContextUtils.saveResponseFieldAsVariable(context, "token", "authToken");

        Assert.assertEquals(extracted, "secret-token-123");
        Assert.assertTrue(context.contains("authToken"));
        Assert.assertEquals(context.getScenarioVariable("authToken"), "secret-token-123");
    }

    @Test
    public void should_throw_when_response_is_missing_during_extraction() {

        TestContext context = new TestContext();

        assertThrowsWithMessage(
                RuntimeException.class,
                "response is null",
                () -> ScenarioContextUtils.extractResponseField(context, "token")
        );
    }

    @Test
    public void should_throw_when_json_path_is_missing_during_extraction() {

        TestContext context = new TestContext();
        context.setResponse(response("{\"token\":\"secret-token-123\"}"));

        assertThrowsWithMessage(
                RuntimeException.class,
                "json path is null",
                () -> ScenarioContextUtils.extractResponseField(context, "token")
        );
    }

    @Test
    public void should_throw_when_extracted_value_is_missing() {

        TestContext context = new TestContext();
        Response response = response("{\"id\":1001}");
        context.setResponse(response);
        context.setJsonPath(new JsonPath(response.asString()));

        assertThrowsWithMessage(
                RuntimeException.class,
                "Response field not found or null: token",
                () -> ScenarioContextUtils.extractResponseField(context, "token")
        );
    }

    @Test
    public void should_return_null_when_allow_null_is_true_and_field_is_missing() {

        TestContext context = new TestContext();
        Response response = response("{\"id\":1001}");
        context.setResponse(response);
        context.setJsonPath(new JsonPath(response.asString()));

        Object extracted = ScenarioContextUtils.extractResponseField(context, "missing", true);

        Assert.assertNull(extracted);
    }

    @Test
    public void should_support_contains_and_clear_scenario_variables() {

        TestContext context = new TestContext();
        context.saveScenarioVariable("authToken", "abc123");

        Assert.assertTrue(context.contains("authToken"));

        context.clearScenarioVariables();

        Assert.assertFalse(context.contains("authToken"));
    }

    private Response response(String body) {

        return new ResponseBuilder()
                .setStatusCode(200)
                .setBody(body)
                .build();
    }

    private void assertThrowsWithMessage(Class<? extends Throwable> expectedType,
                                         String expectedMessagePart,
                                         Runnable action) {

        try {
            action.run();
            Assert.fail("Expected exception of type " + expectedType.getSimpleName());
        } catch (Throwable throwable) {
            Assert.assertTrue(
                    expectedType.isInstance(throwable),
                    "Expected " + expectedType.getSimpleName() + " but got " + throwable.getClass().getSimpleName()
            );
            Assert.assertTrue(
                    throwable.getMessage() != null && throwable.getMessage().contains(expectedMessagePart),
                    "Expected exception message to contain: " + expectedMessagePart + " but was: " + throwable.getMessage()
            );
        }
    }
}
