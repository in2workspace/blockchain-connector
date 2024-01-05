package es.in2.blockchainconnector.configuration;

import es.in2.blockchainconnector.configuration.properties.DLTAdapterProperties;
import es.in2.blockchainconnector.domain.Transaction;
import es.in2.blockchainconnector.domain.TransactionTrader;
import es.in2.blockchainconnector.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Comparator;

@Slf4j
@Component
@RequiredArgsConstructor
public class BlockchainConnectorInitializer {

    private final TransactionService transactionService;
    private final DLTAdapterProperties dltAdapterProperties;

    @EventListener(ApplicationReadyEvent.class)
    public Mono<Void> processAllTransactions() {
        log.debug("Searching for transactions to process...");
        return transactionService.getAllTransactions()
                .collectList()
                .flatMap(transactions -> transactions.stream()
                        .filter(t -> t.getTrader() == TransactionTrader.CONSUMER)
                        .max(Comparator.comparing(Transaction::getCreatedAt))
                        .map(lastConsumerTransaction -> {
                            long lastDateUnixTimestampMillis = lastConsumerTransaction.getCreatedAt().toInstant().toEpochMilli();
                            long nowUnixTimestampMillis = Instant.now().toEpochMilli();
                            return queryDLTAdapterFromRange(lastDateUnixTimestampMillis, nowUnixTimestampMillis);
                        })
                        .orElseGet(this::queryDLTAdapterFromBeginning))
                .then();
    }

    private Mono<Void> queryDLTAdapterFromRange(long startDateUnixTimestampMillis, long endDateUnixTimestampMillis) {
        String dltAdapterQueryURL = buildQueryURL(startDateUnixTimestampMillis, endDateUnixTimestampMillis);
        log.debug(dltAdapterQueryURL);
        return Mono.empty();
    }

    private Mono<Void> queryDLTAdapterFromBeginning() {
        long startUnixTimestampMillis = Instant.EPOCH.toEpochMilli();
        long nowUnixTimestampMillis = Instant.now().toEpochMilli();
        String dltAdapterQueryURL = buildQueryURL(startUnixTimestampMillis, nowUnixTimestampMillis);
        log.debug(dltAdapterQueryURL);
        return Mono.empty();
    }

    private String buildQueryURL(long startDateUnixTimestampMillis, long endDateUnixTimestampMillis) {
        return dltAdapterProperties.domain() + dltAdapterProperties.paths().events() +
                "?startDate=" + startDateUnixTimestampMillis + "&endDate=" + endDateUnixTimestampMillis;
    }

}


