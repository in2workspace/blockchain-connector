package es.in2.blockchainconnector.integration.brokeradapter.controller;

import es.in2.blockchainconnector.integration.brokeradapter.domain.OrionLdNotificationDTO;
import es.in2.blockchainconnector.integration.brokeradapter.service.BrokerAdapterNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/notifications/broker")
@RequiredArgsConstructor
public class BrokerAdapterNotificationController {

    private final BrokerAdapterNotificationService brokerAdapterNotificationService;

    @PostMapping()
    @ResponseStatus(HttpStatus.OK)
    public Mono<Void> brokerNotification(@RequestBody OrionLdNotificationDTO orionLdNotificationDTO) {
        log.debug("Notification received: {}", orionLdNotificationDTO.toString());
        return brokerAdapterNotificationService.processNotification(orionLdNotificationDTO);
    }

}
