package es.in2.blockchainconnector.configuration.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import java.util.Optional;

/**
 * Configuration to connect with the orion-ld-adapter.
 *
 * @param domain - domain of the adapter
 * @param paths  - paths to be used with that adapter instance
 */
@ConfigurationProperties(prefix = "broker-adapter")
public record BrokerAdapterProperties(String domain, @NestedConfigurationProperty BrokerAdapterPathProperties paths) {

    @ConstructorBinding
    public BrokerAdapterProperties(String domain, BrokerAdapterPathProperties paths) {
        this.domain = domain;
        this.paths = Optional.ofNullable(paths).orElse(new BrokerAdapterPathProperties(null, null));
    }

}

