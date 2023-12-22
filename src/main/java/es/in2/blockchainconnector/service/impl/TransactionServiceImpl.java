package es.in2.blockchainconnector.service.impl;

import es.in2.blockchainconnector.domain.Transaction;
import es.in2.blockchainconnector.repository.TransactionRepository;
import es.in2.blockchainconnector.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;

    @Override
    public Mono<Transaction> saveTransaction(Transaction transaction) {
        String processId = MDC.get("processId");
        return transactionRepository.save(transaction)
                .doOnError(error -> log.error("ProcessID: {} - Error saving transaction: {}", processId, error.getMessage()));
    }

    @Override
    public Mono<List<Transaction>> getTransaction(String transactionId) {
        String processIdc = MDC.get("processId");
        log.debug("ProcessID: {} - Getting transactions with id: {}", processIdc, transactionId);
        return transactionRepository.findByEntityId(transactionId)
                .collectList();
    }


}
