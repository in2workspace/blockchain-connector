package es.in2.blockchainconnector.configuration;

import es.in2.blockchainconnector.configuration.properties.*;
import es.in2.blockchainconnector.exception.HashCreationException;
import es.in2.blockchainconnector.utils.Utils;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.NoSuchAlgorithmException;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {


    private final OpenApiProperties openApiProperties;
    private final OperatorProperties operatorProperties;
    private final BlockchainProperties blockchainProperties;
    private final DLTAdapterProperties dltAdapterProperties;
    private final BrokerProperties brokerProperties;
    private final BrokerAdapterProperties brokerAdapterProperties;
    private final NgsiLdSubscriptionConfigProperties ngsiLdSubscriptionConfigProperties;

    @PostConstruct
    public void init() {
        log.debug("OpenApi properties: {}", openApiProperties);
        log.debug("Operator properties: {}", operatorProperties);
        log.debug("Blockchain properties: {}", blockchainProperties);
        log.debug("DLT adapter properties: {}", dltAdapterProperties);
        log.debug("Broker properties: {}", brokerProperties);
        log.debug("Broker adapter properties: {}", brokerAdapterProperties);
        log.debug("NgsiLdSubscriptionConfigProperties properties: {}", ngsiLdSubscriptionConfigProperties);
    }

    @Bean
    public String organizationIdHash() {
        try {
            return Utils.calculateSHA256Hash(operatorProperties.organizationId());
        } catch (NoSuchAlgorithmException e) {
            throw new HashCreationException("Error creating organizationId hash: " + e.getMessage());
        }
    }

}
