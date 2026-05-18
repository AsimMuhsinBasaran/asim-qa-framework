package com.asim.qa.hooks;

import com.asim.qa.utils.AllureUtils;
import io.cucumber.java.BeforeAll;

import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;

public class TestSuiteHooks {

    @BeforeAll
    public static void cleanAllureResults() {

        Path allurePath = Paths.get("target/allure-results");

        try {

            if (Files.exists(allurePath)) {

                Files.walk(allurePath)
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(file -> {
                            if (!file.delete()) {
                                System.out.println("Silinemedi: " + file.getAbsolutePath());
                            }
                        });
            }

            Files.createDirectories(allurePath);
            AllureUtils.writeEnvironmentProperties(allurePath);

            System.out.println("🧹 Allure results cleaned before test run");

        } catch (IOException e) {

            throw new RuntimeException("Allure results temizlenemedi", e);
        }
    }
}
