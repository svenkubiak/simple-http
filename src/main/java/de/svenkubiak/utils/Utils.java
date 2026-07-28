package de.svenkubiak.utils;

import de.svenkubiak.http.Result;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

public final class Utils {
    public static final String FAILSAFE_ACTIVE_MESSAGE = "Failsafe is active; request was not sent";
    private static final Pattern PATTERN = Pattern.compile("[^A-Za-z0-9 ]");
    @SuppressWarnings("rawtypes")
    private static final Set SUCCESS_CODES;
    @SuppressWarnings("findsecbugs:WEAK_TRUST_MANAGER")
    private static final X509ExtendedTrustManager TRUST_MANAGER = new X509ExtendedTrustManager() {
        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[]{};
        }

        @Override
        @SuppressWarnings("java:S4830")
        public void checkClientTrusted(X509Certificate[] chain, String authType) {
        }

        @Override
        @SuppressWarnings("java:S4830")
        public void checkServerTrusted(X509Certificate[] chain, String authType) {
        }

        @Override
        @SuppressWarnings("java:S4830")
        public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket) {
        }

        @Override
        @SuppressWarnings("java:S4830")
        public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket) {
        }

        @Override
        @SuppressWarnings("java:S4830")
        public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine engine) {
        }

        @Override
        @SuppressWarnings("java:S4830")
        public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine engine) {
        }
    };

    static {
        SUCCESS_CODES = Set.of(200, 201, 202, 203, 204, 205, 206, 207, 208, 226);
    }

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

    /**
     * Applies trust-all TLS settings to a client builder.
     * Used by {@link de.svenkubiak.http.Http#disableAllHttpsValidations()}; not intended for direct use.
     */
    public static void applyDisableValidation(HttpClient.Builder builder) {
        builder.sslContext(getSSLContext());
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

    public static String clean(String string) {
        return PATTERN.matcher(string).replaceAll("");
    }

    public static URI toAllowedUri(String url) throws URISyntaxException {
        Objects.requireNonNull(url, "url must not be null");
        var uri = new URI(url);
        var scheme = uri.getScheme();
        if ((!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme))) {
            throw new URISyntaxException(url, "Only http and https URLs are allowed");
        }

        return uri;
    }

    public static Result blockedByFailsafe(Result result) {
        return result.withStatus(-1).withBody(FAILSAFE_ACTIVE_MESSAGE);
    }

    public static byte[] readLimited(InputStream inputStream, long maxBytes) throws IOException {
        byte[] buffer = new byte[8192];
        var output = new java.io.ByteArrayOutputStream();
        int read;

        while ((read = inputStream.read(buffer)) != -1) {
            if (output.size() + read > maxBytes) {
                throw new IOException("Response body exceeds maximum size of " + maxBytes + " bytes");
            }
            output.write(buffer, 0, read);
        }

        return output.toByteArray();
    }
}
