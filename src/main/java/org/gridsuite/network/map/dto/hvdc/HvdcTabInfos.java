/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.hvdc;

import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.Identifiable;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
@SuperBuilder
@Getter
public class HvdcTabInfos extends HvdcInfos {

    public static HvdcTabInfos toData(Identifiable<?> identifiable) {
        HvdcLine hvdcLine = (HvdcLine) identifiable;

        return HvdcTabInfos.builder()
                .name(hvdcLine.getOptionalName().orElse(null))
                .id(hvdcLine.getId())
                .build();
    }
}
