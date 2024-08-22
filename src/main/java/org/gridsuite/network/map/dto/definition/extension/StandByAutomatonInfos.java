package org.gridsuite.network.map.dto.definition.extension;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

/**
 * @author Rehili Ghazwa <ghazwa.rehili at rte-france.com>
 */

@Getter
@Builder
public class StandByAutomatonInfos {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean standBy;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double b0;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double lowVoltageSetPoint;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double highVoltageSetPoint;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double lowVoltageThreshold;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double highVoltageThreshold;
}


