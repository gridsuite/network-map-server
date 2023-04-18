/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.substation;

import com.powsybl.iidm.network.Identifiable;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.gridsuite.network.map.dto.ElementInfos;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
@SuperBuilder
@Getter
public class SubstationInfos extends ElementInfos {

    public static SubstationInfos toData(Identifiable identifiable, InfoType dataType)  {
        switch (dataType) {
            case TAB:
                return SubstationTabInfos.toData(identifiable);
            default:
                throw new UnsupportedOperationException("TODO");
        }
    }
}
