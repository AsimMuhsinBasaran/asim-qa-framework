package com.asim.qa.utils;

import com.asim.qa.context.TestContext;

import io.restassured.path.json.JsonPath;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ScenarioContextUtils {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{([^{}]+)}");

    private ScenarioContextUtils() {
    }

    public static String resolveText(TestContext context, String value) {

        if (value == null || !value.contains("{")) {
            return value;
        }

        Matcher matcher = PLACEHOLDER_PATTERN.matcher(value);
        StringBuffer resolved = new StringBuffer();

        while (matcher.find()) {
            String variableName = matcher.group(1);
            String variableValue = requireVariable(context, variableName);
            matcher.appendReplacement(resolved, Matcher.quoteReplacement(variableValue));
        }

        matcher.appendTail(resolved);

        return resolved.toString();
    }

    public static String requireVariable(TestContext context, String variableName) {

        if (context == null) {
            throw new RuntimeException("Scenario context is not available.");
        }

        if (!context.contains(variableName)) {
            throw new RuntimeException("Scenario variable not found: " + variableName);
        }

        String value = context.getScenarioVariable(variableName);

        if (value == null) {
            throw new RuntimeException("Scenario variable not found: " + variableName);
        }

        return value;
    }

    public static String saveResponseFieldAsVariable(TestContext context,
                                                     String fieldKey,
                                                     String variableName) {

        return saveResponseFieldAsVariable(context, fieldKey, variableName, false);
    }

    public static String saveResponseFieldAsVariable(TestContext context,
                                                     String fieldKey,
                                                     String variableName,
                                                     boolean allowNull) {

        Object extractedValue = extractResponseField(context, fieldKey, allowNull);
        String resolvedValue = extractedValue == null ? null : String.valueOf(extractedValue);
        String maskedValue = SensitiveDataMasker.mask(variableName, resolvedValue);

        context.saveScenarioVariable(variableName, resolvedValue);

        ConsoleLogger.info("Extracted Variable", variableName + " = " + maskedValue);
        AllureUtils.attachRequest("Extracted Variable", variableName + " = " + maskedValue);

        return resolvedValue;
    }

    public static Object extractResponseField(TestContext context,
                                              String fieldKey,
                                              boolean allowNull) {

        requireResponse(context, fieldKey);
        JsonPath jsonPath = requireJsonPath(context, fieldKey);
        Object extractedValue = jsonPath.get(fieldKey);

        if (extractedValue == null && !allowNull) {
            throw new RuntimeException("Response field not found or null: " + fieldKey);
        }

        return extractedValue;
    }

    public static Object extractResponseField(TestContext context, String fieldKey) {

        return extractResponseField(context, fieldKey, false);
    }

    public static boolean contains(TestContext context, String key) {

        return context != null && context.contains(key);
    }

    private static void requireResponse(TestContext context, String fieldKey) {

        if (context == null) {
            throw new RuntimeException("Scenario context is not available.");
        }

        if (context.getResponse() == null) {
            throw new RuntimeException("Cannot extract response field \"" + fieldKey + "\" because response is null.");
        }
    }

    private static JsonPath requireJsonPath(TestContext context, String fieldKey) {

        if (context.getJsonPath() == null) {
            throw new RuntimeException("Cannot extract response field \"" + fieldKey + "\" because json path is null.");
        }

        return context.getJsonPath();
    }
}
