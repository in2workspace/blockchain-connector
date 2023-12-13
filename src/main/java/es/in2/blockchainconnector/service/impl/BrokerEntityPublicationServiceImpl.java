package es.in2.blockchainconnector.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.in2.blockchainconnector.configuration.properties.BrokerAdapterProperties;
import es.in2.blockchainconnector.configuration.properties.BrokerProperties;
import es.in2.blockchainconnector.domain.DLTNotificationDTO;
import es.in2.blockchainconnector.domain.Transaction;
import es.in2.blockchainconnector.domain.TransactionStatus;
import es.in2.blockchainconnector.domain.TransactionTrader;
import es.in2.blockchainconnector.exception.InvalidHashlinkComparisonException;
import es.in2.blockchainconnector.exception.JsonReadingException;
import es.in2.blockchainconnector.exception.RequestErrorException;
import es.in2.blockchainconnector.service.BrokerEntityPublicationService;
import es.in2.blockchainconnector.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.net.http.HttpResponse;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static es.in2.blockchainconnector.utils.Utils.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class BrokerEntityPublicationServiceImpl implements BrokerEntityPublicationService {

    private final BrokerProperties brokerProperties;
    private final BrokerAdapterProperties brokerAdapterProperties;
    private final ObjectMapper objectMapper;
    private final TransactionService transactionService;

    @Override
    public Mono<Void> publishOrDeleteAnEntityIntoContextBroker(String processId, DLTNotificationDTO dltNotificationDTO, String validatedEntity) {
        if (checkIfDeleted(validatedEntity)) {
            try {
                return handleDeletedEntity(processId, dltNotificationDTO, validatedEntity);
            } catch (NoSuchAlgorithmException e) {
                throw new InvalidHashlinkComparisonException("Error while calculating hash");
            }
        }
        return handleBrokerResponse(processId, validatedEntity, dltNotificationDTO);
    }

    private Mono<Void> handleBrokerResponse(String processId, String validatedEntity, DLTNotificationDTO dltNotificationDTO) {
        try {
            // Depending on the status code it will decide if update or publish
            CompletableFuture<HttpResponse<String>> retrievedEntity = getRequest(brokerAdapterProperties.domain() +
                    brokerAdapterProperties.paths().entities() +
                    "/" + extractIdFromEntity(validatedEntity));
            int responseCode = retrievedEntity.thenApply(HttpResponse::statusCode).join();
            if (responseCode == 200) {
                log.info(" > Entity exists");
                return updateEntityToBroker(processId, validatedEntity, dltNotificationDTO);
            } else if (responseCode == 404) {
                log.info(" > Entity doesn't exist");
                return publishEntityToBroker(processId, validatedEntity, dltNotificationDTO);
            } else {
                log.warn("Unhandled response status code: {}", responseCode);
                return Mono.error(new RequestErrorException("Unhandled response status code: " + responseCode));
            }
        } catch (Exception e) {
            log.error("Error handling response", e);
            return Mono.error(e);
        }
    }

    private Mono<Void> handleDeletedEntity(String processId, DLTNotificationDTO dltNotificationDTO, String validatedEntity) throws NoSuchAlgorithmException {
        Transaction transaction = Transaction.builder()
                .id(UUID.randomUUID())
                .transactionId(processId)
                .createdAt(Timestamp.from(Instant.now()))
                .dataLocation(dltNotificationDTO.dataLocation())
                .entityId(extractIdFromDeletedEntity(validatedEntity))
                .entityHash(calculateSHA256Hash(validatedEntity))
                .status(TransactionStatus.PUBLISHED)
                .trader(TransactionTrader.CONSUMER)
                .hash("")
                .newTransaction(true)
                .build();
        String sourceBrokerEntityID = Arrays.stream(dltNotificationDTO.dataLocation().split("entities/|\\?hl="))
                .skip(1)
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
        return deleteRequestResponseCode(brokerAdapterProperties.domain() + brokerAdapterProperties.paths().entities() + "/" + sourceBrokerEntityID)
                .flatMap(responseCode -> {
                    if (responseCode == 204) {
                        return Mono.defer(() -> transactionService.saveTransaction(transaction)).then();
                    }
                    return Mono.empty();
                });
    }

    Mono<Integer> deleteRequestResponseCode(String url) {
        return Mono.just(deleteRequest(url).thenApply(HttpResponse::statusCode).join());
    }

    private String extractIdFromEntity(String entity) {
        try {
            JsonNode jsonNode = objectMapper.readTree(entity);
            return jsonNode.get("id").asText();
        } catch (Exception e) {
            throw new JsonReadingException("Error while extracting data from entity");
        }
    }

    private String extractIdFromDeletedEntity(String entity) {
        try {
            JsonNode jsonNode = objectMapper.readTree(entity);
            return jsonNode.get("detail").asText();
        } catch (Exception e) {
            throw new JsonReadingException("Error while extracting data from entity");
        }
    }

    private boolean checkIfDeleted(String response) {
        try {
            JsonNode jsonNode = objectMapper.readTree(response);
            if (jsonNode.has("title")) {
                String title = jsonNode.get("title").asText();
                return "Entity Not Found".equals(title);
            }
        } catch (Exception e) {
            throw new JsonReadingException("Error while extracting data from entity");
        }
        return false;
    }


    private Mono<Void> publishEntityToBroker(String processId, String brokerEntity, DLTNotificationDTO dltNotificationDTO) throws NoSuchAlgorithmException {
        // Publish the entity to the broker
        String orionLdEntitiesUrl = brokerProperties.internalDomain() + brokerProperties.paths().entities();
        log.debug(" > Publishing entity to: {}", orionLdEntitiesUrl);
        log.debug(" > Entity to publish: {}", brokerEntity);
        Transaction transaction = Transaction.builder()
                .id(UUID.randomUUID())
                .transactionId(processId)
                .createdAt(Timestamp.from(Instant.now()))
                .dataLocation(dltNotificationDTO.dataLocation())
                .entityType(dltNotificationDTO.eventType())
                .entityId(extractIdFromEntity(brokerEntity))
                .entityHash(calculateSHA256Hash(brokerEntity))
                .status(TransactionStatus.PUBLISHED)
                .trader(TransactionTrader.CONSUMER)
                .hash("")
                .newTransaction(true)
                .build();
        return Mono.fromRunnable(() -> postRequest(orionLdEntitiesUrl, brokerEntity))
                .then(Mono.defer(() -> transactionService.saveTransaction(transaction)))
                .then();
    }

    private Mono<Void> updateEntityToBroker(String processId, String brokerEntity, DLTNotificationDTO dltNotificationDTO) throws NoSuchAlgorithmException {
        // Update the entity to the broker
        String brokerUpdateEntitiesUrl = brokerAdapterProperties.domain() + brokerAdapterProperties.paths().entities();
        log.debug(" > Updating entity to {}", brokerUpdateEntitiesUrl);
        Transaction transaction = Transaction.builder()
                .id(UUID.randomUUID())
                .transactionId(processId)
                .createdAt(Timestamp.from(Instant.now()))
                .dataLocation(dltNotificationDTO.dataLocation())
                .entityType(dltNotificationDTO.eventType())
                .entityId(extractIdFromEntity(brokerEntity))
                .entityHash(calculateSHA256Hash(brokerEntity))
                .status(TransactionStatus.PUBLISHED)
                .trader(TransactionTrader.CONSUMER)
                .hash("")
                .newTransaction(true)
                .build();
        return Mono.fromRunnable(() -> patchRequest(brokerUpdateEntitiesUrl, brokerEntity))
                .then(Mono.defer(() -> transactionService.saveTransaction(transaction)))
                .then();
    }


}
