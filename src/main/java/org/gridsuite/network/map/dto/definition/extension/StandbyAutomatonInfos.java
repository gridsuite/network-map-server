/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.definition.extension;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

/**
 * @author Rehili Ghazwa <ghazwa.rehili at rte-france.com>
 */
@Getter
@Builder
public class StandbyAutomatonInfos {

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


