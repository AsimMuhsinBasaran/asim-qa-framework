package com.asim.qa.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Properties;

public class ConfigReader {

    private static final Properties properties = new Properties();

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
}
