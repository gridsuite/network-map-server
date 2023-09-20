package org.gridsuite.network.map.dto.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class IdentifiableShortCircuitInfos {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double ipMin;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double ipMax;
}
