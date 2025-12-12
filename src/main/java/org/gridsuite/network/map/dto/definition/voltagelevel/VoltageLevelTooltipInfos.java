/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.definition.voltagelevel;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.gridsuite.network.map.dto.ElementInfosWithProperties;

import java.util.List;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
@SuperBuilder
@Getter
@Setter
public class VoltageLevelTooltipInfos extends ElementInfosWithProperties {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double uMin;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double uMax;

    private List<VoltageLevelBusTooltipInfos> busInfos;

    @Builder
    @Getter
    @Setter
    public static class VoltageLevelBusTooltipInfos {
        private String id;
        private Double u;
        private Double angle;
        private Double generation;
        private Double load;
        private Double balance;
        private Double icc;
    }

}
