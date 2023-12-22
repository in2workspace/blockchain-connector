package es.in2.blockchainconnector.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.in2.blockchainconnector.domain.DLTNotificationDTO;
import es.in2.blockchainconnector.domain.Transaction;
import es.in2.blockchainconnector.domain.TransactionStatus;
import es.in2.blockchainconnector.domain.TransactionTrader;
import es.in2.blockchainconnector.service.BrokerEntityRetrievalService;
import es.in2.blockchainconnector.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;

import static es.in2.blockchainconnector.utils.HttpUtils.getRequest;
import static es.in2.blockchainconnector.utils.Utils.hasHLParameter;

@Slf4j
@Service
@RequiredArgsConstructor
public class BrokerEntityRetrievalServiceImpl implements BrokerEntityRetrievalService {

    private final ObjectMapper objectMapper;
    private final TransactionService transactionService;

    @Override
    public Mono<String> retrieveEntityFromSourceBroker(String processId, DLTNotificationDTO dltNotificationDTO) {
        // Retrieve one of the entities from the broker
        return Mono.defer(() -> {
            // Get URL from the DLTNotificationDTO.dataLocation()
            String sourceBrokerEntityURL = Arrays.stream(dltNotificationDTO.dataLocation().split("\\?hl="))
                    .findFirst()
                    .orElseThrow(IllegalArgumentException::new);
            // Send request asynchronously
            return Mono.fromFuture(() -> getRequest(sourceBrokerEntityURL))
                    .flatMap(response -> {
                        String entityId = Arrays.stream(dltNotificationDTO.dataLocation().split("entities/|\\?hl="))
                                .skip(1)
                                .findFirst()
                                .orElseThrow(IllegalArgumentException::new);

                        if (!hasHLParameter(dltNotificationDTO.dataLocation()) && response.statusCode() == 404) {
                            log.debug(" > Detected deleted entity notification");
                            // Create and save transaction after receiving the response
                            Transaction transaction = Transaction.builder()
                                    .id(UUID.randomUUID())
                                    .transactionId(processId)
                                    .createdAt(Timestamp.from(Instant.now()))
                                    .dataLocation(dltNotificationDTO.dataLocation())
                                    .entityId(entityId)
                                    .entityHash("")
                                    .status(TransactionStatus.DELETED)
                                    .trader(TransactionTrader.CONSUMER)
                                    .hash("")
                                    .newTransaction(true)
                                    .build();
                            log.debug(response.body());
                            return transactionService.saveTransaction(transaction)
                                    .thenReturn(response.body());
                        }

                        // Create and save transaction after receiving the response
                        Transaction transaction = Transaction.builder()
                                .id(UUID.randomUUID())
                                .transactionId(processId)
                                .createdAt(Timestamp.from(Instant.now()))
                                .dataLocation(dltNotificationDTO.dataLocation())
                                .entityId(entityId)
                                .entityHash("")
                                .status(TransactionStatus.RETRIEVED)
                                .trader(TransactionTrader.CONSUMER)
                                .hash("")
                                .newTransaction(true)
                                .build();
                        log.debug(response.body());
                        return transactionService.saveTransaction(transaction)
                                .thenReturn(response.body());
                    });
        });
    }

}


