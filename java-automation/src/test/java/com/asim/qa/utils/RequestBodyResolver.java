package com.asim.qa.utils;

import com.asim.qa.context.TestContext;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class RequestBodyResolver {

    private static final Pattern REQUEST_BODY_PLACEHOLDER_PATTERN = Pattern.compile("\\{([A-Za-z0-9_.-]+)}");

    private RequestBodyResolver() {
    }

    public static String resolve(TestContext context, String requestBody) {

        if (requestBody == null || !requestBody.contains("{")) {
            return requestBody;
        }

        Matcher matcher = REQUEST_BODY_PLACEHOLDER_PATTERN.matcher(requestBody);
        StringBuffer resolvedBody = new StringBuffer();

        while (matcher.find()) {
            String resolvedValue = ScenarioContextUtils.resolveText(context, matcher.group());
            matcher.appendReplacement(resolvedBody, Matcher.quoteReplacement(resolvedValue));
        }

        matcher.appendTail(resolvedBody);

        return resolvedBody.toString();
    }
}
