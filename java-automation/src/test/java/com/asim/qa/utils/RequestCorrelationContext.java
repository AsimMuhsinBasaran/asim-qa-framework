package com.asim.qa.utils;

import java.util.concurrent.atomic.AtomicLong;

public final class RequestCorrelationContext {

    private static final AtomicLong COUNTER = new AtomicLong();
    private static final ThreadLocal<String> REQUEST_ID = new ThreadLocal<>();

    private RequestCorrelationContext() {
    }

    public static String startRequest() {

        String requestId = String.format("REQ-%06d", COUNTER.incrementAndGet());
        REQUEST_ID.set(requestId);
        return requestId;
    }

    public static String getRequestId() {

        return REQUEST_ID.get();
    }

    public static void clear() {

        REQUEST_ID.remove();
    }
}
