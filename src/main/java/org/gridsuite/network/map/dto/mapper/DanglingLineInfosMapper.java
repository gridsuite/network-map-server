/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.mapper;

import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.extensions.Measurement;
import org.gridsuite.network.map.dto.ElementInfos;
import org.gridsuite.network.map.dto.InfoTypeParameters;
import org.gridsuite.network.map.dto.definition.danglingline.DanglingLineTabInfos;
import org.gridsuite.network.map.dto.utils.ElementUtils;

import static org.gridsuite.network.map.dto.utils.ElementUtils.*;

/**
 * @author AJELLAL Ali <ali.ajellal@rte-france.com>
 */
public final class DanglingLineInfosMapper {
    private DanglingLineInfosMapper() {
    }

    public static ElementInfos toData(Identifiable<?> identifiable, InfoTypeParameters infoTypeParameters) {
        return switch (infoTypeParameters.getInfoType()) {
            case TAB -> toTabInfos(identifiable);
            case LIST -> ElementInfosMapper.toInfosWithType(identifiable);
            default -> throw new UnsupportedOperationException("TODO");
        };
    }

    private static DanglingLineTabInfos toTabInfos(Identifiable<?> identifiable) {
        DanglingLine danglingLine = (DanglingLine) identifiable;
        Terminal terminal = danglingLine.getTerminal();
        DanglingLineTabInfos.DanglingLineTabInfosBuilder<?, ?> builder = DanglingLineTabInfos.builder()
                .name(danglingLine.getOptionalName().orElse(null))
                .id(danglingLine.getId())
                .terminalConnected(terminal.isConnected())
                .voltageLevelId(terminal.getVoltageLevel().getId())
                .nominalV(terminal.getVoltageLevel().getNominalV())
                .country(mapCountry(terminal.getVoltageLevel().getSubstation().orElse(null)))
                .pairingKey(danglingLine.getPairingKey())
                .p0(danglingLine.getP0())
                .properties(getProperties(danglingLine))
                .q0(danglingLine.getQ0())
                .p(nullIfNan(terminal.getP()))
                .q(nullIfNan(terminal.getQ()))
                .i(nullIfNan(terminal.getI()));

        // voltageLevel and substation properties
        builder.voltageLevelProperties(getProperties(terminal.getVoltageLevel()));
        builder.substationProperties(terminal.getVoltageLevel().getSubstation().map(ElementUtils::getProperties).orElse(null));

        builder.measurementP(toMeasurement(danglingLine, Measurement.Type.ACTIVE_POWER, 0))
            .measurementQ(toMeasurement(danglingLine, Measurement.Type.REACTIVE_POWER, 0));

        builder.injectionObservability(toInjectionObservability(danglingLine));

        return builder.build();
    }
}
