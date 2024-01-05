package es.in2.blockchainconnector.service.impl;

import es.in2.blockchainconnector.domain.Transaction;
import es.in2.blockchainconnector.domain.TransactionStatus;
import es.in2.blockchainconnector.domain.TransactionTrader;
import es.in2.blockchainconnector.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.MDC;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TransactionServiceImplTest {

    @InjectMocks
    private TransactionServiceImpl service;

    @Mock
    private TransactionRepository transactionRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void saveTransaction_Success() {
        // Arrange
        String processId = "testProcessId";
        MDC.put("processId", processId);

        Transaction transaction = createSampleTransaction();

        when(transactionRepository.save(any(Transaction.class))).thenReturn(Mono.just(transaction));

        // Act
        Mono<Transaction> resultMono = service.saveTransaction(transaction);

        // Assert
        Transaction resultTransaction = resultMono.block();
        assertEquals(transaction, resultTransaction);

    }


    private Transaction createSampleTransaction() {
        return Transaction.builder()
                .id(UUID.randomUUID())
                .transactionId("sampleTransactionId")
                .createdAt(Timestamp.from(Instant.now()))
                .dataLocation("sampleDataLocation")
                .entityId("sampleEntityId")
                .entityHash("sampleEntityHash")
                .status(TransactionStatus.CREATED)
                .trader(TransactionTrader.PRODUCER)
                .hash("sampleHash")
                .newTransaction(true)
                .build();
    }

    @Test
    void saveTransaction_Fail() {
        // Arrange
        String processId = "testProcessId";
        MDC.put("processId", processId);

        Transaction transaction = createSampleTransaction();

        when(transactionRepository.save(any(Transaction.class))).thenReturn(Mono.empty());

        // Act
        Mono<Transaction> resultMono = service.saveTransaction(transaction);

        // Assert
        Transaction resultTransaction = resultMono.block();
        assertEquals(null, resultTransaction);

    }

    @Test
    void getTransactionShouldReturnTransactions() {
        String transactionId = "testId";
        List<Transaction> expectedTransactions = List.of(new Transaction(), new Transaction());
        when(transactionRepository.findByEntityId(transactionId)).thenReturn(Flux.fromIterable(expectedTransactions));

        Mono<List<Transaction>> result = service.getTransaction(transactionId);

        StepVerifier.create(result)
                .expectNextMatches(transactions -> transactions.equals(expectedTransactions))
                .verifyComplete();

        verify(transactionRepository).findByEntityId(transactionId);
    }

    @Test
    void getTransactionShouldHandleError() {
        String transactionId = "testId";
        when(transactionRepository.findByEntityId(transactionId)).thenReturn(Flux.error(new RuntimeException("Error")));

        Mono<List<Transaction>> result = service.getTransaction(transactionId);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException && throwable.getMessage().equals("Error"))
                .verify();

        verify(transactionRepository).findByEntityId(transactionId);
    }

    @Test
    void getAllTransactionsShouldReturnAllTransactions() {
        // Arrange
        List<Transaction> expectedTransactions = List.of(
                createSampleTransaction(),
                createSampleTransaction()
        );
        when(transactionRepository.findAll()).thenReturn(Flux.fromIterable(expectedTransactions));

        // Act
        Flux<Transaction> resultFlux = service.getAllTransactions();

        // Assert
        StepVerifier.create(resultFlux)
                .expectNextSequence(expectedTransactions)
                .verifyComplete();

        verify(transactionRepository).findAll();
    }


}

