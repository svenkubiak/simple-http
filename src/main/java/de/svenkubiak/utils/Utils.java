package de.svenkubiak.utils;

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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class Utils {
    private static final Map<String, HttpClient> HTTP_CLIENTS = new ConcurrentHashMap<>(16, 0.9f, 1);

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

    public static HttpClient getHttpClient(String url, HttpClient.Version version, boolean followRedirects, boolean disableValidation) {
        Objects.requireNonNull(url, "url can not be null");
        Objects.requireNonNull(version, "version can not be null");

        String key = url.toLowerCase(Locale.ENGLISH) + version + followRedirects + disableValidation;

        HttpClient httpClient = HTTP_CLIENTS.get(key);
        if (httpClient == null) {
            HttpClient.Builder clientBuilder = HttpClient.newBuilder();
            clientBuilder.version(version);

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
}
