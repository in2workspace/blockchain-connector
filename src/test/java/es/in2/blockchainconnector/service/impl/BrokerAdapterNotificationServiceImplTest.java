package es.in2.blockchainconnector.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.in2.blockchainconnector.configuration.properties.BrokerPathProperties;
import es.in2.blockchainconnector.configuration.properties.BrokerProperties;
import es.in2.blockchainconnector.domain.BrokerNotification;
import es.in2.blockchainconnector.domain.Transaction;
import es.in2.blockchainconnector.domain.TransactionStatus;
import es.in2.blockchainconnector.domain.TransactionTrader;
import es.in2.blockchainconnector.service.TransactionService;
import es.in2.blockchainconnector.utils.Utils;
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
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class BrokerAdapterNotificationServiceImplTest {

    @Mock
    private TransactionService transactionService;

    @Mock
    private ObjectMapper objectMapper; // Si es necesario

    @Mock
    private BrokerProperties brokerProperties; // Si es necesario

    @Mock
    private HttpClient httpClient;

    @Mock
    private HttpResponse httpResponse;

    @Mock
    private Utils utils;

    @InjectMocks
    private BrokerAdapterNotificationServiceImpl service;


    @BeforeEach
    void setUp() {
        service = new BrokerAdapterNotificationServiceImpl(
                objectMapper,
                transactionService,
                (new BrokerProperties(null, "http://mkt1-context-broker:8080", new BrokerPathProperties("/api/v1/entities", "/api/v1/subscriptions"))
                ));
    }

    @Test
    void testProcessNotificationWithValidData() throws JsonProcessingException, NoSuchAlgorithmException {
        Map<String, Object> validDataMap = new HashMap<>();
        validDataMap.put("id", "id_value");
        validDataMap.put("type", "type_value");
        validDataMap.put("data", Collections.emptyMap());

        BrokerNotification orionLDNotification = new BrokerNotification(
                "id_value",
                "type_value",
                Collections.singletonList(validDataMap),
                "subscriptionId_value",
                "notifiedAt_value"

        );
        List<Transaction> mockTransaction = Collections.singletonList(Transaction.builder().id(UUID.randomUUID())
                .transactionId("transactionId")
                .dataLocation("")
                .createdAt(Timestamp.from(Instant.now()))
                .entityHash("hash")
                .entityId("entityId")
                .entityType("ProductOffering")
                .newTransaction(false)
                .hash("0x641164b01f2f737f0d0cc866e12714de0218772468d9037a2fc61126ea00d9b3")
                .status(TransactionStatus.RECEIVED).build());

        Transaction resultTransaction = Transaction.builder()
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


        try (MockedStatic<HttpClient> httpUtilsMockedStatic = Mockito.mockStatic(HttpClient.class)) {
            when(transactionService.getTransaction(anyString())).thenReturn(Mono.just(mockTransaction));
            when(objectMapper.writeValueAsString(any())).thenReturn("Mocked response");
            when(transactionService.saveTransaction(any())).thenReturn(Mono.just(resultTransaction));
            when(transactionService.saveTransaction(any(Transaction.class)))
                    .thenReturn(Mono.just(resultTransaction));
            httpUtilsMockedStatic.when(HttpClient::newHttpClient).thenReturn(httpClient);
            when(httpClient.sendAsync(any(), any())).thenReturn(CompletableFuture.completedFuture(httpResponse));
            when(httpResponse.statusCode()).thenReturn(200);
            when(httpResponse.body()).thenReturn("Mocked response");
            service.processNotification(orionLDNotification).subscribe();
            verify(transactionService).getTransaction(anyString());
            verify(transactionService).saveTransaction(any());


        }


    }

    @Test
    void testProcessNotificationWithDeletionData() throws JsonProcessingException, NoSuchAlgorithmException {
        Map<String, Object> validDataMap = new HashMap<>();
        validDataMap.put("id", "id_value");
        validDataMap.put("type", "type_value");
        validDataMap.put("deletedAt", "notifiedAt_value");
        validDataMap.put("data", Collections.emptyMap());

        BrokerNotification orionLDNotification = new BrokerNotification(
                "id_value",
                "type_value",
                Collections.singletonList(validDataMap),
                "subscriptionId_value",
                "notifiedAt_value"

        );
        List<Transaction> mockTransaction = Collections.singletonList(Transaction.builder().id(UUID.randomUUID())
                .transactionId("transactionId")
                .dataLocation("")
                .createdAt(Timestamp.from(Instant.now()))
                .entityHash("hash")
                .entityId("entityId")
                .entityType("ProductOffering")
                .newTransaction(false)
                .hash("0x641164b01f2f737f0d0cc866e12714de0218772468d9037a2fc61126ea00d9b3")
                .status(TransactionStatus.RECEIVED).build());

        Transaction resultTransaction = Transaction.builder()
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


        try (MockedStatic<HttpClient> httpUtilsMockedStatic = Mockito.mockStatic(HttpClient.class)) {
            when(transactionService.getTransaction(anyString())).thenReturn(Mono.just(mockTransaction));
            when(objectMapper.writeValueAsString(any())).thenReturn("Mocked response");
            when(transactionService.saveTransaction(any())).thenReturn(Mono.just(resultTransaction));
            when(transactionService.saveTransaction(any(Transaction.class)))
                    .thenReturn(Mono.just(resultTransaction));
            httpUtilsMockedStatic.when(HttpClient::newHttpClient).thenReturn(httpClient);
            when(httpClient.sendAsync(any(), any())).thenReturn(CompletableFuture.completedFuture(httpResponse));
            when(httpResponse.statusCode()).thenReturn(200);
            when(httpResponse.body()).thenReturn("Mocked response");
            service.processNotification(orionLDNotification).subscribe();
            verify(transactionService).getTransaction(anyString());
            verify(transactionService).saveTransaction(any());


        }
    }

    @Test
    void testProcessNotificationWithDeletionData_lastTransactionDeleted() throws JsonProcessingException, NoSuchAlgorithmException {
        Map<String, Object> validDataMap = new HashMap<>();
        validDataMap.put("id", "id_value");
        validDataMap.put("type", "type_value");
        validDataMap.put("deletedAt", "notifiedAt_value");
        validDataMap.put("data", Collections.emptyMap());

        BrokerNotification orionLDNotification = new BrokerNotification(
                "id_value",
                "type_value",
                Collections.singletonList(validDataMap),
                "subscriptionId_value",
                "notifiedAt_value"

        );
        List<Transaction> mockTransaction = Collections.singletonList(Transaction.builder().id(UUID.randomUUID())
                .transactionId("transactionId")
                .dataLocation("")
                .createdAt(Timestamp.from(Instant.now()))
                .entityHash("hash")
                .entityId("entityId")
                .entityType("ProductOffering")
                .newTransaction(false)
                .hash("0x641164b01f2f737f0d0cc866e12714de0218772468d9037a2fc61126ea00d9b3")
                .status(TransactionStatus.DELETED).build());

        Transaction resultTransaction = Transaction.builder()
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


        try (MockedStatic<HttpClient> httpUtilsMockedStatic = Mockito.mockStatic(HttpClient.class)) {
            when(transactionService.getTransaction(anyString())).thenReturn(Mono.just(mockTransaction));
            when(objectMapper.writeValueAsString(any())).thenReturn("Mocked response");
            when(transactionService.saveTransaction(any())).thenReturn(Mono.just(resultTransaction));
            when(transactionService.saveTransaction(any(Transaction.class)))
                    .thenReturn(Mono.just(resultTransaction));
            httpUtilsMockedStatic.when(HttpClient::newHttpClient).thenReturn(httpClient);
            when(httpClient.sendAsync(any(), any())).thenReturn(CompletableFuture.completedFuture(httpResponse));
            when(httpResponse.statusCode()).thenReturn(200);
            when(httpResponse.body()).thenReturn("Mocked response");
            service.processNotification(orionLDNotification).subscribe();
            verify(transactionService).getTransaction(anyString());

        }


    }

    @Test
    void testProcessNotificationWithValidData_transactionExists() throws JsonProcessingException, NoSuchAlgorithmException {
        Map<String, Object> validDataMap = new HashMap<>();
        validDataMap.put("id", "id_value");
        validDataMap.put("type", "type_value");
        validDataMap.put("data", Collections.emptyMap());

        BrokerNotification orionLDNotification = new BrokerNotification(
                "id_value",
                "type_value",
                Collections.singletonList(validDataMap),
                "subscriptionId_value",
                "notifiedAt_value"

        );
        List<Transaction> mockTransaction = Collections.singletonList(Transaction.builder().id(UUID.randomUUID())
                .transactionId("transactionId")
                .dataLocation("")
                .createdAt(Timestamp.from(Instant.now()))
                .entityHash("0x641164b01f2f737f0d0cc866e12714de0218772468d9037a2fc61126ea00d9b3")
                .entityId("entityId")
                .entityType("ProductOffering")
                .newTransaction(false)
                .hash("0x641164b01f2f737f0d0cc866e12714de0218772468d9037a2fc61126ea00d9b3")
                .status(TransactionStatus.RECEIVED).build());

        Transaction resultTransaction = Transaction.builder()
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


        try (MockedStatic<HttpClient> httpUtilsMockedStatic = Mockito.mockStatic(HttpClient.class)) {
            when(transactionService.getTransaction(anyString())).thenReturn(Mono.just(mockTransaction));
            when(objectMapper.writeValueAsString(any())).thenReturn("Mocked response");
            when(transactionService.saveTransaction(any())).thenReturn(Mono.just(resultTransaction));
            when(transactionService.saveTransaction(any(Transaction.class)))
                    .thenReturn(Mono.just(resultTransaction));
            httpUtilsMockedStatic.when(HttpClient::newHttpClient).thenReturn(httpClient);
            when(httpClient.sendAsync(any(), any())).thenReturn(CompletableFuture.completedFuture(httpResponse));
            when(httpResponse.statusCode()).thenReturn(200);
            when(httpResponse.body()).thenReturn("Mocked response");
            service.processNotification(orionLDNotification).subscribe();
            verify(transactionService).getTransaction(anyString());


        }


    }

    @Test
    void testProcessNotificationWithValidData_transactionEmpty() throws JsonProcessingException, NoSuchAlgorithmException {
        Map<String, Object> validDataMap = new HashMap<>();
        validDataMap.put("id", "id_value");
        validDataMap.put("type", "type_value");
        validDataMap.put("data", Collections.emptyMap());

        BrokerNotification orionLDNotification = new BrokerNotification(
                "id_value",
                "type_value",
                Collections.singletonList(validDataMap),
                "subscriptionId_value",
                "notifiedAt_value"

        );
        List<Transaction> mockTransaction = Collections.emptyList();

        Transaction resultTransaction = Transaction.builder()
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


        try (MockedStatic<HttpClient> httpUtilsMockedStatic = Mockito.mockStatic(HttpClient.class)) {
            when(transactionService.getTransaction(anyString())).thenReturn(Mono.just(mockTransaction));
            when(objectMapper.writeValueAsString(any())).thenReturn("Mocked response");
            when(transactionService.saveTransaction(any())).thenReturn(Mono.just(resultTransaction));
            when(transactionService.saveTransaction(any(Transaction.class)))
                    .thenReturn(Mono.just(resultTransaction));
            httpUtilsMockedStatic.when(HttpClient::newHttpClient).thenReturn(httpClient);
            when(httpClient.sendAsync(any(), any())).thenReturn(CompletableFuture.completedFuture(httpResponse));
            when(httpResponse.statusCode()).thenReturn(200);
            when(httpResponse.body()).thenReturn("Mocked response");
            service.processNotification(orionLDNotification).subscribe();
            verify(transactionService).getTransaction(anyString());


        }
    }
}
