package com.asim.qa.data;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class TestDataFactoryTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-06-01T12:30:45Z"), ZoneOffset.UTC);
    private static final List<String> SENSITIVE_KEYWORDS = List.of(
            "token",
            "password",
            "secret",
            "cookie",
            "session"
    );

    @Test
    public void should_generate_run_id_with_qa_prefix() {

        TestDataFactory factory = new TestDataFactory(FIXED_CLOCK);

        String runId = factory.runId();

        Assert.assertNotNull(runId);
        Assert.assertFalse(runId.isBlank());
        Assert.assertTrue(runId.startsWith("QA-"));
        Assert.assertTrue(runId.contains("20260601123045"));
    }

    @Test
    public void should_generate_traceable_external_refs() {

        TestDataFactory factory = new TestDataFactory(FIXED_CLOCK);

        String externalRef = factory.externalRef();
        String customerExternalRef = factory.customerExternalRef();
        String orderExternalRef = factory.orderExternalRef();

        Assert.assertTrue(externalRef.startsWith("QA-REF-"));
        Assert.assertTrue(customerExternalRef.startsWith("QA-CUSTOMER-"));
        Assert.assertTrue(orderExternalRef.startsWith("QA-ORDER-"));
    }

    @Test
    public void should_generate_unique_external_refs_across_calls() {

        TestDataFactory factory = new TestDataFactory(FIXED_CLOCK);

        Set<String> values = Set.of(
                factory.externalRef(),
                factory.externalRef(),
                factory.customerExternalRef(),
                factory.orderExternalRef()
        );

        Assert.assertEquals(values.size(), 4);
    }

    @Test
    public void should_generate_lowercase_example_test_email() {

        TestDataFactory factory = new TestDataFactory(FIXED_CLOCK);

        String email = factory.email();

        Assert.assertTrue(email.startsWith("qa+qa-"));
        Assert.assertTrue(email.endsWith("@example.test"));
        Assert.assertEquals(email, email.toLowerCase(Locale.ROOT));
    }

    @Test
    public void should_generate_unique_emails_across_calls() {

        TestDataFactory factory = new TestDataFactory(FIXED_CLOCK);

        Set<String> emails = Set.of(
                factory.email(),
                factory.email(),
                factory.email()
        );

        Assert.assertEquals(emails.size(), 3);
    }

    @Test
    public void should_generate_synthetic_gsm_like_values() {

        TestDataFactory factory = new TestDataFactory(FIXED_CLOCK);

        String gsm = factory.gsm();

        Assert.assertTrue(gsm.matches("\\+999000\\d{7}"));
    }

    @Test
    public void should_generate_values_without_sensitive_keywords() {

        TestDataFactory factory = new TestDataFactory(FIXED_CLOCK);

        List<String> generatedValues = List.of(
                factory.runId(),
                factory.externalRef(),
                factory.customerExternalRef(),
                factory.orderExternalRef(),
                factory.email(),
                factory.gsm()
        );

        for (String value : generatedValues) {
            assertPublicSafe(value);
        }
    }

    private static void assertPublicSafe(String value) {

        Assert.assertNotNull(value);
        Assert.assertFalse(value.isBlank());

        String normalizedValue = value.toLowerCase(Locale.ROOT);
        for (String sensitiveKeyword : SENSITIVE_KEYWORDS) {
            Assert.assertFalse(
                    normalizedValue.contains(sensitiveKeyword),
                    "Generated value should not contain sensitive keyword: " + value
            );
        }
    }
}
