/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.network.map.dto.definition.hvdc.hvdcVsc;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.gridsuite.network.map.dto.definition.extension.HvdcAngleDroopActivePowerControlInfos;
import org.gridsuite.network.map.dto.definition.extension.HvdcOperatorActivePowerRangeInfos;
import org.gridsuite.network.map.dto.definition.hvdc.HvdcFormInfos;
import org.gridsuite.network.map.dto.definition.vscconverterstation.VscConverterStationFormInfos;

import java.util.Optional;

/**
 * @author Seddik Yengui <seddik.yengui at rte-france.com>
 */

@SuperBuilder
@Getter
public class HvdcVscFormInfos extends HvdcFormInfos {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private VscConverterStationFormInfos converterStation1;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private VscConverterStationFormInfos converterStation2;

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public Optional<HvdcAngleDroopActivePowerControlInfos> hvdcAngleDroopActivePowerControl;

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private Optional<HvdcOperatorActivePowerRangeInfos> hvdcOperatorActivePowerRange;
}

