package es.in2.blockchainconnector.configuration;

import es.in2.blockchainconnector.configuration.properties.DLTAdapterPathProperties;
import es.in2.blockchainconnector.configuration.properties.DLTAdapterProperties;
import es.in2.blockchainconnector.domain.Transaction;
import es.in2.blockchainconnector.domain.TransactionTrader;
import es.in2.blockchainconnector.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;

import java.sql.Timestamp;
import java.time.Instant;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BlockchainConnectorInitializerTest {

    private TransactionService transactionService;
    private DLTAdapterProperties dltAdapterProperties;
    private BlockchainConnectorInitializer initializer;

    @BeforeEach
    void setUp() {
        // Manual mock initialization
        transactionService = Mockito.mock(TransactionService.class);
        dltAdapterProperties = Mockito.mock(DLTAdapterProperties.class);
        initializer = new BlockchainConnectorInitializer(transactionService, dltAdapterProperties);
    }

    @Test
    void whenConsumerTransactionsExist_thenQueryDLTAdapterFromRange() {
        // Arrange
        Transaction mockTransaction = new Transaction();
        mockTransaction.setTrader(TransactionTrader.CONSUMER);
        mockTransaction.setCreatedAt(Timestamp.from(Instant.now()));
        when(transactionService.getAllTransactions()).thenReturn(Flux.just(mockTransaction));
        when(dltAdapterProperties.domain()).thenReturn("http://example.com");
        when(dltAdapterProperties.paths()).thenReturn(new DLTAdapterProperties("http://dlt-adapter:8080", new DLTAdapterPathProperties("/configureNode", "/publish", "/subscribe", "")).paths());

        // Act
        initializer.processAllTransactions().block();

        // Assert
        verify(transactionService).getAllTransactions();
    }

    @Test
    void whenNoConsumerTransactions_thenQueryDLTAdapterFromBeginning() {
        // Arrange
        Transaction mockTransaction = new Transaction();
        mockTransaction.setTrader(TransactionTrader.PRODUCER); // Non-consumer type
        mockTransaction.setCreatedAt(Timestamp.from(Instant.now()));
        when(transactionService.getAllTransactions()).thenReturn(Flux.just(mockTransaction));
        when(dltAdapterProperties.domain()).thenReturn("http://example.com");
        when(dltAdapterProperties.paths()).thenReturn(new DLTAdapterProperties("http://dlt-adapter:8080", new DLTAdapterPathProperties("/configureNode", "/publish", "/subscribe", "")).paths());

        // Act
        initializer.processAllTransactions().block();

        // Assert
        verify(transactionService).getAllTransactions();
    }
}

