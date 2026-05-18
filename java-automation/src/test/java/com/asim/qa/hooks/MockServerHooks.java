package com.asim.qa.hooks;

import com.asim.qa.config.ConfigReader;
import com.github.tomakehurst.wiremock.WireMockServer;
import io.cucumber.java.AfterAll;
import io.cucumber.java.BeforeAll;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

public class MockServerHooks {

    private static WireMockServer wireMockServer;

    @BeforeAll
    public static synchronized void startMockServer() {
        if (!"mock".equals(ConfigReader.getActiveEnv())) {
            System.out.println("Mock Server skipped for env: " + ConfigReader.getActiveEnv());
            return;
        }

        if (wireMockServer != null && wireMockServer.isRunning()) {
            System.out.println("✅ Mock Server already running on port " + ConfigReader.getMockServerPort());
            return;
        }

        int port = ConfigReader.getMockServerPort();
        WireMockServer server = new WireMockServer(
                options()
                        .port(port)
                        .usingFilesUnderDirectory("../mock-server")
        );

        try {
            server.start();
            wireMockServer = server;
            System.out.println("✅ Mock Server started on port " + port);
        } catch (RuntimeException e) {
            throw new RuntimeException(
                    "Mock Server could not start on port " + port
                            + " for env " + ConfigReader.getActiveEnv()
                            + ". Another test run or process may already be using this port.",
                    e
            );
        }
    }

    @AfterAll
    public static synchronized void stopMockServer() {
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.stop();
            System.out.println("🛑 Mock Server stopped");
        }

        wireMockServer = null;
    }
}
