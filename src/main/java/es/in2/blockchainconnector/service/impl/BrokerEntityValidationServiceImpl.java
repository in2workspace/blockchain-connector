package es.in2.blockchainconnector.service.impl;

import es.in2.blockchainconnector.domain.DLTNotificationDTO;
import es.in2.blockchainconnector.service.BrokerEntityValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import static es.in2.blockchainconnector.utils.Utils.calculateSHA256Hash;
import static es.in2.blockchainconnector.utils.Utils.hasHLParameter;

@Slf4j
@Service
@RequiredArgsConstructor
public class BrokerEntityValidationServiceImpl implements BrokerEntityValidationService {

    @Override
    public Mono<String> validateEntityIntegrity(String brokerEntity, DLTNotificationDTO dltNotificationDTO) {
        try {
            log.debug(" > Validating entity integrity...");
            if(!hasHLParameter(dltNotificationDTO.dataLocation())) {
                log.debug(" > Detected deleted entity notification");
                return Mono.just(brokerEntity);
            }
            // Create Hash from the retrieved entity
            String entityHash = calculateSHA256Hash(brokerEntity);
            log.debug(" > Entity hash: {}", entityHash);
            // Get Hash from the dataLocation
            // Get URL from the DLTNotificationDTO.dataLocation()
            String dataLocation = dltNotificationDTO.dataLocation();
            log.debug(" > Data location: {}", dataLocation);
            String sourceBrokerEntityURL = Arrays.stream(dataLocation.split("\\?hl="))
                    .findFirst()
                    .orElseThrow(IllegalArgumentException::new);
            log.debug(" > Source broker entity URL: {}", sourceBrokerEntityURL);
            // Get Hash from the dataLocation
            String sourceEntityHash = dataLocation
                    .replace(sourceBrokerEntityURL, "")
                    .replace("?hl=", "");
            log.debug(" > Source entity hash: {}", sourceEntityHash);
            log.debug(" > Extracted Broker entity: {}", brokerEntity);
            // Compare both hashes
            if (entityHash.equals(sourceEntityHash)) {
                log.debug(" > Entity integrity is valid");
                return Mono.just(brokerEntity);
            } else {
                log.error(" > Entity integrity is not valid");
                return Mono.error(new IllegalArgumentException("Entity integrity cannot be validated"));
            }
        } catch (NoSuchAlgorithmException e) {
            log.error("Error validating entity integrity: {}", e.getMessage(), e.getCause());
            return Mono.error(e);
        }
    }

}
