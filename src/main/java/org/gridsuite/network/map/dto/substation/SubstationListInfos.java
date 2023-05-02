/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.substation;

import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.VoltageLevel;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.gridsuite.network.map.dto.voltagelevel.AbstractVoltageLevelInfos;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
@SuperBuilder
@Getter
public class SubstationListInfos extends AbstractVoltageLevelInfos {

    public static SubstationListInfos toData(Identifiable<?> identifiable) {
        VoltageLevel voltageLevel = (VoltageLevel) identifiable;
        return SubstationListInfos.builder()
                .name(voltageLevel.getOptionalName().orElse(null))
                .id(voltageLevel.getId())
                .build();
    }
}
