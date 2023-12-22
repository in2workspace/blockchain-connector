package es.in2.blockchainconnector.configuration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.in2.blockchainconnector.configuration.properties.BlockchainProperties;
import es.in2.blockchainconnector.configuration.properties.DLTAdapterProperties;
import es.in2.blockchainconnector.domain.BlockchainNodeDTO;
import es.in2.blockchainconnector.domain.BlockchainNodeSubscriptionDTO;
import es.in2.blockchainconnector.exception.BlockchainNodeSubscriptionException;
import es.in2.blockchainconnector.exception.RequestErrorException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

import static es.in2.blockchainconnector.utils.Utils.APPLICATION_JSON;
import static es.in2.blockchainconnector.utils.Utils.CONTENT_TYPE;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DLTAdapterConfig {

    private final ObjectMapper objectMapper;
    private final BlockchainProperties blockchainProperties;
    private final DLTAdapterProperties dltAdapterProperties;
    private final ApplicationConfig applicationConfig;

    @Bean
    public CookieManager cookieManager() {
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        return cookieManager;
    }

    @Bean
    public HttpClient dltAdapterHttpClient() {
        // Create a single HttpClient instance with a CookieManager to manage cookies
        return HttpClient.newBuilder()
                .cookieHandler(cookieManager())
                .build();
    }

    @Bean
    public void setBlockchainNodeConfiguration() {
        try {
            createBlockchainNodeConfiguration();
        } catch (Exception e) {
            log.error("Error creating default subscription", e);
            Thread.currentThread().interrupt();
        }
    }

    private void createBlockchainNodeConfiguration() {
        String processId = UUID.randomUUID().toString();
        log.info("ProcessID: {} - Creating blockchain node configuration...", processId);
        try {
            String url = dltAdapterProperties.domain() + dltAdapterProperties.paths().configureNode();
            log.debug("ProcessID: {} - Blockchain Node Configuration url: {}", processId, url);
            BlockchainNodeDTO blockchainNodeDTO = new BlockchainNodeDTO(
                    blockchainProperties.rpcAddress(),
                    blockchainProperties.userEthereumAddress(),
                    applicationConfig.organizationIdHash());
            String body = objectMapper.writer().writeValueAsString(blockchainNodeDTO);
            log.debug("ProcessID: {} - Blockchain Node Configuration: {}", processId, body);
            requestCall(processId, URI.create(url), body);
            log.info("ProcessID: {} - Blockchain node configuration created.", processId);
        } catch (JsonProcessingException e) {
            throw new BlockchainNodeSubscriptionException("Error creating blockchain node configuration: " + e.getMessage());
        }
    }

    @Bean
    public void setEventsSubscription() {
        if (blockchainProperties.subscription().active()) {
            try {
                createEventSubscriptionToDLTAdapter();
            } catch (Exception e) {
                log.error("Error creating default subscription", e);
                Thread.currentThread().interrupt();
            }
        }
    }

    private void createEventSubscriptionToDLTAdapter() {
        String processId = UUID.randomUUID().toString();
        log.info("ProcessID: {} - Creating default subscription to blockchain node interface...", processId);
        try {
            String url = dltAdapterProperties.domain() + dltAdapterProperties.paths().subscribe();
            log.debug("ProcessID: {} - Blockchain Node I/F Subscription url: {}", processId, url);
            BlockchainNodeSubscriptionDTO blockchainNodeSubscriptionDTO = BlockchainNodeSubscriptionDTO.builder()
                    .eventTypeList(blockchainProperties.subscription().eventTypes())
                    .notificationEndpoint(blockchainProperties.subscription().notificationEndpoint())
                    .build();
            String body = objectMapper.writer().writeValueAsString(blockchainNodeSubscriptionDTO);
            log.debug("ProcessID: {} - Blockchain Node I/F Subscription body: {}", processId, body);
            requestCall(processId, URI.create(url), body);
            log.info("ProcessID: {} - Subscriptions configuration created.", processId);
        } catch (JsonProcessingException e) {
            throw new BlockchainNodeSubscriptionException("Error creating default subscription: " + e.getMessage());
        }
    }

    private void requestCall(String processId, URI url, String body) {
        HttpClient httpClient = dltAdapterHttpClient();
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .header(CONTENT_TYPE, APPLICATION_JSON)
                .build();
        HttpResponse<String> response;
        try {
            response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            log.debug("ProcessID: {} - Response: {}", processId, response.statusCode() + ": " + response.body());
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RequestErrorException("Error sending request to blockchain node: " + e.getMessage());
        }
        if (response.statusCode() != 200) {
            throw new RequestErrorException("Error sending request to blockchain node");
        }
    }

}
