/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.mapper;

import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.Identifiable;
import org.gridsuite.network.map.dto.ElementInfos;
import org.gridsuite.network.map.dto.definition.voltagelevelconnectables.VoltageLevelConnectableListInfos;

public final class VoltageLevelConnectableInfosMapper {

    private VoltageLevelConnectableInfosMapper() {
    }

    public static ElementInfos toData(Identifiable<?> identifiable, ElementInfos.ElementInfoType dataType) {
        switch (dataType.getInfoType()) {
            case LIST:
                return toListInfos(identifiable);
            default:
                throw new UnsupportedOperationException("TODO");
        }
    }

    private static VoltageLevelConnectableListInfos toListInfos(Identifiable<?> identifiable) {
        Connectable<?> voltageLevelConnectable = (Connectable<?>) identifiable;
        return VoltageLevelConnectableListInfos.builder()
                .id(voltageLevelConnectable.getId())
                .name(voltageLevelConnectable.getOptionalName().orElse(null))
                .type(voltageLevelConnectable.getType())
                .build();
    }
}
