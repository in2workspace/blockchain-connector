package es.in2.blockchainconnector.configuration.properties;

import org.springframework.boot.context.properties.bind.ConstructorBinding;

import java.util.Optional;

public record BrokerAdapterPathProperties(String entities, String subscriptions) {

    @ConstructorBinding
    public BrokerAdapterPathProperties(String entities, String subscriptions) {
        this.entities = Optional.ofNullable(entities).orElse("/api/v1/entities");
        this.subscriptions = Optional.ofNullable(subscriptions).orElse("/api/v1/subscriptions");
    }

}