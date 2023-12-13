package es.in2.blockchainconnector.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.in2.blockchainconnector.configuration.properties.BrokerAdapterPathProperties;
import es.in2.blockchainconnector.configuration.properties.BrokerAdapterProperties;
import es.in2.blockchainconnector.configuration.properties.BrokerPathProperties;
import es.in2.blockchainconnector.configuration.properties.BrokerProperties;
import es.in2.blockchainconnector.domain.*;
import es.in2.blockchainconnector.repository.TransactionRepository;
import es.in2.blockchainconnector.service.TransactionService;
import es.in2.blockchainconnector.utils.HttpUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static es.in2.blockchainconnector.utils.HttpUtils.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(SpringExtension.class)
class BrokerEntityPublicationServiceImplTest {

    @Mock
    private TransactionService transactionService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private HttpClient httpClient;

    @Mock
    private HttpResponse httpResponse;

    @InjectMocks
    private BrokerEntityPublicationServiceImpl brokerEntityPublicationService;

    @BeforeEach
    void setUp() {
        brokerEntityPublicationService = new BrokerEntityPublicationServiceImpl(new BrokerProperties(null, "http://mkt1-context-broker:8080", new BrokerPathProperties("/api/v1/entities", "/api/v1/subscriptions")), new BrokerAdapterProperties("http://mkt1-broker-adapter:8080", new BrokerAdapterPathProperties(null, null)), new ObjectMapper(), transactionService);
    }


    @Test
    void testHandleDeletedEntity() throws NoSuchAlgorithmException, JsonProcessingException {

        try (MockedStatic<HttpClient> httpUtilsMockedStatic = Mockito.mockStatic(HttpClient.class)) {
            // Arrange
            String processId = "process123";
            DLTNotificationDTO dltNotificationDTO = new DLTNotificationDTO(new BlockchainNodeNotificationIdDTO("type", "hex"), "address", "ProductOffering", new BlockchainNodeNotificationTimestampDTO("type", "hex"), "http://mkt1-broker-adapter:8080/api/v1/entities/urn:ngsi-ld:product-offering:443734333?hl=753d19c05e3b98b9dd07be5560dab7f7378bd619725fefff36db48a918e2e311", Collections.emptyList());
            String validatedEntity = "{\"type\":\"https://uri.etsi.org/ngsi-ld/errors/ResourceNotFound\",\"title\":\"Entity Not Found\",\"detail\":\"urn:ngsi-ld:product-offering:443734333\"}";
            Transaction transaction = Transaction.builder()
                    .id(UUID.randomUUID())
                    .transactionId(processId)
                    .createdAt(Timestamp.from(Instant.now()))
                    .dataLocation(dltNotificationDTO.dataLocation())
                    .entityId("urn:ngsi-ld:product-offering:443734333")
                    .entityHash("tuEntityHash")
                    .status(TransactionStatus.PUBLISHED)
                    .trader(TransactionTrader.CONSUMER)
                    .hash("")
                    .newTransaction(true)
                    .build();
            when(transactionService.saveTransaction(transaction)).thenReturn(Mono.just(transaction));
            JsonNode jsonNode = objectMapper.readTree(validatedEntity);
            when(objectMapper.readTree(validatedEntity)).thenReturn(jsonNode);
            httpUtilsMockedStatic.when(HttpClient::newHttpClient).thenReturn(httpClient);
            when(httpResponse.statusCode()).thenReturn(204);
            when(httpClient.sendAsync(any(), any())).thenReturn(CompletableFuture.completedFuture(httpResponse));
            brokerEntityPublicationService.publishOrDeleteAnEntityIntoContextBroker(processId, dltNotificationDTO, validatedEntity).subscribe();

            // Assert
            verify(transactionService, times(1)).saveTransaction(any());


        }

    }

