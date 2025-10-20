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

import java.util.Optional;

import static org.gridsuite.network.map.dto.InfoTypeParameters.QUERY_PARAM_LOAD_NETWORK_COMPONENTS;
import static org.gridsuite.network.map.dto.utils.ElementUtils.*;

/**
 * @author Hugo Marcellin <hugo.marcelin at rte-france.com>
 */

public final class BusInfosMapper {
    private BusInfosMapper() {
    }

    public static ElementInfos toData(Identifiable<?> identifiable, InfoTypeParameters infoTypeParameters) {
        boolean shouldLoadNetworkComponents = Optional.ofNullable(infoTypeParameters.getOptionalParameters().get(QUERY_PARAM_LOAD_NETWORK_COMPONENTS))
            .map(Boolean::valueOf)
            .orElse(false);
        return switch (infoTypeParameters.getInfoType()) {
            case LIST -> ElementInfosMapper.toListInfos(identifiable);
            case TAB -> toTabInfos(identifiable, shouldLoadNetworkComponents);
            default -> throw handleUnsupportedInfoType(infoTypeParameters.getInfoType(), "Bus");
        };
    }

    private static BusTabInfos toTabInfos(Identifiable<?> identifiable, boolean shouldLoadNetworkComponents) {
        Bus bus = (Bus) identifiable;
        BusTabInfos.BusTabInfosBuilder<?, ?> builder = BusTabInfos.builder().id(bus.getId())
                .angle(bus.getAngle())
                .v(bus.getV())
                .voltageLevelId(bus.getVoltageLevel().getId())
                .nominalVoltage(bus.getVoltageLevel().getNominalV())
                .country(mapCountry(bus.getVoltageLevel().getSubstation().orElse(null)))
                .properties(getProperties(bus))
                .substationProperties(bus.getVoltageLevel().getSubstation().map(ElementUtils::getProperties).orElse(null))
                .voltageLevelProperties(getProperties(bus.getVoltageLevel()));

        if (shouldLoadNetworkComponents) {
            builder
                .synchronousComponentNum(bus.getSynchronousComponent().getNum())
                .connectedComponentNum(bus.getConnectedComponent().getNum());
        }

        return builder.build();
    }
}
