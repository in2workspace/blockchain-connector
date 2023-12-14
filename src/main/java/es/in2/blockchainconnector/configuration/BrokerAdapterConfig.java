package es.in2.blockchainconnector.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.in2.blockchainconnector.configuration.properties.BrokerAdapterProperties;
import es.in2.blockchainconnector.configuration.properties.NgsiLdSubscriptionConfigProperties;
import es.in2.blockchainconnector.domain.BrokerSubscriptionRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.naming.CommunicationException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static es.in2.blockchainconnector.utils.Utils.*;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class BrokerAdapterConfig {

    private final ObjectMapper objectMapper;
    private final BrokerAdapterProperties brokerAdapterProperties;
    private final NgsiLdSubscriptionConfigProperties subscriptionConfiguration;

    @Bean
    @Profile("default")
    public void setBrokerSubscription() {
        String processId = UUID.randomUUID().toString();
        log.info("ProcessID: {} - Setting Orion-LD Entities subscription...", processId);
        BrokerSubscriptionRequest brokerSubscriptionRequest = BrokerSubscriptionRequest.builder()
                .id("urn:ngsi-ld:Subscription:" + UUID.randomUUID())
                .type("Subscription")
                .notificationEndpointUri(subscriptionConfiguration.notificationEndpoint())
                .entities(subscriptionConfiguration.entityTypes())
                .build();
        log.debug("ProcessID: {} - Broker Subscription: {}", processId, brokerSubscriptionRequest.toString());
        try {
            String orionLdInterfaceUrl = brokerAdapterProperties.domain() + brokerAdapterProperties.paths().subscriptions();
            log.debug("ProcessID: {} - Broker Subscription URL: {}", processId, orionLdInterfaceUrl);
            String requestBody = objectMapper.writer().writeValueAsString(brokerSubscriptionRequest);
            log.debug("ProcessID: {} - Broker Subscription request body: {}", processId, requestBody);
            // Create request
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(orionLdInterfaceUrl))
                    .headers(CONTENT_TYPE, APPLICATION_JSON,
                            ACCEPT_HEADER, APPLICATION_JSON)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();
            // Send request asynchronously
            CompletableFuture<HttpResponse<String>> response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
            // Verify Response HttpStatus
            if (response.get().statusCode() != 201) {
                throw new CommunicationException("Error creating default subscription");
            }
            log.info("ProcessID: {} - Broker Entities subscription created successfully.", processId);
        } catch (CommunicationException | InterruptedException | IOException | ExecutionException e) {
            log.error("ProcessID: {} - Error creating default subscription", processId, e);
            Thread.currentThread().interrupt();
        }
    }

}
