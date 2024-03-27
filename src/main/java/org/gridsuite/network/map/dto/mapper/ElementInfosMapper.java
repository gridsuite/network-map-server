/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.mapper;

import com.powsybl.iidm.network.Identifiable;
import org.gridsuite.network.map.dto.ElementInfoWithType;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
public final class ElementInfosMapper {
    private ElementInfosMapper() {
    }

    public static ElementInfoWithType toInfosWithType(Identifiable<?> identifiable) {
        return ElementInfoWithType.builder()
                .id(identifiable.getId())
                .name(identifiable.getOptionalName().orElse(null))
                .type(identifiable.getType())
                .build();
    }
}
