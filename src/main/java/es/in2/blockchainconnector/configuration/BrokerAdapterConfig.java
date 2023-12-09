package es.in2.blockchainconnector.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.in2.blockchainconnector.configuration.properties.BrokerAdapterProperties;
import es.in2.blockchainconnector.configuration.properties.NgsiLdSubscriptionConfigProperties;
import es.in2.blockchainconnector.domain.BrokerSubscriptionDTO;
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
        log.info(">>> Setting Orion-LD Entities subscription...");
        BrokerSubscriptionDTO brokerSubscriptionDTO = BrokerSubscriptionDTO.builder()
                .id("urn:ngsi-ld:Subscription:" + UUID.randomUUID())
                .type("Subscription")
                .notificationEndpointUri(subscriptionConfiguration.notificationEndpoint())
                .entities(subscriptionConfiguration.entityTypes())
                .build();
        log.debug(" > Orion-LD Subscription: {}", brokerSubscriptionDTO.toString());
        try {
            String orionLdInterfaceUrl = brokerAdapterProperties.domain() + brokerAdapterProperties.paths().subscriptions();
            log.debug(" > Orion-LD Subscription URL: {}", orionLdInterfaceUrl);
            String requestBody = objectMapper.writer().writeValueAsString(brokerSubscriptionDTO);
            log.debug(" > Orion-LD Subscription request body: {}", requestBody);
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
            log.info(" > Orion-LD Entities subscription created successfully.");
        } catch (CommunicationException | InterruptedException | IOException | ExecutionException e) {
            log.error("Error creating default subscription", e);
            Thread.currentThread().interrupt();
        }
    }

}
