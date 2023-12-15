package es.in2.blockchainconnector.service.impl;

import es.in2.blockchainconnector.domain.DLTNotificationDTO;
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
        String entityHash = "entityHash";
        String dataLocation = "sourceBrokerEntityURL?hl=0xb24443caf5b89330d7d9d8c45dad4786f90b3a1da6a526a2de34181c2b20326f";

        DLTNotificationDTO dltNotificationDTO = new DLTNotificationDTO(null, null, null, null, "sourceBrokerEntityURL?hl=0xb24443caf5b89330d7d9d8c45dad4786f90b3a1da6a526a2de34181c2b20326f", null);

        String result = brokerEntityValidationService.validateEntityIntegrity(brokerEntity, dltNotificationDTO).block();
        assertEquals(brokerEntity, result);
    }

    @Test
    void testValidateEntityIntegrity_Failure() throws NoSuchAlgorithmException {
        String brokerEntity = "testEntity";

        DLTNotificationDTO dltNotificationDTO = new DLTNotificationDTO(null, null, null, null, "sourceBrokerEntityURL?hl=wrongHash", null);

        assertThrows(IllegalArgumentException.class, () -> brokerEntityValidationService.validateEntityIntegrity(brokerEntity, dltNotificationDTO).block());
    }
}

