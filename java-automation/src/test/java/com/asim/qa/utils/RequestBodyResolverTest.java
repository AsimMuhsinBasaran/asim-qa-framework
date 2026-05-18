package com.asim.qa.utils;

import com.asim.qa.context.TestContext;
import org.testng.Assert;
import org.testng.annotations.Test;

public class RequestBodyResolverTest {

    @Test
    public void should_resolve_placeholders_in_request_body() {

        TestContext context = new TestContext();
        context.saveScenarioVariable("name", "Alice");

        String resolved = RequestBodyResolver.resolve(context, "{\"name\":\"{name}\"}");

        Assert.assertEquals(resolved, "{\"name\":\"Alice\"}");
    }

    @Test
    public void should_resolve_mixed_string_interpolation_in_request_body() {

        TestContext context = new TestContext();
        context.saveScenarioVariable("id", "42");

        String resolved = RequestBodyResolver.resolve(context, "user-{id}-active");

        Assert.assertEquals(resolved, "user-42-active");
    }

    @Test(expectedExceptions = RuntimeException.class,
            expectedExceptionsMessageRegExp = ".*Scenario variable not found: missing.*")
    public void should_fail_fast_when_placeholder_is_missing() {

        TestContext context = new TestContext();

        RequestBodyResolver.resolve(context, "{\"id\":\"{missing}\"}");
    }

    @Test
    public void should_preserve_null_and_blank_bodies() {

        TestContext context = new TestContext();

        Assert.assertNull(RequestBodyResolver.resolve(context, null));
        Assert.assertEquals(RequestBodyResolver.resolve(context, ""), "");
        Assert.assertEquals(RequestBodyResolver.resolve(context, "   "), "   ");
    }
}
