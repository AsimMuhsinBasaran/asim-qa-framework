package com.asim.qa.utils;

public class ConsoleLogger {

    private static final ThreadLocal<String> SCENARIO_NAME = new ThreadLocal<>();

    public static void setScenarioName(String scenarioName) {

        SCENARIO_NAME.set(scenarioName);
    }

    public static void clearScenarioName() {

        SCENARIO_NAME.remove();
    }

    public static void section(String title) {

        println("");
        println("==================================================");
        println("🚀 " + title);
        println("==================================================");
    }

    public static void info(String key, Object value) {

        println(String.format("🔹 %-20s : %s", key, SensitiveDataMasker.mask(value)));
    }

    public static void assertion(String field, Object expected, Object actual) {

        info("Field", field);
        info("Expected", SensitiveDataMasker.mask(field, expected));
        info("Actual", SensitiveDataMasker.mask(field, actual));
    }

    public static void pass(String message) {

        println("✅ " + message);
    }

    public static void fail(String message) {

        println("❌ " + message);
    }

    public static void warn(String message) {

        println("⚠️  " + message);
    }

    public static void step(String message) {

        println("➡️  " + message);
    }

    public static void line() {

        println("--------------------------------------------------");
    }

    public static void duration(long startTimeMillis) {

        long duration = System.currentTimeMillis() - startTimeMillis;

        println("⏱️  Duration             : " + duration + " ms");
    }

    public static void body(String body) {

        String maskedBody = SensitiveDataMasker.mask(body);

        if (maskedBody == null || maskedBody.isEmpty()) {
            println(maskedBody);
            return;
        }

        String[] lines = maskedBody.split("\\R", -1);

        for (String line : lines) {
            println(line);
        }
    }

    private static void println(String message) {

        System.out.println(prefix() + message);
    }

    private static String prefix() {

        Thread currentThread = Thread.currentThread();
        String threadName = currentThread.getName();
        String scenarioName = SCENARIO_NAME.get();

        if (scenarioName == null || scenarioName.isBlank()) {
            scenarioName = "unknown";
        }

        return "[thread=" + threadName + "][scenario=" + scenarioName + "] ";
    }
}
