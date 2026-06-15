package com.asim.qa.data;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

public class TestDataFactory {

    private static final DateTimeFormatter RUN_ID_TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final String PUBLIC_TEST_EMAIL_DOMAIN = "example.test";
    private static final AtomicLong SEQUENCE = new AtomicLong();

    private final Clock clock;

    public TestDataFactory() {

        this(Clock.systemUTC());
    }

    public TestDataFactory(Clock clock) {

        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    public String runId() {

        return safeId("QA");
    }

    public String externalRef() {

        return safeId("QA-REF");
    }

    public String customerExternalRef() {

        return safeId("QA-CUSTOMER");
    }

    public String orderExternalRef() {

        return safeId("QA-ORDER");
    }

    public String email() {

        return ("qa+" + safeId("QA").toLowerCase(Locale.ROOT) + "@" + PUBLIC_TEST_EMAIL_DOMAIN)
                .toLowerCase(Locale.ROOT);
    }

    public String gsm() {

        long value = Math.floorMod(SEQUENCE.incrementAndGet(), 10_000_000L);
        return String.format(Locale.ROOT, "+999000%07d", value);
    }

    private String safeId(String prefix) {

        String timestamp = LocalDateTime.now(clock).format(RUN_ID_TIMESTAMP_FORMAT);
        long nextValue = SEQUENCE.incrementAndGet();

        return String.format(Locale.ROOT, "%s-%s-%06d", prefix, timestamp, nextValue);
    }
}
