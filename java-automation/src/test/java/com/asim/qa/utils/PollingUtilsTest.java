package com.asim.qa.utils;

import org.testng.Assert;
import org.testng.annotations.Test;

public class PollingUtilsTest {

    @Test
    public void should_match_numeric_actual_with_numeric_expected() {

        Assert.assertTrue(PollingUtils.fieldValueMatchesExpected(1, "1"));
    }

    @Test
    public void should_match_boolean_actual_with_boolean_expected() {

        Assert.assertTrue(PollingUtils.fieldValueMatchesExpected(true, "true"));
    }

    @Test
    public void should_match_null_actual_with_null_expected() {

        Assert.assertTrue(PollingUtils.fieldValueMatchesExpected(null, "null"));
    }

    @Test
    public void should_match_string_actual_with_string_expected() {

        Assert.assertTrue(PollingUtils.fieldValueMatchesExpected("1", "1"));
    }

    @Test
    public void should_not_match_numeric_actual_with_ambiguous_leading_zero_expected() {

        Assert.assertFalse(PollingUtils.fieldValueMatchesExpected(1, "01"));
    }
}
