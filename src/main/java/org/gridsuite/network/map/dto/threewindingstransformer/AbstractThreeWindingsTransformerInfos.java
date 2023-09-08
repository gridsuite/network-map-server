/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.threewindingstransformer;

import com.powsybl.iidm.network.Identifiable;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.gridsuite.network.map.dto.ElementInfos;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
@SuperBuilder
@Getter
public abstract class AbstractThreeWindingsTransformerInfos extends ElementInfos {
    public static ElementInfos toData(Identifiable<?> identifiable, ElementInfoType dataType) {
        switch (dataType.getInfoType()) {
            case LIST:
                return ThreeWindingsTransformerListInfos.toData(identifiable);
            case TAB:
                return ThreeWindingsTransformerTabInfos.toData(identifiable);
            default:
                throw new UnsupportedOperationException("TODO");
        }
    }
}
