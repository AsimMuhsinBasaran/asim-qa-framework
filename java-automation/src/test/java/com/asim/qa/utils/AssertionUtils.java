package com.asim.qa.utils;

import io.qameta.allure.Allure;

import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertNull;

import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

public class AssertionUtils {

    public static void assertField(Object actual, String expected, String fieldName) {

        String actualValue = String.valueOf(actual);
        String maskedExpected = SensitiveDataMasker.mask(fieldName, expected);
        String maskedActual = SensitiveDataMasker.mask(fieldName, actualValue);

        ConsoleLogger.info("Field", fieldName);
        ConsoleLogger.info("Expected", maskedExpected);
        ConsoleLogger.info("Actual", maskedActual);

        Allure.addAttachment(
                "Assertion - " + fieldName,
                "Expected : " + maskedExpected + "\n" +
                        "Actual   : " + maskedActual
        );

        try {

            assertEquals(actualValue, expected);

        } catch (AssertionError e) {

            ConsoleLogger.fail(fieldName + " doğrulanamadı");

            throw e;
        }
    }

    public static void assertFieldNotNull(String fieldName, Object actual) {

        ConsoleLogger.info("Field", fieldName);

        String maskedActual = SensitiveDataMasker.mask(fieldName, actual);

        ConsoleLogger.info("Expected", "NOT NULL");

        ConsoleLogger.info("Actual", maskedActual);

        Allure.addAttachment(

                "Assertion - " + fieldName,

                "Expected : NOT NULL\n" +

                        "Actual   : " + maskedActual

        );

        try {

            org.testng.Assert.assertNotNull(actual);

        } catch (AssertionError e) {

            ConsoleLogger.fail(fieldName + " null geldi");

            throw e;

        }

    }

    public static void assertStatusCode(int actual, int expected) {

        ConsoleLogger.info("Field", "statusCode");

        ConsoleLogger.info("Expected", expected);

        ConsoleLogger.info("Actual", actual);

        Allure.addAttachment(

                "Assertion - statusCode",

                "Field: statusCode\n" +

                        "Expected: " + expected + "\n" +

                        "Actual: " + actual

        );

        try {

            assertEquals(actual, expected);

        } catch (AssertionError e) {

            ConsoleLogger.fail("Status code doğrulanamadı");

            throw e;

        }

    }

    public static void assertFieldContains(String key, Object actual, String expected) {

        String actualValue = String.valueOf(actual);
        String maskedExpected = SensitiveDataMasker.mask(key, expected);
        String maskedActual = SensitiveDataMasker.mask(key, actualValue);

        ConsoleLogger.info("Field", key);
        ConsoleLogger.info("Expected Contains", maskedExpected);
        ConsoleLogger.info("Actual", maskedActual);

        Allure.addAttachment(
                "Assertion - " + key,
                "Field: " + key + "\n" +
                        "Expected Contains: " + maskedExpected + "\n" +
                        "Actual: " + maskedActual
        );

        try {

            if (!actualValue.contains(expected)) {

                throw new AssertionError(
                        "expected value containing [" + expected + "] but found [" + actualValue + "]"
                );
            }

        } catch (AssertionError e) {

            ConsoleLogger.fail(key + " contains doğrulanamadı");

            throw e;
        }
    }

    public static void assertResponseTimeLessThan(long actualTime, int expectedMaxTime) {

        ConsoleLogger.info("Field", "responseTime");
        ConsoleLogger.info("Expected", "< " + expectedMaxTime + " ms");
        ConsoleLogger.info("Actual", actualTime + " ms");

        Allure.addAttachment(
                "Assertion - responseTime",
                "Field: responseTime\n" +
                        "Expected: < " + expectedMaxTime + " ms\n" +
                        "Actual: " + actualTime + " ms"
        );

        try {

            if (actualTime >= expectedMaxTime) {
                throw new AssertionError(
                        "expected response time less than [" + expectedMaxTime + " ms] but found [" + actualTime + " ms]"
                );
            }

        } catch (AssertionError e) {

            ConsoleLogger.fail("Response time doğrulanamadı");

            throw e;
        }
    }

    public static void assertArraySize(String field,
                                       int actualSize,
                                       int expectedSize) {

        ConsoleLogger.info("Field", field);
        ConsoleLogger.info("Expected Size", expectedSize);
        ConsoleLogger.info("Actual Size", actualSize);

        Allure.addAttachment(
                "Assertion - " + field + " Array Size",
                "Expected Size: " + expectedSize +
                        "\nActual Size: " + actualSize
        );

        try {

            assertEquals(actualSize, expectedSize);

        } catch (AssertionError e) {

            ConsoleLogger.fail(field + " size doğrulanamadı");

            throw e;
        }
    }

    public static void assertFieldNull(String field, Object actualValue) {

        ConsoleLogger.info("Field", field);
        ConsoleLogger.info("Expected", null);
        ConsoleLogger.info("Actual", SensitiveDataMasker.mask(field, actualValue));

        Allure.addAttachment(
                "Assertion - " + field,
                "Expected: null" +
                        "\nActual: " + SensitiveDataMasker.mask(field, actualValue)
        );

        try {

            assertNull(actualValue);

        } catch (AssertionError e) {

            ConsoleLogger.fail(field + " null doğrulanamadı");

            throw e;
        }
    }

