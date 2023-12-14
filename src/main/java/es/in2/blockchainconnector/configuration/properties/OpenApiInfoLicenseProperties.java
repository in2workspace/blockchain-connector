package es.in2.blockchainconnector.configuration.properties;

import org.springframework.boot.context.properties.bind.ConstructorBinding;

import java.util.Optional;

/**
 * OpenApiInfoLicenseProperties
 *
 * @param name - license name
 * @param url - license url
 */
public record OpenApiInfoLicenseProperties(String name, String url) {

    @ConstructorBinding
    public OpenApiInfoLicenseProperties(String name, String url) {
        this.name = Optional.ofNullable(name).orElse("Apache 2.0");
        this.url = Optional.ofNullable(url).orElse("https://www.apache.org/licenses/LICENSE-2.0");
    }

}
