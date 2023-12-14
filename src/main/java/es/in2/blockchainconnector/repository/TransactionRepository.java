package es.in2.blockchainconnector.repository;

import es.in2.blockchainconnector.domain.Transaction;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Repository
public interface TransactionRepository extends ReactiveCrudRepository<Transaction, UUID> {
    Flux<Transaction> findByEntityId(String entityId);
}
