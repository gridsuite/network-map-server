package org.gridsuite.network.map.dto.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GeneratorShortCircuitInfos {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double transientReactance;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double stepUpTransformerReactance;

}


