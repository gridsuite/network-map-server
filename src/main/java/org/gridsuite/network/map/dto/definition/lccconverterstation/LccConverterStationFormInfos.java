/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.network.map.dto.definition.lccconverterstation;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.gridsuite.network.map.config.nan.NullAndNaNFilter;
import org.gridsuite.network.map.dto.ElementInfos;
import org.gridsuite.network.map.dto.definition.extension.ConnectablePositionInfos;
import org.gridsuite.network.map.dto.definition.hvdc.HvdcShuntCompensatorsInfos;

import java.util.List;

/**
 * @author Rehili Ghazwa <ghazwa.rehili at rte-france.com>
 */

@SuperBuilder
@Getter
public class LccConverterStationFormInfos extends ElementInfos {
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NullAndNaNFilter.class)
    private Float powerFactor;

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NullAndNaNFilter.class)
    private Float lossFactor;

    private String voltageLevelId;

    private Boolean terminalConnected;

    private ConnectablePositionInfos connectablePosition;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String busOrBusbarSectionId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<HvdcShuntCompensatorsInfos.ShuntCompensatorInfos> shuntCompensatorsOnSide;
}

