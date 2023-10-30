package es.in2.blockchain.connector.integration.orionld.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import java.util.List;
import java.util.Optional;

/**
 * Configuration of the ngsi-ld subscription between the connector and the context broker
 * @param notificationEndpoint - endpoint to be used for notifications from the context broker
 * @param subscriptionType - type to be used for subscriptions
 * @param idPrefix - prefix of the id
 * @param entityTypes - entity types to subscribe to
 */
@ConfigurationProperties(prefix = "ngsi-subscription")
public record NgsiLdSubscriptionConfiguration(String notificationEndpoint, String subscriptionType, String idPrefix,
											  List<String> entityTypes) {

	@ConstructorBinding
	public NgsiLdSubscriptionConfiguration(String notificationEndpoint, String subscriptionType, String idPrefix,
			List<String> entityTypes) {
		this.notificationEndpoint = notificationEndpoint;
		this.subscriptionType = Optional.ofNullable(subscriptionType).orElse("Subscription");
		this.idPrefix = Optional.ofNullable(idPrefix).orElse("urn:ngsi-ld:Subscription:");
		this.entityTypes = Optional.ofNullable(entityTypes).orElse(List.of());
	}
}
