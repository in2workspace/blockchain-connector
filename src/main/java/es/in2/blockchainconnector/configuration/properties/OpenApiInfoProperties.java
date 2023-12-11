package es.in2.blockchainconnector.configuration.properties;

import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import java.util.Optional;

/**
 * OpenApiInfoProperties
 *
 * @param title - title
 * @param version - version
 * @param description - description
 * @param termsOfService - terms of service
 * @param contact - contact information
 * @param license - project license
 */
public record OpenApiInfoProperties(String title, String version, String description, String termsOfService,
                                    @NestedConfigurationProperty OpenApiInfoContactProperties contact,
                                    @NestedConfigurationProperty OpenApiInfoLicenseProperties license) {

    @ConstructorBinding
    public OpenApiInfoProperties(String title, String version, String description, String termsOfService,
                                 OpenApiInfoContactProperties contact, OpenApiInfoLicenseProperties license) {
        this.title = Optional.ofNullable(title).orElse("IN2 Wallet User Registry project");
        this.version = Optional.ofNullable(version).orElse("1.0.0");
        this.description = Optional.ofNullable(description).orElse("This API exposes endpoints to manage the IN2 Wallet User Registry component.");
        this.termsOfService = Optional.ofNullable(termsOfService).orElse("https://example.es/terms-of-service");
        this.contact = Optional.ofNullable(contact).orElse(new OpenApiInfoContactProperties(null, null, null));
        this.license = Optional.ofNullable(license).orElse(new OpenApiInfoLicenseProperties(null, null));
    }

}
