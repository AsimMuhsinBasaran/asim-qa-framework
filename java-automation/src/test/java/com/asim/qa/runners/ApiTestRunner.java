package com.asim.qa.runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

@CucumberOptions(
        features = "src/test/resources/features",
        glue = {
                "com.asim.qa.stepdefinitions",
                "com.asim.qa.hooks"
        },
        plugin = {
                "pretty",
                "html:target/cucumber-report.html",
                "json:target/cucumber.json",
                "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"
        },
        monochrome = true
)
public class ApiTestRunner extends AbstractTestNGCucumberTests {

    @Override
    @DataProvider(parallel = true)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
