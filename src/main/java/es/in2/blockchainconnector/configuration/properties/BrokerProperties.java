package es.in2.blockchainconnector.configuration.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import java.util.Optional;

/**
 * Configuration intended to connect the NGSI-LD ContextBroker
 *
 * @param externalDomain - domain that the broker is externally available. Used for the hashlink.
 * @param internalDomain - internal address of the broker, used to connect from within the connector
 * @param paths          - ngis-ld paths to be used when connecting the broker
 */
@ConfigurationProperties(prefix = "broker")
public record BrokerProperties(String externalDomain, String internalDomain,
                               @NestedConfigurationProperty BrokerPathProperties paths) {

    @ConstructorBinding
    public BrokerProperties(String externalDomain, String internalDomain, BrokerPathProperties paths) {
        this.externalDomain = Optional.ofNullable(externalDomain).orElse("https://emxample.org/scorpio");
        this.internalDomain = Optional.ofNullable(internalDomain).orElse("http://localhost:1026");
        this.paths = Optional.ofNullable(paths).orElse(new BrokerPathProperties(null, null));
    }

}
