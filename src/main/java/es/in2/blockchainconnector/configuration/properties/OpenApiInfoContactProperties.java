package es.in2.blockchainconnector.configuration.properties;

import org.springframework.boot.context.properties.bind.ConstructorBinding;

import java.util.Optional;

/**
 * OpenApiInfoContactProperties
 *
 * @param email - contact email
 * @param name - contact name
 * @param url - organization url
 */
public record OpenApiInfoContactProperties(String email, String name, String url) {

    @ConstructorBinding
    public OpenApiInfoContactProperties(String email, String name, String url) {
        this.email = Optional.ofNullable(email).orElse("info@example.es");
        this.name = Optional.ofNullable(name).orElse("John Doe");
        this.url = Optional.ofNullable(url).orElse("https://example.es");
    }

}
