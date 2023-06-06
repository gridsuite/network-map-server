/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.substation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Substation;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.gridsuite.network.map.dto.voltagelevel.VoltageLevelMapInfos;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
@SuperBuilder
@Getter
public class SubstationMapInfos extends AbstractSubstationInfos {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<VoltageLevelMapInfos> voltageLevels;

    public static SubstationMapInfos toData(Identifiable<?> identifiable) {
        Substation substation = (Substation) identifiable;
        return SubstationMapInfos.builder()
                .id(substation.getId())
                .name(substation.getOptionalName().orElse(null))
                .voltageLevels(substation.getVoltageLevelStream().map(VoltageLevelMapInfos::toData).collect(Collectors.toList()))
                .build();
    }
}
