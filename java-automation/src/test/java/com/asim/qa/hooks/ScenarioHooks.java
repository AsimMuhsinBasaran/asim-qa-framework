package com.asim.qa.hooks;

import com.asim.qa.context.TestContext;
import com.asim.qa.utils.AllureUtils;
import com.asim.qa.utils.ConsoleLogger;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;

public class ScenarioHooks {

    private final TestContext context;

    public ScenarioHooks(TestContext context) {

        this.context = context;
    }

    @Before(order = 0)
    public void setLoggingContext(Scenario scenario) {

        ConsoleLogger.setScenarioName(scenario.getName());
    }

    @After
    public void clearLoggingContext(Scenario scenario) {

        if (scenario.isFailed()) {
            attachFailureDiagnostics();
        }

        ConsoleLogger.clearScenarioName();
    }

    private void attachFailureDiagnostics() {

        AllureUtils.attachApiFailureDiagnostics(context);
    }
}
