package no.nav.aap.api.util;

import org.slf4j.MDC;

import java.util.Optional;

public final class MDCUtil {

    public static final String NAV_CONSUMER_ID = "Nav-Consumer-Id";
    public static final String NAV_CONSUMER_TOKEN = "Nav-Consumer-Token";
    public static final String NAV_CALL_ID = "Nav-CallId";
    public static final String NAV_CALL_ID1 = "Nav-Call-Id";
    public static final String NAV_USER_ID = "Nav-User-Id";
    public static final String NAV_AKTØR_ID = "Nav-Aktør-Id";
    public static final String CALL_ID = "callId";
    public static final String X_CORRELATION_ID = "X-Correlation-ID";

    private MDCUtil() {
    }


    public static String callId() {
        return MDC.get(NAV_CALL_ID);
    }

    public static String consumerId() {
        return MDC.get(NAV_CONSUMER_ID);
    }

    public static void toMDC(String key, Object value) {
        Optional.ofNullable(value).ifPresent(v -> toMDC(key,v));
    }

    public static void toMDC(String key, String value) {
        toMDC(key, value, null);
    }

    public static void toMDC(String key, String value, String defaultValue) {
        MDC.put(key, Optional.ofNullable(value).orElse(defaultValue));
    }
}
