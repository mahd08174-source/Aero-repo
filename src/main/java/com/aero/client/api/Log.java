package com.aero.client.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Log {
    private static final Logger L = LoggerFactory.getLogger("AeroClient");
    private static long lastErr;
    public static void info(String s) { L.info(s); }
    public static void error(String s, Throwable t) {
        long now = System.currentTimeMillis();
        if (now - lastErr > 5000) { lastErr = now; L.error(s, t); }
    }
    private Log() {}
}