    @Test
    void testHandlePublicationEntity() throws NoSuchAlgorithmException, JsonProcessingException {

        try (MockedStatic<HttpClient> httpUtilsMockedStatic = Mockito.mockStatic(HttpClient.class)) {
            String processId = "process123";
            DLTNotificationDTO dltNotificationDTO = new DLTNotificationDTO(new BlockchainNodeNotificationIdDTO("type", "hex"), "address", "ProductOffering", new BlockchainNodeNotificationTimestampDTO("type", "hex"), "http://mkt1-broker-adapter:8080/api/v1/entities/urn:ngsi-ld:product-offering:443734333?hl=753d19c05e3b98b9dd07be5560dab7f7378bd619725fefff36db48a918e2e311", Collections.emptyList());
            String validatedEntity = "{\"id\":\"urn:ngsi-ld:product-offering:443734333\",\"type\":\"ProductOffering\",\"category\":{\"type\":\"Property\",\"value\":\"B2C product orders\"},\"channel\":{\"type\":\"Property\",\"value\":\"Used channel for order capture\",\"@id\":{\"type\":\"Property\",\"value\":\"1\"},\"name\":{\"type\":\"Property\",\"value\":\"Online channel\"},\"role\":{\"type\":\"Property\",\"value\":\"Used channel for order captures\"}},\"description\":{\"type\":\"Property\",\"value\":\"Product Order illustration sample\"},\"externalId\":{\"type\":\"Property\",\"value\":\"PO-456\"},\"note\":{\"type\":\"Property\",\"value\":\"This is a TMF product order illustration\",\"author\":{\"type\":\"Property\",\"value\":\"Jean Pontus\"},\"date\":{\"type\":\"Property\",\"value\":\"2019-04-30T08:13:59.509Z\"},\"@id\":{\"type\":\"Property\",\"value\":\"1\"},\"text\":{\"type\":\"Property\",\"value\":\"This is a TMF product order illustration\"}},\"priority\":{\"type\":\"Property\",\"value\":1},\"productOrderItem\":{\"type\":\"Property\",\"value\":[]},\"relatedParty\":{\"type\":\"Property\",\"value\":[{\"@referredType\":\"Individual\",\"@type\":\"RelatedParty\",\"href\":\"https://host:port/partyManagement/v4/individual/456-dd-df45\",\"id\":\"456-dd-df45\",\"name\":\"Joe Doe\",\"role\":\"Seller\"}]},\"requestedCompletionDate\":{\"type\":\"Property\",\"value\":\"2019-05-02T08:13:59.506Z\"},\"requestedStartDate\":{\"type\":\"Property\",\"value\":\"2019-05-03T08:13:59.506Z\"}}";
            Transaction transaction = Transaction.builder()
                    .id(UUID.randomUUID())
                    .transactionId(processId)
                    .createdAt(Timestamp.from(Instant.now()))
                    .dataLocation(dltNotificationDTO.dataLocation())
                    .entityId("urn:ngsi-ld:product-offering:443734333")
                    .entityHash("tuEntityHash")
                    .status(TransactionStatus.PUBLISHED)
                    .trader(TransactionTrader.CONSUMER)
                    .hash("")
                    .newTransaction(true)
                    .build();

            when(transactionService.saveTransaction(transaction)).thenReturn(Mono.just(transaction));
            JsonNode jsonNode = objectMapper.readTree(validatedEntity);
            when(objectMapper.readTree(validatedEntity)).thenReturn(jsonNode);
            httpUtilsMockedStatic.when(HttpClient::newHttpClient).thenReturn(httpClient);
            when(httpResponse.statusCode()).thenReturn(200);
            when(httpClient.sendAsync(any(), any())).thenReturn(CompletableFuture.completedFuture(httpResponse));
            when(httpResponse.body()).thenReturn(validatedEntity);
            brokerEntityPublicationService.publishOrDeleteAnEntityIntoContextBroker(processId, dltNotificationDTO, validatedEntity).subscribe();

            verify(transactionService, times(1)).saveTransaction(any());


        }
    }

