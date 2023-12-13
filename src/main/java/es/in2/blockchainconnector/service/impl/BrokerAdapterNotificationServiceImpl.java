package es.in2.blockchainconnector.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.in2.blockchainconnector.domain.*;
import es.in2.blockchainconnector.exception.BrokerNotificationParserException;
import es.in2.blockchainconnector.service.BrokerAdapterNotificationService;
import es.in2.blockchainconnector.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BrokerAdapterNotificationServiceImpl implements BrokerAdapterNotificationService {

    private final ObjectMapper objectMapper;
    private final TransactionService transactionService;

    @Override
    public Mono<OnChainEventDTO> processNotification(ScorpioNotification scorpioNotification) {
        if (scorpioNotification == null || scorpioNotification.data().isEmpty()) {
            return Mono.error(new IllegalArgumentException("Invalid BrokerNotificationDTO"));
        }

        Map<String, Object> dataMap = scorpioNotification.data().get(0);
        if (dataMap == null || dataMap.get("id") == null) {
            return Mono.error(new IllegalArgumentException("Invalid dataMap in BrokerNotificationDTO"));
        }

        String processId = MDC.get("processId");
        String id = dataMap.get("id").toString();
        if (dataMap.containsKey("deletedAt")) {
            Mono<Transaction> eventTypeMono = transactionService.getTransaction(id);
            return eventTypeMono
                    .doOnNext(transaction -> log.debug("ProcessID: {} - Transaction: {}", processId, transaction))
                    .flatMap(previousEntityTransaction -> {
                        String dataToPersist;
                        try {
                            dataToPersist = objectMapper.writeValueAsString(dataMap);
                        } catch (JsonProcessingException e) {
                            log.error("ProcessID: {} - Error processing JSON: {}", processId, e.getMessage());
                            return Mono.error(new BrokerNotificationParserException("Error processing JSON", e));
                        }

                        OnChainEventDTO onChainEventDTO = OnChainEventDTO.builder()
                                .id(id)
                                .eventType(previousEntityTransaction.getEntityType())
                                .dataMap(dataMap)
                                .data(dataToPersist)
                                .build();

                        Transaction transaction = Transaction.builder()
                                .id(UUID.randomUUID())
                                .transactionId(processId)
                                .createdAt(Timestamp.from(Instant.now()))
                                .dataLocation("")
                                .entityId(id)
                                .entityType(previousEntityTransaction.getEntityType())
                                .entityHash("")
                                .status(TransactionStatus.RECEIVED)
                                .trader(TransactionTrader.PRODUCER)
                                .hash("")
                                .newTransaction(true)
                                .build();

                        return transactionService.saveTransaction(transaction)
                                .thenReturn(onChainEventDTO);
                    });
        } else {
            String dataToPersist;
            try {
                dataToPersist = objectMapper.writeValueAsString(dataMap);
            } catch (JsonProcessingException e) {
                log.error("ProcessID: {} - Error processing JSON: {}", processId, e.getMessage());
                return Mono.error(new BrokerNotificationParserException("Error processing JSON", e));
            }

            OnChainEventDTO onChainEventDTO = OnChainEventDTO.builder()
                    .id(id)
                    .eventType(dataMap.get("type").toString())
                    .dataMap(dataMap)
                    .data(dataToPersist)
                    .build();

            Transaction transaction = Transaction.builder()
                    .id(UUID.randomUUID())
                    .transactionId(processId)
                    .createdAt(Timestamp.from(Instant.now()))
                    .dataLocation("")
                    .entityId(id)
                    .entityType(dataMap.get("type").toString())
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

}
