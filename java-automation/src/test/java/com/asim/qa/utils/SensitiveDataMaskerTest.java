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
    public void should_not_mask_non_sensitive_key_in_mask_overload() {

        String masked = SensitiveDataMasker.mask("title", "Public Title");

        Assert.assertEquals(masked, "Public Title");
    }
}
