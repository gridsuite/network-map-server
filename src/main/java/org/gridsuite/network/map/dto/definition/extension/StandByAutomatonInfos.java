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
    private Boolean standby;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double b0;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double lowVoltageSetpoint;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double highVoltageSetpoint;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double lowVoltageThreshold;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double highVoltageThreshold;
}


