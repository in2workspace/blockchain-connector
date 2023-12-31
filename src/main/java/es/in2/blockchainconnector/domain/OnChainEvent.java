package es.in2.blockchainconnector.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

@Builder
public record OnChainEvent(
        @JsonProperty("eventType") String eventType,
        @JsonProperty("iss") String organizationId,
        @JsonProperty("entityId") String entityId,
        @JsonProperty("previousEntityHash") String previousEntityHash,
        @JsonProperty("dataLocation") String dataLocation,
        @JsonProperty("relevantMetadata") List<String> metadata
) {
}
