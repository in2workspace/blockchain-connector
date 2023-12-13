package es.in2.blockchainconnector.service;

import es.in2.blockchainconnector.domain.OrionLDNotification;
import es.in2.blockchainconnector.domain.OnChainEventDTO;
import es.in2.blockchainconnector.domain.ScorpioNotification;
import reactor.core.publisher.Mono;

public interface BrokerAdapterNotificationService {

    Mono<OnChainEventDTO> processNotification(ScorpioNotification scorpioNotification);

}
