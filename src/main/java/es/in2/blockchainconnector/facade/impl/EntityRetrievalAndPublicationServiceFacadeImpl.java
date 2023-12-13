package es.in2.blockchainconnector.facade.impl;

import es.in2.blockchainconnector.domain.DLTNotificationDTO;
import es.in2.blockchainconnector.facade.EntityRetrievalAndPublicationServiceFacade;
import es.in2.blockchainconnector.service.BrokerEntityPublicationService;
import es.in2.blockchainconnector.service.BrokerEntityRetrievalService;
import es.in2.blockchainconnector.service.BrokerEntityValidationService;
import es.in2.blockchainconnector.service.DltAdapterNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class EntityRetrievalAndPublicationServiceFacadeImpl implements EntityRetrievalAndPublicationServiceFacade {

    private final DltAdapterNotificationService dltAdapterNotificationService;
    private final BrokerEntityRetrievalService brokerEntityRetrievalService;
    private final BrokerEntityValidationService brokerEntityValidationService;
    private final BrokerEntityPublicationService brokerEntityPublicationService;

    @Override
    public Mono<Void> retrieveAndPublishEntityIntoBroker(DLTNotificationDTO dltNotificationDTO) {
        String processId = MDC.get("processId");
        return dltAdapterNotificationService.processNotification(dltNotificationDTO).doOnSuccess(voidValue -> log.info("ProcessID: {} - DLT Notification processed successfully", processId)).then(Mono.defer(() -> brokerEntityRetrievalService.retrieveEntityFromSourceBroker(processId, dltNotificationDTO))).flatMap(entityString -> {
            log.info("ProcessID: {} - Entity retrieved successfully {}", processId, entityString);
            return brokerEntityValidationService.validateEntityIntegrity(entityString, dltNotificationDTO);
        }).flatMap(validatedEntity -> {
            log.info("ProcessID: {} - Entity validated successfully {}", processId, validatedEntity);
            return brokerEntityPublicationService.publishOrDeleteAnEntityIntoContextBroker(processId, dltNotificationDTO, validatedEntity);
        }).doOnSuccess(voidValue -> log.info("ProcessID: {} - Entity retrieval, validation, and publication completed", processId));
    }

}
