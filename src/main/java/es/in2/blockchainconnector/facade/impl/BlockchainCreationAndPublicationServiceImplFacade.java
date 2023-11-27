package es.in2.blockchainconnector.facade.impl;

import es.in2.blockchainconnector.domain.BrokerNotificationDTO;
import es.in2.blockchainconnector.facade.BlockchainCreationAndPublicationServiceFacade;
import es.in2.blockchainconnector.service.BlockchainEventCreationService;
import es.in2.blockchainconnector.service.BlockchainEventPublicationService;
import es.in2.blockchainconnector.service.BrokerAdapterNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlockchainCreationAndPublicationServiceImplFacade implements BlockchainCreationAndPublicationServiceFacade {

    private final BrokerAdapterNotificationService brokerAdapterNotificationService;
    private final BlockchainEventCreationService blockchainEventCreationService;
    private final BlockchainEventPublicationService blockchainEventPublicationService;

    @Override
    public Mono<Void> createAndPublishABlockchainEventIntoBlockchainNode(BrokerNotificationDTO brokerNotificationDTO) {
        String processId = MDC.get("processId");
        return brokerAdapterNotificationService.processNotification(brokerNotificationDTO)
                .doOnSuccess(voidValue -> log.info("ProcessID: {} - Broker Notification processed successfully", processId))
                .flatMap(onchainEventDTO -> blockchainEventCreationService.createBlockchainEvent(onchainEventDTO, processId))
                .doOnSuccess(voidValue -> log.info("ProcessID: {} - Blockchain Event created successfully", processId))
                .flatMap(onchainEvent -> blockchainEventPublicationService.publishBlockchainEventIntoBlockchainNode(onchainEvent, processId))
                .doOnSuccess(voidValue -> log.info("ProcessID: {} - Blockchain Event published successfully", processId))
                .doOnError(error -> log.error("Error creating or publishing Blockchain Event: {}", error.getMessage(), error));
    }
}
