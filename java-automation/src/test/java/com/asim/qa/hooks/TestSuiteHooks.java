package com.asim.qa.hooks;

import com.asim.qa.utils.AllureUtils;
import io.cucumber.java.BeforeAll;

import java.io.IOException;
import java.nio.file.*;

public class TestSuiteHooks {

    @BeforeAll
    public static void prepareAllureResults() {

        Path allurePath = Paths.get("target/allure-results");

        try {
            Files.createDirectories(allurePath);
            AllureUtils.writeEnvironmentProperties(allurePath);

            System.out.println("Allure results directory prepared");

        } catch (IOException e) {

            throw new RuntimeException("Allure results hazirlanamadi", e);
        }
    }
}
