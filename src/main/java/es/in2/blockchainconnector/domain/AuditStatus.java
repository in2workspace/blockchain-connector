package es.in2.blockchainconnector.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuditStatus {
    RECEIVED("RECEIVED"),
    PUBLISHED("PUBLISHED"),
    CREATED("CREATED");

    private final String description;
}