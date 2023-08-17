/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.definition.hvdc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.powsybl.iidm.network.HvdcConverterStation;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.gridsuite.network.map.dto.ElementInfos;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
@SuperBuilder
@Getter
public class HvdcMapInfos extends ElementInfos {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String voltageLevelId1;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String voltageLevelId2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean terminal1Connected;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean terminal2Connected;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double p1;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double p2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private HvdcConverterStation.HvdcType hvdcType;

}
