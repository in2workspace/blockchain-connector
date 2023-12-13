package es.in2.blockchainconnector.service.impl;

import es.in2.blockchainconnector.configuration.ApplicationConfig;
import es.in2.blockchainconnector.configuration.properties.BrokerPathProperties;
import es.in2.blockchainconnector.configuration.properties.BrokerProperties;
import es.in2.blockchainconnector.domain.OnChainEvent;
import es.in2.blockchainconnector.domain.OnChainEventDTO;
import es.in2.blockchainconnector.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import reactor.core.publisher.Mono;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


class BlockchainEventCreationServiceImplTest {
    @Mock
    private TransactionService transactionService;

    @Mock
    private ApplicationConfig applicationConfig;

    @Mock
    private HttpClient httpClient;

    @Mock
    private HttpResponse httpResponse;

    @InjectMocks
    private BlockchainEventCreationServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        BrokerProperties brokerProperties = new BrokerProperties("http://localhost:1026", "http://localhost:1026", new BrokerPathProperties("/v2", "/entities"));
        service = new BlockchainEventCreationServiceImpl(brokerProperties, transactionService, applicationConfig);

    }

    @Test
    void createBlockchainEventTransaction() {
        try (MockedStatic<HttpClient> httpUtilsMockedStatic = Mockito.mockStatic(HttpClient.class)) {

            // Arrange

            String processId = "testProcessId";
            String entityHashed = "testEntityHashed";

            // Create a sample OnChainEventDTO
            OnChainEventDTO onChainEventDTO = OnChainEventDTO.builder()
                    .id("sampleId")
                    .eventType("sampleEventType")
                    .dataMap(Collections.singletonMap("sampleKey", "sampleValue"))
                    .data("sampleData")
                    .build();

            // Mock the behavior of saveTransaction in TransactionService
            when(transactionService.saveTransaction(any())).thenReturn(Mono.empty());
            httpUtilsMockedStatic.when(HttpClient::newHttpClient).thenReturn(httpClient);
            when(httpResponse.statusCode()).thenReturn(200);
            when(httpResponse.body()).thenReturn(entityHashed);
            when(httpClient.sendAsync(any(), any())).thenReturn(CompletableFuture.completedFuture(httpResponse));

            // Act
            Mono<OnChainEvent> resultMono = service.createBlockchainEvent(processId, onChainEventDTO);

            // Assert
            OnChainEvent result = resultMono.block(); // Blocks until the Mono is completed
            assert result != null;

            // Verify that saveTransaction was called exactly once with any Transaction object as an argument
            verify(transactionService, times(1)).saveTransaction(any());
        }
    }
}
