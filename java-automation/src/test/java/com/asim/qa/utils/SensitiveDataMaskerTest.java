package com.asim.qa.utils;

import org.testng.Assert;
import org.testng.annotations.Test;

public class SensitiveDataMaskerTest {

    @Test
    public void should_mask_bearer_token() {

        String input = "Authorization: Bearer abc123xyz";

        String masked = SensitiveDataMasker.mask(input);

        Assert.assertEquals(masked, "Authorization: Bearer ***MASKED***");
    }

    @Test
    public void should_mask_json_string_token_field() {

        String input = "{\"token\":\"secret-token-123\"}";

        String masked = SensitiveDataMasker.mask(input);

        Assert.assertEquals(masked, "{\"token\":\"***MASKED***\"}");
    }

    @Test
    public void should_mask_basic_authorization_header() {

        String input = "Authorization: Basic dXNlcjpwYXNz";

        String masked = SensitiveDataMasker.mask(input);

        Assert.assertEquals(masked, "Authorization: Basic ***MASKED***");
    }

    @Test
    public void should_mask_key_value_password() {

        String input = "password=mySecret123";

        String masked = SensitiveDataMasker.mask(input);

        Assert.assertEquals(masked, "password=***MASKED***");
    }

    @Test
    public void should_mask_sensitive_key_in_mask_overload() {

        String masked = SensitiveDataMasker.mask("password", "mySecret123");

        Assert.assertEquals(masked, "***MASKED***");
    }

    @Test
    public void should_mask_authorization_key_in_mask_overload() {

        String masked = SensitiveDataMasker.mask("authorization", "Bearer abc123xyz");

        Assert.assertEquals(masked, "***MASKED***");
    }

    @Test
    public void should_mask_session_id_key_in_mask_overload() {

        String masked = SensitiveDataMasker.mask("sessionId", "sid-123");

        Assert.assertEquals(masked, "***MASKED***");
    }

    @Test
    public void should_mask_snake_case_session_id_key_in_mask_overload() {

        String masked = SensitiveDataMasker.mask("session_id", "sid-123");

        Assert.assertEquals(masked, "***MASKED***");
    }

    @Test
    public void should_mask_cookie_key_in_mask_overload() {

        String masked = SensitiveDataMasker.mask("cookie", "sessionId=sid-123");

        Assert.assertEquals(masked, "***MASKED***");
    }

    @Test
    public void should_mask_set_cookie_key_in_mask_overload() {

        String masked = SensitiveDataMasker.mask("set-cookie", "sessionId=sid-123; HttpOnly");

        Assert.assertEquals(masked, "***MASKED***");
    }

    @Test
    public void should_mask_cookie_header() {

        String input = "Cookie: sessionId=sid-123; theme=dark";

        String masked = SensitiveDataMasker.mask(input);

        Assert.assertEquals(masked, "Cookie: ***MASKED***");
    }

    @Test
    public void should_mask_set_cookie_header() {

        String input = "Set-Cookie: sessionId=sid-123; HttpOnly";

        String masked = SensitiveDataMasker.mask(input);

        Assert.assertEquals(masked, "Set-Cookie: ***MASKED***");
    }

    @Test
    public void should_mask_json_session_id_field() {

        String input = "{\"sessionId\":\"sid-123\"}";

        String masked = SensitiveDataMasker.mask(input);

        Assert.assertEquals(masked, "{\"sessionId\":\"***MASKED***\"}");
    }

    @Test
    public void should_mask_key_value_session_id() {

        String input = "sessionId=sid-123";

        String masked = SensitiveDataMasker.mask(input);

        Assert.assertEquals(masked, "sessionId=***MASKED***");
    }

    @Test
    public void should_not_mask_non_sensitive_key_in_mask_overload() {

        String masked = SensitiveDataMasker.mask("title", "Public Title");

        Assert.assertEquals(masked, "Public Title");
    }
}
