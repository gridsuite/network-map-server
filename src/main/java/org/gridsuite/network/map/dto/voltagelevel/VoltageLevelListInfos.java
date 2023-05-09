/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.voltagelevel;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.VoltageLevel;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
@SuperBuilder
@Getter
public class VoltageLevelListInfos extends AbstractVoltageLevelInfos {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String substationId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private TopologyKind topologyKind;

    public static VoltageLevelListInfos toData(Identifiable<?> identifiable) {
        VoltageLevel voltageLevel = (VoltageLevel) identifiable;
        return VoltageLevelListInfos.builder()
                .name(voltageLevel.getOptionalName().orElse(null))
                .id(voltageLevel.getId())
                .substationId(voltageLevel.getSubstation().map(Substation::getId).orElse(null))
                .topologyKind(voltageLevel.getTopologyKind())
                .build();
    }
}