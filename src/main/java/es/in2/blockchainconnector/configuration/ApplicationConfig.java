package es.in2.blockchainconnector.configuration;

import es.in2.blockchainconnector.configuration.properties.OperatorProperties;
import es.in2.blockchainconnector.exception.HashCreationException;
import es.in2.blockchainconnector.utils.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.NoSuchAlgorithmException;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {

    private final OperatorProperties operatorProperties;

    @Bean
    public String organizationIdHash() {
        try {
            return Utils.calculateSHA256Hash(operatorProperties.organizationId());
        } catch (NoSuchAlgorithmException e) {
            throw new HashCreationException("Error creating organizationId hash: " + e.getMessage());
        }
    }

}
