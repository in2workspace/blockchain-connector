package es.in2.blockchainconnector.configuration.properties;

import org.springframework.boot.context.properties.bind.ConstructorBinding;

import java.util.Optional;

/**
 * OpenApiInfoLicenseProperties
 *
 * @param url - server url
 * @param description
 */
public record OpenApiServerProperties(String url, String description) {

    @ConstructorBinding
    public OpenApiServerProperties(String url, String description) {
        this.url = Optional.ofNullable(url).orElse("http://localhost:8080");
        this.description = Optional.ofNullable(description).orElse("Wallet User Registry Local Server");
    }

}
