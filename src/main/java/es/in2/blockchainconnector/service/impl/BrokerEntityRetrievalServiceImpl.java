package es.in2.blockchainconnector.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.in2.blockchainconnector.domain.DLTNotificationDTO;
import es.in2.blockchainconnector.domain.Transaction;
import es.in2.blockchainconnector.domain.TransactionStatus;
import es.in2.blockchainconnector.domain.TransactionTrader;
import es.in2.blockchainconnector.exception.HashLinkException;
import es.in2.blockchainconnector.service.BrokerEntityRetrievalService;
import es.in2.blockchainconnector.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static es.in2.blockchainconnector.utils.HttpUtils.getRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class BrokerEntityRetrievalServiceImpl implements BrokerEntityRetrievalService {

    private final TransactionService transactionService;
    private final ObjectMapper objectMapper;

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

    private static boolean hasHLParameter(String urlString) {
        try {
            URL url = new URL(urlString);
            Map<String, String> queryParams = splitQuery(url);
            log.debug("Query params: {}", queryParams);
            return queryParams.containsKey("hl");
        } catch (MalformedURLException e) {
            throw new HashLinkException("Error parsing datalocation");
        }
    }


    private static Map<String, String> splitQuery(URL url) {
        if (url.getQuery() == null || url.getQuery().isEmpty()) {
            return new HashMap<>();
        }
        Map<String, String> queryPairs = new HashMap<>();
        String[] pairs = url.getQuery().split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            queryPairs.put(pair.substring(0, idx), idx > 0 && pair.length() > idx + 1 ? pair.substring(idx + 1) : null);
        }
        return queryPairs;
    }

}


