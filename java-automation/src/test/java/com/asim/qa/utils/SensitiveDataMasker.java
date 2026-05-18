package com.asim.qa.utils;

import java.util.regex.Pattern;

public final class SensitiveDataMasker {

    private static final String MASK = "***MASKED***";
    private static final String SENSITIVE_KEYS = "password|passwd|pwd|token|access_token|refresh_token|id_token|apiKey|api_key|clientSecret|client_secret|secret";

    private static final Pattern AUTHORIZATION_HEADER_PATTERN = Pattern.compile(
            "(?i)(\\bAuthorization\\s*[:=]\\s*Bearer\\s+)([^,}\\]\\s]+)"
    );
    private static final Pattern BASIC_AUTHORIZATION_HEADER_PATTERN = Pattern.compile(
            "(?i)(\\bAuthorization\\s*[:=]\\s*Basic\\s+)([^,}\\]\\s]+)"
    );
    private static final Pattern BEARER_PATTERN = Pattern.compile(
            "(?i)(\\bBearer\\s+)([^,}\\]\\s]+)"
    );
    private static final Pattern JSON_STRING_FIELD_PATTERN = Pattern.compile(
            "(?i)(\"(?:" + SENSITIVE_KEYS + ")\"\\s*:\\s*\")([^\"]*)(\")"
    );
    private static final Pattern JSON_NON_STRING_FIELD_PATTERN = Pattern.compile(
            "(?i)(\"(?:" + SENSITIVE_KEYS + ")\"\\s*:\\s*)([^,}\\]\\s\"]+)"
    );
    private static final Pattern KEY_VALUE_PATTERN = Pattern.compile(
            "(?i)((?<!\")\\b[A-Za-z0-9_.-]*(?:" + SENSITIVE_KEYS + ")[A-Za-z0-9_.-]*\\s*[:=]\\s*)(\"?)([^,}\\]\\s\"]+)(\"?)"
    );
    private static final Pattern SENSITIVE_KEY_PATTERN = Pattern.compile(
            "(?i).*(?:" + SENSITIVE_KEYS + ").*"
    );

    private SensitiveDataMasker() {
    }

    public static String mask(String input) {

        if (input == null) {
            return null;
        }

        String masked = maskPattern(input, AUTHORIZATION_HEADER_PATTERN, "$1" + MASK);
        masked = maskPattern(masked, BASIC_AUTHORIZATION_HEADER_PATTERN, "$1" + MASK);
        masked = maskPattern(masked, BEARER_PATTERN, "$1" + MASK);
        masked = maskPattern(masked, JSON_STRING_FIELD_PATTERN, "$1" + MASK + "$3");
        masked = maskPattern(masked, JSON_NON_STRING_FIELD_PATTERN, "$1\"" + MASK + "\"");
        masked = maskPattern(masked, KEY_VALUE_PATTERN, "$1$2" + MASK + "$4");

        return masked;
    }

    public static String mask(Object input) {

        return input == null ? null : mask(String.valueOf(input));
    }

    public static String mask(String key, Object value) {

        if (value == null) {
            return null;
        }

        return isSensitiveKey(key) ? MASK : mask(value);
    }

    public static boolean isSensitiveKey(String key) {

        return key != null && SENSITIVE_KEY_PATTERN.matcher(key).matches();
    }

    private static String maskPattern(String input, Pattern pattern, String replacement) {

        return pattern.matcher(input).replaceAll(replacement);
    }
}
