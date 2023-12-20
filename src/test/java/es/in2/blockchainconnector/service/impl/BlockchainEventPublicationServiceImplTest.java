package es.in2.blockchainconnector.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.in2.blockchainconnector.configuration.DLTAdapterConfig;
import es.in2.blockchainconnector.configuration.properties.DLTAdapterPathProperties;
import es.in2.blockchainconnector.configuration.properties.DLTAdapterProperties;
import es.in2.blockchainconnector.domain.OnChainEvent;
import es.in2.blockchainconnector.domain.Transaction;
import es.in2.blockchainconnector.service.TransactionService;
import es.in2.blockchainconnector.utils.Utils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlockchainEventPublicationServiceImplTest {

    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private DLTAdapterProperties dltAdapterProperties;
    @Mock
    private DLTAdapterConfig dltAdapterConfig;
    @Mock
    private TransactionService transactionService;
    @Mock
    private HttpClient httpClient;
    @Mock
    private HttpResponse<String> httpResponse;

    @InjectMocks
    private BlockchainEventPublicationServiceImpl blockchainEventPublicationService;

    @Test
    void testPublishBlockchainEventIntoBlockchainNode() throws Exception {
        String processId = "processId";
        OnChainEvent onChainEvent = new OnChainEvent("id", "type", "entityId", "", "http://example.com/entity?hl=test", null);

        try (MockedStatic<HttpClient> httpUtilsMockedStatic = Mockito.mockStatic(HttpClient.class)) {
            when(dltAdapterProperties.domain()).thenReturn("http://example.com");
            when(dltAdapterProperties.paths()).thenReturn(new DLTAdapterProperties("http://dlt-adapter:8080", new DLTAdapterPathProperties("/configureNode", "/publish", "/subscribe")).paths());
            when(objectMapper.writeValueAsString(onChainEvent)).thenReturn("json");
            when(dltAdapterConfig.dltAdapterHttpClient()).thenReturn(httpClient);
            Transaction mockTransaction = new Transaction();
            when(transactionService.saveTransaction(any())).thenReturn(Mono.just(mockTransaction));

            blockchainEventPublicationService.publishBlockchainEventIntoBlockchainNode(processId, onChainEvent).subscribe();

            verify(transactionService).saveTransaction(any(Transaction.class));
        }



        }
    }

