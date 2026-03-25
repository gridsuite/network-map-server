/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.mapper;

import com.powsybl.iidm.network.BoundaryLine;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.extensions.Measurement.Type;
import org.gridsuite.network.map.dto.ElementInfos;
import org.gridsuite.network.map.dto.InfoTypeParameters;
import org.gridsuite.network.map.dto.definition.boundaryline.BoundaryLineTabInfos;
import org.gridsuite.network.map.dto.utils.ElementUtils;
import org.gridsuite.network.map.dto.utils.ExtensionUtils;

import static org.gridsuite.network.map.dto.utils.ElementUtils.*;

/**
 * @author AJELLAL Ali <ali.ajellal@rte-france.com>
 */
public final class BoundaryLineInfosMapper {
    private BoundaryLineInfosMapper() {
    }

    public static ElementInfos toData(Identifiable<?> identifiable, InfoTypeParameters infoTypeParameters) {
        return switch (infoTypeParameters.getInfoType()) {
            case TAB -> toTabInfos(identifiable);
            case LIST -> ElementInfosMapper.toInfosWithType(identifiable);
            default -> throw handleUnsupportedInfoType(infoTypeParameters.getInfoType(), "BoundaryLine");
        };
    }

    private static BoundaryLineTabInfos toTabInfos(Identifiable<?> identifiable) {
        BoundaryLine boundaryLine = (BoundaryLine) identifiable;
        Terminal terminal = boundaryLine.getTerminal();
        BoundaryLineTabInfos.BoundaryLineTabInfosBuilder<?, ?> builder = BoundaryLineTabInfos.builder()
                .name(boundaryLine.getOptionalName().orElse(null))
                .id(boundaryLine.getId())
                .terminalConnected(terminal.isConnected())
                .voltageLevelId(terminal.getVoltageLevel().getId())
                .nominalV(terminal.getVoltageLevel().getNominalV())
                .country(mapCountry(terminal.getVoltageLevel().getSubstation().orElse(null)))
                .pairingKey(boundaryLine.getPairingKey())
                .p0(boundaryLine.getP0())
                .properties(getProperties(boundaryLine))
                .q0(boundaryLine.getQ0())
                .p(nullIfNan(terminal.getP()))
                .q(nullIfNan(terminal.getQ()))
                .i(nullIfNan(terminal.getI()));

        // voltageLevel and substation properties
        builder.voltageLevelProperties(getProperties(terminal.getVoltageLevel()));
        builder.substationProperties(terminal.getVoltageLevel().getSubstation().map(ElementUtils::getProperties).orElse(null));

        builder.measurementP(ExtensionUtils.toMeasurement(boundaryLine, Type.ACTIVE_POWER, 0))
            .measurementQ(ExtensionUtils.toMeasurement(boundaryLine, Type.REACTIVE_POWER, 0));

        builder.injectionObservability(ExtensionUtils.toInjectionObservability(boundaryLine));

        return builder.build();
    }
}
