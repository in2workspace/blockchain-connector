package es.in2.blockchainconnector.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.sql.Timestamp;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table("transactions")
public class Transaction implements Persistable<UUID> {

    @Id
    @Column("id")
    private UUID id;

    @Column("transaction_id")
    private String transactionId;

    @Column("created_at")
    private Timestamp createdAt;

    @Column("data_location")
    private String dataLocation;

    @Column("entity_id")
    private String entityId;

    @Column("entity_type")
    private String entityType;

    @Column("entity_hash")
    private String entityHash;

    @Column("status")
    private TransactionStatus status;

    @Column("trader")
    private TransactionTrader trader;

    @Column("hash")
    private String hash;

    @Transient
    private boolean newTransaction;

    @Override
    @Transient
    public boolean isNew() {
        return this.newTransaction || id == null;
    }

    public Transaction setAsNew() {
        this.newTransaction = true;
        return this;
    }

}
