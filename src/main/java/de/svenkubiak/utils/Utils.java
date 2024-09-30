package de.svenkubiak.utils;

import de.svenkubiak.http.Failsafe;
import de.svenkubiak.http.Result;
import jdk.jfr.Frequency;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;
import java.net.Socket;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

public final class Utils {
    private static final Map<String, HttpClient> HTTP_CLIENTS = new ConcurrentHashMap<>(8, 0.9f, 1);
    private static final Map<String, Failsafe> FAILSAFES = new ConcurrentHashMap<>(8, 0.9f, 1);
    private static final Executor EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();
    private static final Pattern PATTERN = Pattern.compile("[^A-Za-z0-9 ]");
    @SuppressWarnings("rawtypes")
    private static final Set SUCCESS_CODES;
    static {
        SUCCESS_CODES = Set.of(200, 201, 202, 203, 204, 205, 206, 207, 208, 226);
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

    private Utils() {
    }

    private static SSLContext getSSLContext() {
        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{TRUST_MANAGER}, new SecureRandom());
        } catch (Exception e) {
            //Intentionally left blank
        }

        return sslContext;
    }

    public static boolean isSuccessCode(int statusCode) {
        return SUCCESS_CODES.contains(statusCode);
    }

    public static String getFormDataAsString(Map<String, String> formData) {
        var buffer = new StringBuilder();
        for (Map.Entry<String, String> singleEntry : formData.entrySet()) {
            if (!buffer.isEmpty()) {
                buffer.append('&');
            }
            buffer.append(URLEncoder.encode(singleEntry.getKey(), StandardCharsets.UTF_8))
                .append('=')
                .append(URLEncoder.encode(singleEntry.getValue(), StandardCharsets.UTF_8));
        }

        return buffer.toString();
    }

    public static HttpClient getHttpClient(boolean followRedirects, boolean disableValidation) {
        var key = String.valueOf(followRedirects) + String.valueOf(disableValidation);

        var httpClient = HTTP_CLIENTS.get(key);
        if (httpClient == null || httpClient.isTerminated()) {
            var clientBuilder = HttpClient.newBuilder().executor(EXECUTOR);

            if (followRedirects) {
                clientBuilder.followRedirects(HttpClient.Redirect.ALWAYS);
            }

            if (disableValidation) {
                clientBuilder.sslContext(Utils.getSSLContext());
            }

            httpClient = clientBuilder.build();
            HTTP_CLIENTS.put(key, httpClient);
        }

        return httpClient;
    }

    public static String clean(String string) {
        return PATTERN.matcher(string).replaceAll("");
    }

    public static Failsafe getFailsafe(String url, int threshold, Duration delay) {
        Objects.requireNonNull(url, "url must not be null");

        if (delay == null) {
            return null;
        }

        Failsafe failsafe = FAILSAFES.get(url);
        if (failsafe == null) {
            failsafe = Failsafe.of(threshold, delay);
        }

        return failsafe;
    }

    public static void setFailsafe(String url, Failsafe failsafe, Result result) {
        Objects.requireNonNull(url, "url must not be null");
        Objects.requireNonNull(failsafe, "failsafe must not be null");
        Objects.requireNonNull(result, "result must not be null");

        System.out.println("seeting failsafe");

        if (result.isValid()) {
            failsafe.success();
        } else {
            failsafe.error();
        }

        FAILSAFES.put(url, failsafe);
    }
}