    public static void assertGreaterThan(String field, int actual, int expected) {

        ConsoleLogger.info("Field", field);
        ConsoleLogger.info("Expected", "> " + expected);
        ConsoleLogger.info("Actual", actual);

        Allure.addAttachment(
                "Assertion - " + field,
                "Field: " + field + "\n" +
                        "Expected: > " + expected + "\n" +
                        "Actual: " + actual
        );

        try {

            if (actual <= expected) {
                throw new AssertionError(
                        "Expected [" + actual + "] to be greater than [" + expected + "]"
                );
            }

        } catch (AssertionError e) {

            ConsoleLogger.fail(field + " greater than doğrulanamadı");

            throw e;
        }
    }

    public static void assertLessThan(String field, int actual, int expected) {

        ConsoleLogger.info("Field", field);
        ConsoleLogger.info("Expected", "< " + expected);
        ConsoleLogger.info("Actual", actual);

        Allure.addAttachment(
                "Assertion - " + field,
                "Field: " + field + "\n" +
                        "Expected: < " + expected + "\n" +
                        "Actual: " + actual
        );

        try {

            if (actual >= expected) {
                throw new AssertionError(
                        "Expected [" + actual + "] to be less than [" + expected + "]"
                );
            }

        } catch (AssertionError e) {

            ConsoleLogger.fail(field + " less than doğrulanamadı");

            throw e;
        }
    }

    public static void assertFieldExists(String field, Object actualValue) {

        boolean exists = actualValue != null;
        String maskedActual = SensitiveDataMasker.mask(field, actualValue);

        ConsoleLogger.info("Field", field);
        ConsoleLogger.info("Expected", "EXISTS");
        ConsoleLogger.info("Actual Exists", exists);

        Allure.addAttachment(
                "Assertion - " + field + " Exists",
                        "Field: " + field + "\n" +
                        "Expected: EXISTS\n" +
                        "Actual Exists: " + exists + "\n" +
                        "Actual Value: " + maskedActual
        );

        try {

            org.testng.Assert.assertNotNull(actualValue, "Field not found: " + field);

        } catch (AssertionError e) {

            ConsoleLogger.fail(field + " exists doğrulanamadı");

            throw e;
        }
    }

    public static void assertFieldNotExists(String field, Object actualValue) {

        boolean exists = actualValue != null;
        String maskedActual = SensitiveDataMasker.mask(field, actualValue);

        ConsoleLogger.info("Field", field);
        ConsoleLogger.info("Expected", "NOT EXISTS");
        ConsoleLogger.info("Actual Exists", exists);

        Allure.addAttachment(
                "Assertion - " + field + " Not Exists",
                        "Field: " + field + "\n" +
                        "Expected: NOT EXISTS\n" +
                        "Actual Exists: " + exists + "\n" +
                        "Actual Value: " + maskedActual
        );

        try {

            org.testng.Assert.assertNull(actualValue, "Field should not exist: " + field);

        } catch (AssertionError e) {

            ConsoleLogger.fail(field + " not exist doğrulanamadı");

            throw e;
        }
    }

    public static void assertArrayContains(String field,
                                           java.util.List<?> actualValues,
                                           String expectedValue) {

        java.util.List<String> normalizedValues = actualValues.stream()
                .map(String::valueOf)
                .toList();
        String maskedExpected = SensitiveDataMasker.mask(field, expectedValue);
        String maskedActual = SensitiveDataMasker.mask(field, normalizedValues);

        ConsoleLogger.info("Field", field);
        ConsoleLogger.info("Expected Value", maskedExpected);
        ConsoleLogger.info("Actual Values", maskedActual);

        Allure.addAttachment(
                "Assertion - " + field + " Array Contains",
                        "Field: " + field + "\n" +
                        "Expected Value: " + maskedExpected + "\n" +
                        "Actual Values: " + maskedActual
        );

        try {

            org.testng.Assert.assertTrue(
                    normalizedValues.contains(expectedValue),
                    "Expected value not found in array: " + expectedValue
            );

        } catch (AssertionError e) {

            ConsoleLogger.fail(field + " array contains doğrulanamadı");

            throw e;
        }
    }
    public static void assertJsonSchema(Response response, String schemaPath) {

        ConsoleLogger.info("Schema File", schemaPath);

        Allure.addAttachment(
                "JSON Schema Validation",
                "Schema File: " + schemaPath
        );

        try {

            response.then()
                    .assertThat()
                    .body(matchesJsonSchemaInClasspath(schemaPath));

            ConsoleLogger.pass("JSON schema doğrulandı");

        } catch (AssertionError e) {

            ConsoleLogger.fail("JSON schema doğrulanamadı");

            Allure.addAttachment(
                    "Schema Validation Error",
                    e.getMessage()
            );

            throw e;
        }
    }

}