    @Test
    void testHandlePublicationEntity_EntityDoesNotExist() throws NoSuchAlgorithmException, JsonProcessingException {

        try (MockedStatic<HttpClient> httpUtilsMockedStatic = Mockito.mockStatic(HttpClient.class)) {
            String processId = "process123";
            DLTNotificationDTO dltNotificationDTO = new DLTNotificationDTO(new BlockchainNodeNotificationIdDTO("type", "hex"), "address", "ProductOffering", new BlockchainNodeNotificationTimestampDTO("type", "hex"), "http://mkt1-broker-adapter:8080/api/v1/entities/urn:ngsi-ld:product-offering:443734333?hl=753d19c05e3b98b9dd07be5560dab7f7378bd619725fefff36db48a918e2e311", Collections.emptyList());
            String validatedEntity = "{\"id\":\"urn:ngsi-ld:product-offering:443734333\",\"type\":\"ProductOffering\",\"category\":{\"type\":\"Property\",\"value\":\"B2C product orders\"},\"channel\":{\"type\":\"Property\",\"value\":\"Used channel for order capture\",\"@id\":{\"type\":\"Property\",\"value\":\"1\"},\"name\":{\"type\":\"Property\",\"value\":\"Online channel\"},\"role\":{\"type\":\"Property\",\"value\":\"Used channel for order captures\"}},\"description\":{\"type\":\"Property\",\"value\":\"Product Order illustration sample\"},\"externalId\":{\"type\":\"Property\",\"value\":\"PO-456\"},\"note\":{\"type\":\"Property\",\"value\":\"This is a TMF product order illustration\",\"author\":{\"type\":\"Property\",\"value\":\"Jean Pontus\"},\"date\":{\"type\":\"Property\",\"value\":\"2019-04-30T08:13:59.509Z\"},\"@id\":{\"type\":\"Property\",\"value\":\"1\"},\"text\":{\"type\":\"Property\",\"value\":\"This is a TMF product order illustration\"}},\"priority\":{\"type\":\"Property\",\"value\":1},\"productOrderItem\":{\"type\":\"Property\",\"value\":[]},\"relatedParty\":{\"type\":\"Property\",\"value\":[{\"@referredType\":\"Individual\",\"@type\":\"RelatedParty\",\"href\":\"https://host:port/partyManagement/v4/individual/456-dd-df45\",\"id\":\"456-dd-df45\",\"name\":\"Joe Doe\",\"role\":\"Seller\"}]},\"requestedCompletionDate\":{\"type\":\"Property\",\"value\":\"2019-05-02T08:13:59.506Z\"},\"requestedStartDate\":{\"type\":\"Property\",\"value\":\"2019-05-03T08:13:59.506Z\"}}";
            Transaction transaction = Transaction.builder()
                    .id(UUID.randomUUID())
                    .transactionId(processId)
                    .createdAt(Timestamp.from(Instant.now()))
                    .dataLocation(dltNotificationDTO.dataLocation())
                    .entityId("urn:ngsi-ld:product-offering:443734333")
                    .entityHash("tuEntityHash")
                    .status(TransactionStatus.PUBLISHED)
                    .trader(TransactionTrader.CONSUMER)
                    .hash("")
                    .newTransaction(true)
                    .build();

            when(transactionService.saveTransaction(transaction)).thenReturn(Mono.just(transaction));
            JsonNode jsonNode = objectMapper.readTree(validatedEntity);
            when(objectMapper.readTree(validatedEntity)).thenReturn(jsonNode);
            httpUtilsMockedStatic.when(HttpClient::newHttpClient).thenReturn(httpClient);
            when(httpResponse.statusCode()).thenReturn(404);
            when(httpClient.sendAsync(any(), any())).thenReturn(CompletableFuture.completedFuture(httpResponse));
            brokerEntityPublicationService.publishOrDeleteAnEntityIntoContextBroker(processId, dltNotificationDTO, validatedEntity).subscribe();

            verify(transactionService, times(1)).saveTransaction(any());


        }
    }

}
