package es.in2.blockchainconnector.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.in2.blockchainconnector.configuration.properties.BrokerProperties;
import es.in2.blockchainconnector.domain.*;
import es.in2.blockchainconnector.exception.BrokerNotificationParserException;
import es.in2.blockchainconnector.exception.HashCreationException;
import es.in2.blockchainconnector.service.BrokerAdapterNotificationService;
import es.in2.blockchainconnector.service.TransactionService;
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
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static es.in2.blockchainconnector.utils.HttpUtils.getRequest;
import static es.in2.blockchainconnector.utils.Utils.calculateSHA256Hash;

@Slf4j
@Service
@RequiredArgsConstructor
public class BrokerAdapterNotificationServiceImpl implements BrokerAdapterNotificationService {

    private final ObjectMapper objectMapper;
    private final TransactionService transactionService;
    private final BrokerProperties brokerProperties;

    @Override
    public Mono<OnChainEventDTO> processNotification(BrokerNotification brokerNotification) {
        if (brokerNotification == null || brokerNotification.data().isEmpty()) {
            return Mono.error(new IllegalArgumentException("Invalid BrokerNotificationDTO"));
        }

        Map<String, Object> dataMap = brokerNotification.data().get(0);
        if (dataMap == null || dataMap.get("id") == null) {
            return Mono.error(new IllegalArgumentException("Invalid dataMap in BrokerNotificationDTO"));
        }

        String processId = MDC.get("processId");
        String id = dataMap.get("id").toString();

        return transactionService.getTransaction(id)
                .doOnNext(transactions -> log.debug("ProcessID: {} - Transactions: {}", processId, transactions))
                .flatMap(previousTransaction -> processBasedOnPreviousTransaction(dataMap, previousTransaction, processId));
    }

    private Mono<OnChainEventDTO> processBasedOnPreviousTransaction(Map<String, Object> dataMap, List<Transaction> previousTransaction, String processId) {
        String dataToPersist;
        log.debug("ProcessID: {} - Previous transaction: {}", processId, previousTransaction);
        try {
            dataToPersist = objectMapper.writeValueAsString(dataMap);
        } catch (JsonProcessingException e) {
            log.error("ProcessID: {} - Error processing JSON: {}", processId, e.getMessage());
            return Mono.error(new BrokerNotificationParserException("Error processing JSON", e));
        }
        String hashedEntity;
        try {
            hashedEntity = calculateSHA256Hash(getRequest
                    (brokerProperties.internalDomain() + brokerProperties.paths().entities() + "/" + dataMap.get("id"))
                    .thenApply(HttpResponse::body).join());
        } catch (NoSuchAlgorithmException e) {
            throw new HashCreationException("Error calculating hash");
        }
        if (previousTransaction.isEmpty()) {
            log.debug("ProcessID: {} - new transaction", processId);
            return createAndSaveTransaction(dataMap, processId, dataToPersist);
        }
        else if (dataMap.containsKey("deletedAt")) {
            if (previousTransaction.get(previousTransaction.size() -1).getStatus() == TransactionStatus.DELETED) {
                log.debug("ProcessID: {} - Transaction already deleted", processId);
                return Mono.empty();
            }
            log.debug("ProcessID: {} - Creating deleted transaction", processId);
            return createAndSaveDeletedTransaction(dataMap, previousTransaction.get(previousTransaction.size() -1), processId, dataToPersist);
        } else if (Objects.equals(previousTransaction.get(previousTransaction.size() -1).getEntityHash(), hashedEntity)) {
            log.debug("ProcessID: {} - Entity hash matches previous transaction", processId);
            return Mono.empty();
        } else if (previousTransaction.get(previousTransaction.size() - 1).getStatus() == TransactionStatus.DELETED) {
            log.debug("ProcessID: {} - Transaction already deleted", processId);
            return createAndSaveTransaction(dataMap, processId, dataToPersist);
        }
        else {
            log.debug("ProcessID: {} - Update entity detected", processId);
            return createAndSaveTransaction(dataMap, processId, dataToPersist);
        }
    }
    private Mono<OnChainEventDTO> createAndSaveTransaction(Map<String, Object> dataMap, String processId, String dataToPersist) {
        String id = dataMap.get("id").toString();
        OnChainEventDTO onChainEventDTO = OnChainEventDTO.builder()
                .id(id)
                .eventType(dataMap.get("type").toString())
                .dataMap(dataMap)
                .data(dataToPersist)
                .build();

        String dataToPersistHash;
        try {
            dataToPersistHash = calculateSHA256Hash(getRequest
                    (brokerProperties.internalDomain() + brokerProperties.paths().entities() + "/" + dataMap.get("id"))
                    .thenApply(HttpResponse::body).join());
        } catch (NoSuchAlgorithmException e) {
            throw new HashCreationException("Error calculating hash");
        }

        Transaction transaction = Transaction.builder()
                .id(UUID.randomUUID())
                .transactionId(processId)
                .createdAt(Timestamp.from(Instant.now()))
                .dataLocation("")
                .entityId(id)
                .entityType(dataMap.get("type").toString())
                .entityHash(dataToPersistHash)
                .status(TransactionStatus.RECEIVED)
                .trader(TransactionTrader.PRODUCER)
                .hash("")
                .newTransaction(true)
                .build();

        return transactionService.saveTransaction(transaction)
                .thenReturn(onChainEventDTO);
    }

    private Mono<OnChainEventDTO> createAndSaveDeletedTransaction(Map<String, Object> dataMap, Transaction previousTransaction, String processId, String dataToPersist) {
        String id = dataMap.get("id").toString();
        OnChainEventDTO onChainEventDTO = OnChainEventDTO.builder()
                .id(id)
                .eventType(previousTransaction.getEntityType())
                .dataMap(dataMap)
                .data(dataToPersist)
                .build();

        Transaction transaction = Transaction.builder()
                .id(UUID.randomUUID())
                .transactionId(processId)
                .createdAt(Timestamp.from(Instant.now()))
                .dataLocation("")
                .entityId(id)
                .entityType(previousTransaction.getEntityType())
                .entityHash("")
                .status(TransactionStatus.RECEIVED)
                .trader(TransactionTrader.PRODUCER)
                .hash("")
                .newTransaction(true)
                .build();

        return transactionService.saveTransaction(transaction)
                .thenReturn(onChainEventDTO);
    }


}
