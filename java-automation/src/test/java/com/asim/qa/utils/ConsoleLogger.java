package com.asim.qa.utils;

public class ConsoleLogger {

    public static void section(String title) {

        System.out.println("\n==================================================");
        System.out.println("🚀 " + title);
        System.out.println("==================================================");
    }

    public static void info(String key, Object value) {

        System.out.printf("🔹 %-20s : %s%n", key, SensitiveDataMasker.mask(value));
    }

    public static void assertion(String field, Object expected, Object actual) {

        info("Field", field);
        info("Expected", SensitiveDataMasker.mask(field, expected));
        info("Actual", SensitiveDataMasker.mask(field, actual));
    }

    public static void pass(String message) {

        System.out.println("✅ " + message);
    }

    public static void fail(String message) {

        System.out.println("❌ " + message);
    }

    public static void warn(String message) {

        System.out.println("⚠️  " + message);
    }

    public static void step(String message) {

        System.out.println("➡️  " + message);
    }

    public static void line() {

        System.out.println("--------------------------------------------------");
    }

    public static void duration(long startTimeMillis) {

        long duration = System.currentTimeMillis() - startTimeMillis;

        System.out.println("⏱️  Duration             : " + duration + " ms");
    }
}
