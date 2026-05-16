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

        ConsoleLogger.info("Field", fieldName);
        ConsoleLogger.info("Expected", expected);
        ConsoleLogger.info("Actual", actualValue);

        Allure.addAttachment(
                "Assertion - " + fieldName,
                "Expected : " + expected + "\n" +
                        "Actual   : " + actualValue
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

        ConsoleLogger.info("Expected", "NOT NULL");

        ConsoleLogger.info("Actual", actual);

        Allure.addAttachment(

                "Assertion - " + fieldName,

                "Expected : NOT NULL\n" +

                        "Actual   : " + actual

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

        ConsoleLogger.info("Field", key);
        ConsoleLogger.info("Expected Contains", expected);
        ConsoleLogger.info("Actual", actualValue);

        Allure.addAttachment(
                "Assertion - " + key,
                "Field: " + key + "\n" +
                        "Expected Contains: " + expected + "\n" +
                        "Actual: " + actualValue
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
        ConsoleLogger.info("Actual", actualValue);

        Allure.addAttachment(
                "Assertion - " + field,
                "Expected: null" +
                        "\nActual: " + actualValue
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

        ConsoleLogger.info("Field", field);
        ConsoleLogger.info("Expected", "EXISTS");
        ConsoleLogger.info("Actual Exists", exists);

        Allure.addAttachment(
                "Assertion - " + field + " Exists",
                "Field: " + field + "\n" +
                        "Expected: EXISTS\n" +
                        "Actual Exists: " + exists + "\n" +
                        "Actual Value: " + actualValue
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

        ConsoleLogger.info("Field", field);
        ConsoleLogger.info("Expected", "NOT EXISTS");
        ConsoleLogger.info("Actual Exists", exists);

        Allure.addAttachment(
                "Assertion - " + field + " Not Exists",
                "Field: " + field + "\n" +
                        "Expected: NOT EXISTS\n" +
                        "Actual Exists: " + exists + "\n" +
                        "Actual Value: " + actualValue
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

        ConsoleLogger.info("Field", field);
        ConsoleLogger.info("Expected Value", expectedValue);
        ConsoleLogger.info("Actual Values", normalizedValues);

        Allure.addAttachment(
                "Assertion - " + field + " Array Contains",
                "Field: " + field + "\n" +
                        "Expected Value: " + expectedValue + "\n" +
                        "Actual Values: " + normalizedValues
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