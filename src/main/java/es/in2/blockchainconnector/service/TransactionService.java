package es.in2.blockchainconnector.service;

import es.in2.blockchainconnector.domain.Transaction;
import reactor.core.publisher.Mono;

public interface TransactionService {
    Mono<Transaction> saveTransaction(Transaction transaction);
    Mono<Transaction> getTransaction(String transactionId);
}
