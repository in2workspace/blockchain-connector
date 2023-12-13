package es.in2.blockchainconnector.facade;

import es.in2.blockchainconnector.domain.OrionLDNotification;
import es.in2.blockchainconnector.domain.ScorpioNotification;
import reactor.core.publisher.Mono;

public interface BlockchainCreationAndPublicationServiceFacade {
    Mono<Void> createAndPublishABlockchainEventIntoBlockchainNode(ScorpioNotification scorpioNotification);
}
