package es.in2.blockchainconnector.service.impl;

import es.in2.blockchainconnector.domain.DLTNotificationDTO;
import es.in2.blockchainconnector.exception.HashLinkException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BrokerEntityValidationServiceImplTest {

    @InjectMocks
    private BrokerEntityValidationServiceImpl brokerEntityValidationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testValidateEntityIntegrity() {
        String brokerEntity = "testEntity";


        DLTNotificationDTO dltNotificationDTO = new DLTNotificationDTO(null, null, null, null, "http://mkt1-broker-adapter:8080/api/v1/entities/urn:ngsi-ld:product-offering:443734333?hl=0xb24443caf5b89330d7d9d8c45dad4786f90b3a1da6a526a2de34181c2b20326f", null);

        String result = brokerEntityValidationService.validateEntityIntegrity(brokerEntity, dltNotificationDTO).block();
        assertEquals(brokerEntity, result);
    }

    @Test
    void testValidateEntityIntegrity_deleted() {
        String brokerEntity = "testEntity";


        DLTNotificationDTO dltNotificationDTO = new DLTNotificationDTO(null, null, null, null, "http://mkt1-broker-adapter:8080/api/v1/entities/urn:ngsi-ld:product-offering:443734333", null);

        String result = brokerEntityValidationService.validateEntityIntegrity(brokerEntity, dltNotificationDTO).block();
        assertEquals(brokerEntity, result);
    }

    @Test
    void testValidateEntityIntegrity_Failure() throws NoSuchAlgorithmException {
        String brokerEntity = "testEntity";

        DLTNotificationDTO dltNotificationDTO = new DLTNotificationDTO(null, null, null, null, "sourceBrokerEntityURL?hl=wrongHash", null);

        assertThrows(HashLinkException.class, () -> brokerEntityValidationService.validateEntityIntegrity(brokerEntity, dltNotificationDTO).block());
    }
}

