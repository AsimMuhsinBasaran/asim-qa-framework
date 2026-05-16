package com.asim.qa.hooks;

import com.asim.qa.utils.ConsoleLogger;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;

public class ScenarioHooks {

    @Before(order = 0)
    public void setLoggingContext(Scenario scenario) {

        ConsoleLogger.setScenarioName(scenario.getName());
    }

    @After
    public void clearLoggingContext() {

        ConsoleLogger.clearScenarioName();
    }
}
