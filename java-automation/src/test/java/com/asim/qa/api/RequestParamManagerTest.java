package com.asim.qa.api;

import com.asim.qa.context.TestContext;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

public class RequestParamManagerTest {

    @Test
    public void should_add_path_param_and_overwrite_same_key() {

        RequestParamManager manager = new RequestParamManager();

        manager.addPathParam("id", "1");
        manager.addPathParam("id", "2");

        Map<String, String> snapshot = manager.getPathParamsSnapshot();

        Assert.assertEquals(snapshot.get("id"), "2");
        Assert.assertEquals(snapshot.size(), 1);
    }

    @Test
    public void should_add_query_param_and_overwrite_same_key() {

        RequestParamManager manager = new RequestParamManager();

        manager.addQueryParam("page", "1");
        manager.addQueryParam("page", "2");

        Map<String, String> snapshot = manager.getQueryParamsSnapshot();

        Assert.assertEquals(snapshot.get("page"), "2");
        Assert.assertEquals(snapshot.size(), 1);
    }

    @Test
    public void should_resolve_placeholder_with_scenario_variable() {

        RequestParamManager manager = new RequestParamManager();
        TestContext context = new TestContext();
        context.saveScenarioVariable("postId", "99");

        manager.addPathParam(context, "id", "{postId}");
        manager.addQueryParam(context, "filter", "post-{postId}");

        Assert.assertEquals(manager.getPathParamsSnapshot().get("id"), "99");
        Assert.assertEquals(manager.getQueryParamsSnapshot().get("filter"), "post-99");
        Assert.assertEquals(manager.resolveEndpoint(context, "/posts/{postId}"), "/posts/99");
    }

    @Test(expectedExceptions = RuntimeException.class,
            expectedExceptionsMessageRegExp = ".*Unresolved endpoint placeholder: missingId.*")
    public void should_throw_clear_error_for_missing_placeholder() {

        RequestParamManager manager = new RequestParamManager();
        TestContext context = new TestContext();

        manager.resolveEndpoint(context, "/posts/{missingId}");
    }

    @Test
    public void should_clear_only_path_and_query_params_in_api_client() {

        ApiClient apiClient = new ApiClient();
        apiClient.addHeader("X-Custom", "value");
        apiClient.useBearerAuth("token-123");
        apiClient.addPathParam("id", "1");
        apiClient.addQueryParam("page", "10");

        apiClient.clearParams();

        Assert.assertTrue(apiClient.getPathParamsSnapshot().isEmpty());
        Assert.assertTrue(apiClient.getQueryParamsSnapshot().isEmpty());
        Assert.assertEquals(apiClient.getHeaders().get("Content-Type"), "application/json");
        Assert.assertEquals(apiClient.getHeaders().get("X-Custom"), "value");
        Assert.assertEquals(apiClient.getHeaders().get("Authorization"), "Bearer token-123");
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = ".*Path param key must not be blank.*")
    public void should_reject_blank_path_param_key() {

        RequestParamManager manager = new RequestParamManager();
        manager.addPathParam("   ", "1");
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = ".*Query param key must not be null.*")
    public void should_reject_null_query_param_key() {

        RequestParamManager manager = new RequestParamManager();
        manager.addQueryParam(null, "1");
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = ".*Path param value must not be null.*")
    public void should_reject_null_path_param_value() {

        RequestParamManager manager = new RequestParamManager();
        manager.addPathParam("id", null);
    }
}
