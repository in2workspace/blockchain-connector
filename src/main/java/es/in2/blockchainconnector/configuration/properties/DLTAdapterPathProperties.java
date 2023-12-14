package es.in2.blockchainconnector.configuration.properties;

import org.springframework.boot.context.properties.bind.ConstructorBinding;

import java.util.Optional;

/**
 * DLTAdapterPathProperties
 *
 * @param configureNode - configure node path
 * @param publish - publish path
 * @param subscribe - subscribe path
 */
public record DLTAdapterPathProperties(String configureNode, String publish, String subscribe) {

    @ConstructorBinding
    public DLTAdapterPathProperties(String configureNode, String publish, String subscribe) {
        this.configureNode = Optional.ofNullable(configureNode).orElse("/api/v1/configureNode");
        this.publish = Optional.ofNullable(publish).orElse("/api/v1/publishEvent");
        this.subscribe = Optional.ofNullable(subscribe).orElse("/api/v1/subscribe");
    }

}

