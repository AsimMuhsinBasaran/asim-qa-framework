package com.asim.qa.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Properties;

public class ConfigReader {

    private static final Properties properties = new Properties();
    private static final int DEFAULT_API_RETRY_MAX_ATTEMPTS = 3;
    private static final int DEFAULT_API_RETRY_DELAY_MS = 1000;
    private static final int DEFAULT_API_POLLING_MAX_ATTEMPTS = 3;
    private static final int DEFAULT_API_POLLING_DELAY_MS = 1000;

    static {

        loadProperties("config.properties", true);
        loadProperties("config/env/" + resolveActiveEnv() + ".properties", false);

    }

    private static void loadProperties(String resourcePath, boolean required) {

        try (InputStream input =
                     ConfigReader.class
                             .getClassLoader()
                             .getResourceAsStream(resourcePath)) {

            if (input == null) {
                if (required) {
                    throw new RuntimeException(resourcePath + " bulunamadı!");
                }

                throw new RuntimeException("Environment config bulunamadı: " + resourcePath);
            }

            properties.load(input);

        } catch (IOException e) {
            throw new UncheckedIOException("Config dosyası yüklenemedi: " + resourcePath, e);
        }
    }

    public static String get(String key) {

        String systemValue = System.getProperty(key);

        if (systemValue != null) {
            return systemValue;
        }

        return properties.getProperty(key);
    }

    public static String getBaseUrl() {

        String baseUrlKey = "base.url";
        String baseUrl = get(baseUrlKey);

        if (baseUrl == null || baseUrl.isBlank()) {
            throw new RuntimeException("Base URL bulunamadı: " + baseUrlKey);
        }

        return baseUrl;

    }

    public static String getActiveEnv() {
        return resolveActiveEnv();
    }

    private static String resolveActiveEnv() {

        String env = System.getProperty("env");

        if (env == null || env.isBlank()) {
            env = get("env");
        }

        if (env == null || env.isBlank()) {
            env = "mock";
        }

        return env.trim().toLowerCase();
    }

    public static int getApiTimeoutMs() {

        String timeout = get("api.timeout.ms");

        if (timeout == null || timeout.isBlank()) {
            throw new RuntimeException("api.timeout.ms config değeri bulunamadı!");
        }

        try {
            return Integer.parseInt(timeout);
        } catch (NumberFormatException e) {
            throw new RuntimeException("api.timeout.ms geçerli bir sayı olmalı: " + timeout, e);
        }
    }

    public static int getMockServerPort() {

        String portValue = get("mock.server.port");

        if (portValue == null || portValue.isBlank()) {
            return 9090;
        }

        try {
            return Integer.parseInt(portValue.trim());
        } catch (NumberFormatException e) {
            throw new RuntimeException("mock.server.port geçerli bir sayı olmalı: " + portValue, e);
        }
    }

    public static int getApiRetryMaxAttempts() {
        return getPositiveInt("api.retry.maxAttempts", DEFAULT_API_RETRY_MAX_ATTEMPTS);
    }

    public static int getApiRetryDelayMs() {
        return getZeroOrPositiveInt("api.retry.delayMs", DEFAULT_API_RETRY_DELAY_MS);
    }

    public static int getApiPollingMaxAttempts() {
        return getPositiveInt("api.polling.maxAttempts", DEFAULT_API_POLLING_MAX_ATTEMPTS);
    }

    public static int getApiPollingDelayMs() {
        return getZeroOrPositiveInt("api.polling.delayMs", DEFAULT_API_POLLING_DELAY_MS);
    }

    private static int getPositiveInt(String key, int defaultValue) {

        int value = getIntOrDefault(key, defaultValue);

        if (value <= 0) {
            throw new RuntimeException(key + " sıfırdan büyük olmalı: " + value);
        }

        return value;
    }

    private static int getZeroOrPositiveInt(String key, int defaultValue) {

        int value = getIntOrDefault(key, defaultValue);

        if (value < 0) {
            throw new RuntimeException(key + " sıfır veya daha büyük olmalı: " + value);
        }

        return value;
    }

    private static int getIntOrDefault(String key, int defaultValue) {

        String value = get(key);

        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new RuntimeException(key + " geçerli bir sayı olmalı: " + value, e);
        }
    }
}
