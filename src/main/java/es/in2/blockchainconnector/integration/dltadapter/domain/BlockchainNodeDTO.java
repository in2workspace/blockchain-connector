package es.in2.blockchainconnector.integration.dltadapter.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BlockchainNodeDTO {

    @JsonProperty("rpcAddress")
    private String rpcAddress;

    @JsonProperty("userEthereumAddress")
    private String userEthereumAddress;

}
