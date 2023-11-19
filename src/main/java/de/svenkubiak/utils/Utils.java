package de.svenkubiak.utils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;
import java.net.Socket;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;

public final class Utils {
    @SuppressWarnings("rawtypes")
    private static final List SUCCESS_CODES;

    static {
        SUCCESS_CODES = List.of(200, 201, 202, 203, 204, 205, 206, 207, 208, 226);
    }

    private static final X509ExtendedTrustManager TRUST_MANAGER = new X509ExtendedTrustManager() {
        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[]{};
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) {
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket) {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket) {
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine engine) {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine engine) {
        }
    };

    public static SSLContext getSSLContext() {
        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{TRUST_MANAGER}, new SecureRandom());
        } catch (Exception e) {
            //Intentionally left blank
        }

        return sslContext;
    }

    public static boolean isSuccessCode(int statuscode) {
        return SUCCESS_CODES.contains(statuscode);
    }

    public static String getFormDataAsString(Map<String, String> formData) {
        StringBuffer buffer = new StringBuffer();
        for (Map.Entry<String, String> singleEntry : formData.entrySet()) {
            if (!buffer.isEmpty()) {
                buffer.append("&");
            }
            buffer.append(URLEncoder.encode(singleEntry.getKey(), StandardCharsets.UTF_8));
            buffer.append("=");
            buffer.append(URLEncoder.encode(singleEntry.getValue(), StandardCharsets.UTF_8));
        }

        return buffer.toString();
    }
}
