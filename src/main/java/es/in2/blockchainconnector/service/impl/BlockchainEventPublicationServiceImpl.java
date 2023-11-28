package es.in2.blockchainconnector.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.in2.blockchainconnector.configuration.DLTAdapterConfig;
import es.in2.blockchainconnector.configuration.properties.DLTAdapterProperties;
import es.in2.blockchainconnector.domain.OnChainEvent;
import es.in2.blockchainconnector.domain.Transaction;
import es.in2.blockchainconnector.domain.TransactionStatus;
import es.in2.blockchainconnector.domain.TransactionTrader;
import es.in2.blockchainconnector.exception.BrokerNotificationParserException;
import es.in2.blockchainconnector.exception.DLTAdapterCommunicationException;
import es.in2.blockchainconnector.service.BlockchainEventPublicationService;
import es.in2.blockchainconnector.service.TransactionService;
import es.in2.blockchainconnector.utils.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static es.in2.blockchainconnector.utils.Utils.APPLICATION_JSON;
import static es.in2.blockchainconnector.utils.Utils.CONTENT_TYPE;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlockchainEventPublicationServiceImpl implements BlockchainEventPublicationService {

    private final ObjectMapper objectMapper;
    private final DLTAdapterProperties dltAdapterProperties;
    private final DLTAdapterConfig dltAdapterConfig;
    private final TransactionService transactionService;

    @Override
    public Mono<Void> publishBlockchainEventIntoBlockchainNode(String processId, OnChainEvent onChainEvent) {
        return Mono.fromCallable(() -> {
                    try {
                        log.debug("ProcessID: {} - Publishing On-Chain Event into Blockchain Node...", processId);
                        URI uri = URI.create(dltAdapterProperties.domain() + dltAdapterProperties.paths().publish());
                        log.debug("ProcessID: {} - URI: {}", processId, uri);
                        String requestBody = objectMapper.writeValueAsString(onChainEvent);
                        log.debug("ProcessID: {} - Request Body: {}", processId, requestBody);
                        HttpRequest httpRequest = HttpRequest.newBuilder()
                                .uri(uri)
                                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                                .header(CONTENT_TYPE, APPLICATION_JSON)
                                .build();
                        CompletableFuture<HttpResponse<String>> responseFuture = dltAdapterConfig.dltAdapterHttpClient()
                                .sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString());
                        return Mono.fromFuture(() -> responseFuture);
                    } catch (JsonProcessingException e) {
                        log.error("ProcessID: {} - Error publishing On-Chain Event into Blockchain Node: {}", processId, e.getMessage());
                        throw new DLTAdapterCommunicationException("Failed to publish On-Chain Event into Blockchain Node", e);
                    }
                })
                .flatMap(response -> {
                    Transaction transaction = Transaction.builder()
                            .id(UUID.randomUUID())
                            .transactionId(processId)
                            .createdAt(Timestamp.from(Instant.now()))
                            .dataLocation(onChainEvent.dataLocation())
                            .entityId(Utils.extractEntityId(onChainEvent.dataLocation()))
                            .entityHash(extractHlValue(onChainEvent.dataLocation()))
                            .status(TransactionStatus.PUBLISHED)
                            .trader(TransactionTrader.PRODUCER)
                            .hash("")
                            .newTransaction(true)
                            .build();
                    log.debug(transaction.toString());
                    return transactionService.saveTransaction(transaction);
                })
                .doOnError(error -> log.error("Error publishing On-Chain Event into Blockchain Node: {}", error.getMessage(), error))
                .then();
    }

    public static String extractHlValue(String entityUrl) {
        try {
            URI uri = new URI(entityUrl);
            String query = uri.getQuery();
            String[] params = query.split("&");
            for (String param : params) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2 && "hl".equals(keyValue[0])) {
                    return URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                }
            }
        } catch (URISyntaxException e) {
            throw new BrokerNotificationParserException("Error while extracting hl value from datalocation");
        }
        return null;
    }

}
