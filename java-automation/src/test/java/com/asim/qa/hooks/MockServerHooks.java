package com.asim.qa.hooks;

import com.asim.qa.config.ConfigReader;
import com.github.tomakehurst.wiremock.WireMockServer;
import io.cucumber.java.AfterAll;
import io.cucumber.java.BeforeAll;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

public class MockServerHooks {

    private static WireMockServer wireMockServer;

    @BeforeAll
    public static void startMockServer() {
        if (!"mock".equals(ConfigReader.getActiveEnv())) {
            System.out.println("Mock Server skipped for env: " + ConfigReader.getActiveEnv());
            return;
        }

        wireMockServer = new WireMockServer(
                options()
                        .port(9090)
                        .usingFilesUnderDirectory("../mock-server")
        );

        wireMockServer.start();

        System.out.println("✅ Mock Server started on port 9090");
    }

    @AfterAll
    public static void stopMockServer() {
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.stop();
            System.out.println("🛑 Mock Server stopped");
        }
    }
}
