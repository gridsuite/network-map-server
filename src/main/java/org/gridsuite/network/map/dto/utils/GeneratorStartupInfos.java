package org.gridsuite.network.map.dto.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GeneratorStartupInfos {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double plannedActivePowerSetPoint;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double marginalCost;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double plannedOutageRate;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double forcedOutageRate;
}
