package com.asim.qa.runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

@CucumberOptions(
        features = "src/test/resources/features",
        glue = {
                "com.asim.qa.stepdefinitions"
        },
        plugin = {
                "pretty",
                "html:target/dev-cucumber-report.html",
                "json:target/dev-cucumber.json",
                "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"
        },
        tags = "@dev",
        monochrome = true
)
public class DevApiTestRunner extends AbstractTestNGCucumberTests {
}