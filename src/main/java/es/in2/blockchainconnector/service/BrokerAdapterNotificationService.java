package es.in2.blockchainconnector.service;

import es.in2.blockchainconnector.domain.OnChainEventDTO;
import es.in2.blockchainconnector.domain.BrokerNotification;
import reactor.core.publisher.Mono;

public interface BrokerAdapterNotificationService {

    Mono<OnChainEventDTO> processNotification(BrokerNotification brokerNotification);

}
