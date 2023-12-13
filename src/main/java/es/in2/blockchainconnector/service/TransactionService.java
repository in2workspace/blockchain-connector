package es.in2.blockchainconnector.service;

import es.in2.blockchainconnector.domain.Transaction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface TransactionService {
    Mono<Transaction> saveTransaction(Transaction transaction);
    Mono<Transaction> getTransaction(String transactionId);
}
