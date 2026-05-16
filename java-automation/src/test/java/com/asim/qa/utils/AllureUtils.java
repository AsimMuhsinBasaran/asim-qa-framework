package com.asim.qa.utils;

import io.qameta.allure.Allure;

public class AllureUtils {

    public static void attachRequest(String title, String body) {

        Allure.addAttachment(title, body == null ? "[no body]" : SensitiveDataMasker.mask(body));
    }

    public static void attachResponse(String title, String body) {

        Allure.addAttachment(title, body == null ? "[no body]" : SensitiveDataMasker.mask(body));
    }
}
