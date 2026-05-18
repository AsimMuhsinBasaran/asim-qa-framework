package com.asim.qa.utils;

import com.asim.qa.config.ConfigReader;
import io.qameta.allure.Allure;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class AllureUtils {

    public static void attachRequest(String title, String body) {

        Allure.addAttachment(title, body == null ? "[no body]" : SensitiveDataMasker.mask(body));
    }

    public static void attachResponse(String title, String body) {

        Allure.addAttachment(title, body == null ? "[no body]" : SensitiveDataMasker.mask(body));
    }

    public static void attachPollingSummary(String body) {

        Allure.addAttachment("Polling Summary", body == null ? "[no summary]" : SensitiveDataMasker.mask(body));
    }

    public static void writeEnvironmentProperties(Path allureResultsDir) {

        List<String> lines = new ArrayList<>();
        lines.add("Environment=" + ConfigReader.getActiveEnv());
        lines.add("BaseUrl=" + ConfigReader.getBaseUrl());
        lines.add("JavaVersion=" + System.getProperty("java.version", "unknown"));
        lines.add("OS=" + System.getProperty("os.name", "unknown"));
        lines.add("RunnerClass=" + System.getProperty("runner.class", "ApiTestRunner"));

        String cucumberTags = System.getProperty("cucumber.filter.tags");
        if (cucumberTags != null && !cucumberTags.isBlank()) {
            lines.add("CucumberTags=" + cucumberTags.trim());
        }

        Path environmentFile = allureResultsDir.resolve("environment.properties");

        try {
            Files.write(environmentFile, lines);
        } catch (IOException e) {
            throw new RuntimeException("Allure environment.properties yazılamadı: " + environmentFile, e);
        }
    }
}
