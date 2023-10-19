package es.in2.blockchain.connector.integration.blockchainnode.configuration;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Component
public class BlockchainNodeProperties {

    @Value("${blockchain-node-if.api.domain}")
    private String apiDomain;

    @Value("${blockchain-node-if.api.path.configure-node}")
    private String apiPathConfigureNode;

    @Value("${blockchain-node-if.api.path.publish}")
    private String apiPathPublish;

    @Value("${blockchain-node-if.api.path.subscribe}")
    private String apiPathSubscribe;

    @Value("${blockchain-node-if.subscription.notification-endpoint-uri}")
    private String subscriptionNotificationEndpointUri;

    @Value("${blockchain-node-if.subscription.event-types}")
    private List<String> subscriptionEventTypeList;

    @Value("${blockchain-node-if.node.rpcAddress}")
    private String nodeRpcAddress;

    @Value("${blockchain-node-if.node.userEthereumAddress}")
    private String nodeUserEthereumAddress;

    @Value("${blockchain-node-if.subscription.active}")
    private boolean subscriptionActive;

}
