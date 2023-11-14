/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.mapper;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Identifiable;
import org.gridsuite.network.map.dto.ElementInfos;

public final class BusInfosMapper {
    private BusInfosMapper() {
    }

    public static ElementInfos toData(Identifiable<?> identifiable, ElementInfos.ElementInfoType dataType) {
        switch (dataType.getInfoType()) {
            case LIST:
                return toListInfos(identifiable);
            default:
                throw new UnsupportedOperationException("TODO");
        }
    }

    private static ElementInfos toListInfos(Identifiable<?> identifiable) {
        Bus bus = (Bus) identifiable;
        return ElementInfos.builder()
                .id(bus.getId())
                .name(bus.getOptionalName().orElse(null))
                .build();
    }
}
