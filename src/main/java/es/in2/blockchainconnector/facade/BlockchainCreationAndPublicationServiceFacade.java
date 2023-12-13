package es.in2.blockchainconnector.facade;

import es.in2.blockchainconnector.domain.BrokerNotification;
import reactor.core.publisher.Mono;

public interface BlockchainCreationAndPublicationServiceFacade {
    Mono<Void> createAndPublishABlockchainEventIntoBlockchainNode(BrokerNotification brokerNotification);
}
