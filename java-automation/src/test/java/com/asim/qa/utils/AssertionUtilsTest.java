package com.asim.qa.utils;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.math.BigDecimal;

public class AssertionUtilsTest {

    @Test
    public void should_compare_string_values() {

        AssertionUtils.assertField("ACTIVE", "ACTIVE", "status");
        AssertionUtils.assertField("null", "null", "nullableText");
    }

    @Test
    public void should_compare_numeric_values_by_type() {

        AssertionUtils.assertField(1, "1", "id");
        AssertionUtils.assertField(new BigDecimal("10.50"), "10.500", "price");
        Assert.assertFalse(AssertionUtils.matchesExpected(1, "01"));
    }

    @Test
    public void should_compare_boolean_values_by_type() {

        AssertionUtils.assertField(true, "true", "enabled");
    }

    @Test
    public void should_compare_null_values_by_type() {

        AssertionUtils.assertField(null, "null", "deletedAt");
    }

    @Test
    public void should_report_path_expected_actual_and_actual_type_on_failure() {

        AssertionError error = Assert.expectThrows(
                AssertionError.class,
                () -> AssertionUtils.assertField(1, "abc", "id")
        );

        Assert.assertTrue(error.getMessage().contains("path=id"));
        Assert.assertTrue(error.getMessage().contains("expected=abc"));
        Assert.assertTrue(error.getMessage().contains("actual=1"));
        Assert.assertTrue(error.getMessage().contains("actualType=Integer"));
    }
}
