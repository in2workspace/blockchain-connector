package es.in2.blockchainconnector.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TransactionStatus {
    RECEIVED("notified"),
    CREATED("created"),
    RETRIEVED("retrieved"),
    PUBLISHED("published"),
    DELETED("deleted");

    private final String description;
}
