package es.in2.blockchainconnector.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.in2.blockchainconnector.domain.*;
import es.in2.blockchainconnector.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import reactor.core.publisher.Mono;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


class BrokerEntityRetrievalServiceImplTest {

    @Mock
    private TransactionService transactionService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private HttpClient httpClient;

    @Mock
    private HttpResponse httpResponse;

    @InjectMocks
    private BrokerEntityRetrievalServiceImpl brokerEntityRetrievalService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        brokerEntityRetrievalService = new BrokerEntityRetrievalServiceImpl(
                transactionService,
                objectMapper);
    }

    @Test
    void testRetrieveEntityFromSourceBroker() throws Exception {
        // Mocking DLTNotificationDTO
        DLTNotificationDTO dltNotificationDTO = new DLTNotificationDTO(new BlockchainNodeNotificationIdDTO("type", "hex"), "address", "ProductOffering", new BlockchainNodeNotificationTimestampDTO("type", "hex"), "http://mkt1-broker-adapter:8080/api/v1/entities/urn:ngsi-ld:product-offering:443734333?hl=753d19c05e3b98b9dd07be5560dab7f7378bd619725fefff36db48a918e2e311", Collections.emptyList());
        // Mocking ObjectMapper response
        JsonNode jsonNode = Mockito.mock(JsonNode.class);
        when(jsonNode.get("id")).thenReturn(Mockito.mock(JsonNode.class));
        when(jsonNode.get("id").asText()).thenReturn("mockedEntityId");
        when(objectMapper.readTree("Mocked response")).thenReturn(jsonNode);
        String mockedResponse = "Mocked response";

        // Mocking TransactionService response
        Transaction mockedTransaction = Transaction.builder()
                .id(UUID.randomUUID())
                .transactionId("mockedProcessId")
                .createdAt(Timestamp.valueOf("2021-01-01 00:00:00.000000000"))
                .dataLocation("http://example.com/entity?hl=test")
                .entityId("mockedEntityId")
                .entityHash("")
                .status(TransactionStatus.RETRIEVED)
                .trader(TransactionTrader.CONSUMER)
                .hash("")
                .newTransaction(true)
                .build();
        when(transactionService.saveTransaction(any(Transaction.class)))
                .thenReturn(Mono.just(mockedTransaction));

        try (MockedStatic<HttpClient> httpUtilsMockedStatic = Mockito.mockStatic(HttpClient.class)) {
            httpUtilsMockedStatic.when(HttpClient::newHttpClient).thenReturn(httpClient);
            when(httpResponse.statusCode()).thenReturn(200);
            when(httpResponse.body()).thenReturn(mockedResponse);
            when(httpClient.sendAsync(any(), any())).thenReturn(CompletableFuture.completedFuture(httpResponse));
            brokerEntityRetrievalService.retrieveEntityFromSourceBroker("mockedProcessId", dltNotificationDTO).subscribe();
            verify(transactionService, times(1)).saveTransaction(any());
        }
    }
}
