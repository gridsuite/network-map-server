/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.mapper;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Identifiable;
import org.gridsuite.network.map.dto.ElementInfos;
import org.gridsuite.network.map.dto.InfoTypeParameters;
import org.gridsuite.network.map.dto.definition.bus.BusListInfos;
import org.gridsuite.network.map.dto.definition.bus.BusTabInfos;

import static org.gridsuite.network.map.dto.utils.ElementUtils.getProperties;
import static org.gridsuite.network.map.dto.utils.ElementUtils.mapCountry;

/**
 * @author Hugo Marcellin <hugo.marcelin at rte-france.com>
 */

public final class BusInfosMapper {
    private BusInfosMapper() {
    }

    public static ElementInfos toData(Identifiable<?> identifiable, ElementInfos.InfoType infoType, @SuppressWarnings("squid:S1172") InfoTypeParameters additionalOptionalParams) {
        switch (infoType) {
            case LIST:
                return toListInfos(identifiable);
            case TAB:
                return toTabInfos(identifiable);
            default:
                throw new UnsupportedOperationException("TODO");
        }
    }

    public static BusListInfos toListInfos(Identifiable<?> identifiable) {
        Bus bus = (Bus) identifiable;
        return BusListInfos.builder()
                .id(bus.getId())
                .name(bus.getOptionalName().orElse(null))
                .build();
    }

    public static BusTabInfos toTabInfos(Identifiable<?> identifiable) {
        Bus bus = (Bus) identifiable;
        BusTabInfos.BusTabInfosBuilder<?, ?> builder = BusTabInfos.builder().id(bus.getId())
                .angle(bus.getAngle())
                .v(bus.getV())
                .voltageLevelId(bus.getVoltageLevel().getId())
                .nominalVoltage(bus.getVoltageLevel().getNominalV())
                .country(mapCountry(bus.getVoltageLevel().getSubstation().orElse(null)))
                .synchronousComponentNum(bus.getSynchronousComponent().getNum())
                .properties(getProperties(bus))
                .connectedComponentNum(bus.getConnectedComponent().getNum());

        return builder.build();
    }
}
