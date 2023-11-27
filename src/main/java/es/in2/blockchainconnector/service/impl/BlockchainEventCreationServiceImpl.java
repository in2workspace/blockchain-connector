package es.in2.blockchainconnector.service.impl;

import es.in2.blockchainconnector.configuration.properties.BrokerProperties;
import es.in2.blockchainconnector.configuration.properties.OperatorProperties;
import es.in2.blockchainconnector.domain.*;
import es.in2.blockchainconnector.exception.HashLinkException;
import es.in2.blockchainconnector.service.BlockchainEventCreationService;
import es.in2.blockchainconnector.service.TransactionService;
import es.in2.blockchainconnector.utils.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.net.http.HttpResponse;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static es.in2.blockchainconnector.utils.Utils.HASHLINK_PREFIX;
import static es.in2.blockchainconnector.utils.Utils.getRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlockchainEventCreationServiceImpl implements BlockchainEventCreationService {

    private final OperatorProperties operatorProperties;
    private final BrokerProperties brokerProperties;
    private final TransactionService transactionService;

    @Override
    public Mono<OnChainEvent> createBlockchainEvent(OnChainEventDTO onChainEventDTO, String processId) {
        return Mono.fromCallable(() -> {
            try {
                log.debug("ProcessID: {} - Creating blockchain event...", processId);
                String entityHashed;
                if (onChainEventDTO.dataMap().containsKey("deletedAt")) {
                    // Calculate SHA-256 hash from origin data
                    entityHashed = Utils.calculateSHA256Hash(getRequest
                            (brokerProperties.internalDomain() + brokerProperties.paths().entities() + "/" + onChainEventDTO.id())
                            .thenApply(HttpResponse::body).join());
                } else {
                    // Calculate SHA-256 hash from the data
                    entityHashed = Utils.calculateSHA256Hash(onChainEventDTO.data());
                }
                // Build dynamic URL by Broker Entity Use Case
                String brokerEntityUrl = brokerProperties.internalDomain() + brokerProperties.paths().entities();
                // Create DataLocation parameter (Hashlink)
                String dataLocation = brokerEntityUrl + "/" + onChainEventDTO.id() + HASHLINK_PREFIX + entityHashed;
                // Build OnChainEvent
                OnChainEvent onChainEvent = OnChainEvent.builder()
                        .eventType(onChainEventDTO.eventType())
                        .organizationId(Utils.calculateSHA256Hash(operatorProperties.organizationId()))
                        .dataLocation(dataLocation)
                        .metadata(List.of())
                        .build();
                log.debug("ProcessID: {} - OnChainEvent created: {}", processId, onChainEvent);
                return onChainEvent;
            } catch (NoSuchAlgorithmException e) {
                log.error("ProcessID: {} - Error creating blockchain event: {}", processId, e.getMessage());
                throw new HashLinkException("Error creating blockchain event", e.getCause());
            }
        }).flatMap(onChainEvent -> {
            Transaction transaction;
            try {
                transaction = Transaction.builder()
                        .id(UUID.randomUUID())
                        .transactionId(processId)
                        .createdAt(Timestamp.from(Instant.now()))
                        .dataLocation(onChainEvent.dataLocation())
                        .entityId(onChainEventDTO.id())
                        .entityHash(Utils.calculateSHA256Hash(onChainEventDTO.data()))
                        .status(TransactionStatus.CREATED)
                        .trader(TransactionTrader.PRODUCER)
                        .hash("")
                        .newTransaction(true)
                        .build();
            } catch (NoSuchAlgorithmException e) {
                return Mono.error(new HashLinkException("Error calculating hash"));
            }
            return transactionService.saveTransaction(transaction).thenReturn(onChainEvent);
                }).onErrorMap(NoSuchAlgorithmException.class, e -> new HashLinkException("Error creating blockchain event", e.getCause()));
    }

}
