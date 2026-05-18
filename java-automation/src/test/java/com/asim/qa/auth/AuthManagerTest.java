package com.asim.qa.auth;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.LinkedHashMap;
import java.util.Map;

public class AuthManagerTest {

    @Test
    public void should_apply_bearer_auth_to_headers() {

        AuthManager authManager = new AuthManager();
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "old-token");

        authManager.useBearerAuth("new-token");

        Map<String, String> effectiveHeaders = authManager.applyToHeaders(headers);

        Assert.assertEquals(effectiveHeaders.get("Authorization"), "Bearer new-token");
        Assert.assertEquals(effectiveHeaders.get("Content-Type"), "application/json");
    }

    @Test
    public void should_apply_basic_auth_to_headers() {

        AuthManager authManager = new AuthManager();
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Content-Type", "application/json");

        authManager.useBasicAuth("demo-user", "demo-pass");

        Map<String, String> effectiveHeaders = authManager.applyToHeaders(headers);

        Assert.assertEquals(effectiveHeaders.get("Authorization"), "Basic ZGVtby11c2VyOmRlbW8tcGFzcw==");
        Assert.assertEquals(effectiveHeaders.get("Content-Type"), "application/json");
    }

    @Test
    public void should_apply_api_key_to_headers() {

        AuthManager authManager = new AuthManager();
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("X-Api-Key", "old-key");

        authManager.useApiKeyAuth("X-Api-Key", "new-key");

        Map<String, String> effectiveHeaders = authManager.applyToHeaders(headers);

        Assert.assertEquals(effectiveHeaders.get("X-Api-Key"), "new-key");
        Assert.assertEquals(effectiveHeaders.get("Content-Type"), "application/json");
    }

    @Test
    public void should_overwrite_existing_authorization_header_when_bearer_auth_is_applied() {

        AuthManager authManager = new AuthManager();
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "old-token");

        authManager.useBearerAuth("override-token");

        Map<String, String> effectiveHeaders = authManager.applyToHeaders(headers);

        Assert.assertEquals(effectiveHeaders.size(), 2);
        Assert.assertEquals(effectiveHeaders.get("Authorization"), "Bearer override-token");
    }

    @Test
    public void should_mask_sensitive_auth_values_in_description() {

        AuthManager authManager = new AuthManager();
        authManager.useBearerAuth("super-secret-token");

        String description = authManager.describeAuth();

        Assert.assertTrue(description.contains("***MASKED***"));
        Assert.assertFalse(description.contains("super-secret-token"));
    }
}
