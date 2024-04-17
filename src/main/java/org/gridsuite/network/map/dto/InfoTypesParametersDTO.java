package org.gridsuite.network.map.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class InfoTypesParametersDTO {
    Map<String, String> additionalParams;
}
