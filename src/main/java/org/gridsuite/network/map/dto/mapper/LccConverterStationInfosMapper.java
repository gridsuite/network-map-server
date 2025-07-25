/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.mapper;

import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.LccConverterStation;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.extensions.Measurement.Type;
import org.gridsuite.network.map.dto.ElementInfos;
import org.gridsuite.network.map.dto.InfoTypeParameters;
import org.gridsuite.network.map.dto.definition.lccconverterstation.LccConverterStationFormInfos;
import org.gridsuite.network.map.dto.definition.lccconverterstation.LccConverterStationTabInfos;
import org.gridsuite.network.map.dto.utils.ElementUtils;
import org.gridsuite.network.map.dto.utils.ExtensionUtils;

import static org.gridsuite.network.map.dto.mapper.HvdcInfosMapper.toShuntCompensatorInfos;
import static org.gridsuite.network.map.dto.utils.ElementUtils.*;

/**
 * @author AJELLAL Ali <ali.ajellal@rte-france.com>
 */

public final class LccConverterStationInfosMapper {
    private LccConverterStationInfosMapper() {
    }

    public static ElementInfos toData(Identifiable<?> identifiable, InfoTypeParameters infoTypeParameters) {
        return switch (infoTypeParameters.getInfoType()) {
            case TAB -> toTabInfos(identifiable);
            case LIST -> ElementInfosMapper.toInfosWithType(identifiable);
            case FORM -> toFormInfos(identifiable);
            default -> throw new UnsupportedOperationException("TODO");
        };
    }

    private static LccConverterStationTabInfos toTabInfos(Identifiable<?> identifiable) {
        LccConverterStation lccConverterStation = (LccConverterStation) identifiable;
        Terminal terminal = lccConverterStation.getTerminal();
        LccConverterStationTabInfos.LccConverterStationTabInfosBuilder<?, ?> builder = LccConverterStationTabInfos.builder();
        builder
                .name(lccConverterStation.getOptionalName().orElse(null))
                .id(lccConverterStation.getId())
                .voltageLevelId(terminal.getVoltageLevel().getId())
                .nominalV(terminal.getVoltageLevel().getNominalV())
                .country(mapCountry(terminal.getVoltageLevel().getSubstation().orElse(null)))
                .terminalConnected(terminal.isConnected())
                .lossFactor(lccConverterStation.getLossFactor())
                .properties(getProperties(lccConverterStation))
                .powerFactor(lccConverterStation.getPowerFactor());

        if (!Double.isNaN(terminal.getP())) {
            builder.p(terminal.getP());
        }
        if (!Double.isNaN(terminal.getQ())) {
            builder.q(terminal.getQ());
        }
        if (lccConverterStation.getHvdcLine() != null) {
            builder.hvdcLineId(lccConverterStation.getHvdcLine().getId());
        }

        // voltageLevel and substation properties
        builder.voltageLevelProperties(getProperties(terminal.getVoltageLevel()));
        builder.substationProperties(terminal.getVoltageLevel().getSubstation().map(ElementUtils::getProperties).orElse(null));

        builder.measurementP(ExtensionUtils.toMeasurement(lccConverterStation, Type.ACTIVE_POWER, 0))
            .measurementQ(ExtensionUtils.toMeasurement(lccConverterStation, Type.REACTIVE_POWER, 0));

        builder.injectionObservability(ExtensionUtils.toInjectionObservability(lccConverterStation));

        return builder.build();
    }

    static LccConverterStationFormInfos toFormInfos(Identifiable<?> identifiable) {
        LccConverterStation lccConverterStation = (LccConverterStation) identifiable;
        Terminal terminal = lccConverterStation.getTerminal();
        LccConverterStationFormInfos.LccConverterStationFormInfosBuilder<?, ?> builder = LccConverterStationFormInfos.builder();
        builder
                .name(lccConverterStation.getOptionalName().orElse(null))
                .id(lccConverterStation.getId())
                .voltageLevelId(terminal.getVoltageLevel().getId())
                .busOrBusbarSectionId(getBusOrBusbarSection(terminal))
                .terminalConnected(terminal.isConnected())
                .connectablePosition(ExtensionUtils.toMapConnectablePosition(lccConverterStation, 0))
                .lossFactor(lccConverterStation.getLossFactor())
                .powerFactor(lccConverterStation.getPowerFactor());
        builder.shuntCompensatorsOnSide(toShuntCompensatorInfos(getBusOrBusbarSection(terminal), terminal.getVoltageLevel().getShuntCompensatorStream()));
        return builder.build();
    }
}
