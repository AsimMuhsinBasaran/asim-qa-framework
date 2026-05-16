package com.asim.qa.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigReader {

    private static final Properties properties = new Properties();

    static {

        try (InputStream input =
                     ConfigReader.class
                             .getClassLoader()
                             .getResourceAsStream("config.properties")) {

            if (input == null) {
                throw new RuntimeException("config.properties bulunamadı!");
            }

            properties.load(input);

        } catch (IOException e) {
            throw new RuntimeException("Config dosyası yüklenemedi!", e);
        }
    }

    public static String get(String key) {
        return properties.getProperty(key);
    }

    public static String getBaseUrl() {

        String env = get("env");

        return get("base.url." + env);

    }
}