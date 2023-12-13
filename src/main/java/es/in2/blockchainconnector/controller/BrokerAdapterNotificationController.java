package es.in2.blockchainconnector.controller;

import es.in2.blockchainconnector.domain.BrokerNotification;
import es.in2.blockchainconnector.facade.BlockchainCreationAndPublicationServiceFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/notifications/broker")
@RequiredArgsConstructor
public class BrokerAdapterNotificationController {

    private final BlockchainCreationAndPublicationServiceFacade blockchainCreationAndPublicationServiceFacade;

    @PostMapping()
    @ResponseStatus(HttpStatus.OK)
    public Mono<Void> brokerNotification(@RequestBody BrokerNotification brokerNotification) {
        // Create a unique ID for the process
        String processId = UUID.randomUUID().toString();
        // Add the process ID to the Mapped Diagnostic Context (MDC)
        MDC.put("processId", processId);
        // Async Process Start
        log.debug("ProcessID: {} - Broker Notification received: {}", processId, brokerNotification.toString());
        return blockchainCreationAndPublicationServiceFacade
                .createAndPublishABlockchainEventIntoBlockchainNode(brokerNotification)
                .doFinally(signalType -> MDC.remove("processId"));
    }

}
