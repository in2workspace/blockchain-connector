package es.in2.blockchainconnector.utils;

import es.in2.blockchainconnector.exception.BrokerNotificationParserException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class Utils {

    public static final String SHA_256_ALGORITHM = "SHA-256";
    public static final String HASHLINK_PREFIX = "?hl=";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String APPLICATION_JSON = "application/json";
    public static final String ACCEPT_HEADER = "Accept";

    public static boolean isNullOrBlank(String string) {
        return string == null || string.isBlank();
    }

    public static CompletableFuture<HttpResponse<String>> getRequest(String url) {
        // Create request
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).headers(ACCEPT_HEADER, APPLICATION_JSON).GET().build();
        // Send request asynchronously
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    public static CompletableFuture<HttpResponse<String>> deleteRequest(String url) {
        // Create DELETE request
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .headers(ACCEPT_HEADER, APPLICATION_JSON)
                .DELETE()
                .build();
        // Send request asynchronously
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    public static String calculateSHA256Hash(String data) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(SHA_256_ALGORITHM);
        byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(hash);
    }

    private static String bytesToHex(byte[] bytes) {
        Formatter formatter = new Formatter();
        for (byte b : bytes) {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }

    public static String postRequest(String url, String requestBody) {
        // Create request
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).headers(CONTENT_TYPE, APPLICATION_JSON, ACCEPT_HEADER, APPLICATION_JSON).POST(HttpRequest.BodyPublishers.ofString(requestBody)).build();
        // Send request asynchronously
        CompletableFuture<HttpResponse<String>> response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        return response.thenApply(HttpResponse::body).join();
    }

    public static String patchRequest(String url, String requestBody) {
        // Create request
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .headers(CONTENT_TYPE, APPLICATION_JSON, ACCEPT_HEADER, APPLICATION_JSON)
                .method("PATCH", HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        // Send request asynchronously
        CompletableFuture<HttpResponse<String>> response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        // Wait for the response and return the body
        return response.thenApply(HttpResponse::body).join();
    }

}