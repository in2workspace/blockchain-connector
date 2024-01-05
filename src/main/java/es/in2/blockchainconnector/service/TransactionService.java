package es.in2.blockchainconnector.service;

import es.in2.blockchainconnector.domain.Transaction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface TransactionService {
    Mono<Transaction> saveTransaction(Transaction transaction);
    Mono<List<Transaction>> getTransaction(String transactionId);
    Flux<Transaction> getAllTransactions();
}
