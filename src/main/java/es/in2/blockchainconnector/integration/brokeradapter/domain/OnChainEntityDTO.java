package es.in2.blockchainconnector.integration.brokeradapter.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OnChainEntityDTO {

    private String id;

    private String eventType;

    private Map<String, Object> dataMap;

    private String data;

}