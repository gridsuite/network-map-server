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
import org.gridsuite.network.map.dto.definition.bus.BusTabInfos;
import org.gridsuite.network.map.dto.utils.ElementUtils;

import static org.gridsuite.network.map.dto.utils.ElementUtils.getProperties;
import static org.gridsuite.network.map.dto.utils.ElementUtils.mapCountry;

/**
 * @author Hugo Marcellin <hugo.marcelin at rte-france.com>
 */

public final class BusInfosMapper {
    private BusInfosMapper() {
    }

    public static ElementInfos toData(Identifiable<?> identifiable, InfoTypeParameters infoTypeParameters) {
        switch (infoTypeParameters.getInfoType()) {
            case LIST:
                return ElementInfosMapper.toListInfos(identifiable);
            case TAB:
                return toTabInfos(identifiable);
            default:
                throw new UnsupportedOperationException("TODO");
        }
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
                .connectedComponentNum(bus.getConnectedComponent().getNum())
                .substationProperties(bus.getVoltageLevel().getSubstation().map(ElementUtils::getProperties).orElse(null))
                .voltageLevelProperties(getProperties(bus.getVoltageLevel()));

        return builder.build();
    }
}
