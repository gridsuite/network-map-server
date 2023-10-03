package org.gridsuite.network.map.dto.definition.extension;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class IdentifiableShortCircuitInfos {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double ipMin;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double ipMax;

}
