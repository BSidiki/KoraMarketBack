package com.koramarket.common.utils;

import java.util.UUID;

public class IdUtils {
    public static String randomUuid() {
        return UUID.randomUUID().toString();
    }

    // Si tu veux un id court pour l’UI, par exemple
    public static String shortId() {
        return randomUuid().split("-")[0];
    }
}
