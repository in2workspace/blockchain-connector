package es.in2.blockchainconnector.utils;

import es.in2.blockchainconnector.exception.EntityAlreadyExistException;
import es.in2.blockchainconnector.exception.ForbiddenAccessException;
import es.in2.blockchainconnector.exception.UnauthorizedAccessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;

import static es.in2.blockchainconnector.utils.MessageUtils.*;

@Slf4j
@Component
public class HttpUtils {

    public static final String CONTENT_TYPE = "Content-Type";
    public static final String APPLICATION_JSON = "application/json";
    public static final String ACCEPT_HEADER = "Accept";
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

    public static String postRequest(String url, String requestBody) {
        // Create request
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).headers(CONTENT_TYPE, APPLICATION_JSON, ACCEPT_HEADER, APPLICATION_JSON).POST(HttpRequest.BodyPublishers.ofString(requestBody)).build();
        // Send request asynchronously
        CompletableFuture<HttpResponse<String>> response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        return response.thenApply(HttpResponse::body).join();
    }
    public static CompletableFuture<HttpResponse<String>> getRequest(String url) {
        // Create request
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).headers(ACCEPT_HEADER, APPLICATION_JSON).GET().build();
        // Send request asynchronously
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    public static String postDLTRequest(String url, String requestBody) {
        // Create request
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).headers(CONTENT_TYPE, APPLICATION_JSON).POST(HttpRequest.BodyPublishers.ofString(requestBody)).build();
        // Send request asynchronously
        CompletableFuture<HttpResponse<String>> response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        return response.thenApply(HttpResponse::body).join();
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
}
