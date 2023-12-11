package es.in2.blockchainconnector.configuration.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import java.util.Optional;

/**
 * Configuration to connect the dtl-adapter at
 *
 * @param domain - domain of the adapter
 * @param paths  - paths to be used with the adapter instance
 */
@ConfigurationProperties(prefix = "dlt-adapter")
public record DLTAdapterProperties(String domain, @NestedConfigurationProperty DLTAdapterPathProperties paths) {

    @ConstructorBinding
    public DLTAdapterProperties(String domain, DLTAdapterPathProperties paths) {
        this.domain = Optional.ofNullable(domain).orElse("http://localhost:8080");
        this.paths = Optional.ofNullable(paths).orElse(new DLTAdapterPathProperties(null, null, null));
    }

}
