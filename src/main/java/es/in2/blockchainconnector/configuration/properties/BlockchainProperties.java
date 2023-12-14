package es.in2.blockchainconnector.configuration.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import java.util.Optional;

/**
 * Configuration to connect the blockchain
 *
 * @param rpcAddress          - rpc address of the node to be used
 * @param userEthereumAddress - address of the user in the ethereum compatible blockchain
 * @param subscription        - configuration to be used for subscribing at blockchain events
 */
@ConfigurationProperties(prefix = "blockchain")
public record BlockchainProperties(String rpcAddress, String userEthereumAddress,
                                   @NestedConfigurationProperty BlockchainSubscriptionConfigProperties subscription) {

    @ConstructorBinding
    public BlockchainProperties(String rpcAddress, String userEthereumAddress,
                                BlockchainSubscriptionConfigProperties subscription) {
        this.rpcAddress = Optional.ofNullable(rpcAddress).orElse("https://red-t.alastria.io/v0/9461d9f4292b41230527d57ee90652a6");
        this.userEthereumAddress = Optional.ofNullable(userEthereumAddress).orElse("0xb794f5ea0ba39494ce839613fffba74279579268");
        this.subscription = Optional.ofNullable(subscription).orElse(new BlockchainSubscriptionConfigProperties(null, false, null));
    }

}
