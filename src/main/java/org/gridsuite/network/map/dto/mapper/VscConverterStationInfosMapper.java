/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.mapper;

import com.powsybl.iidm.network.*;
import com.powsybl.network.store.iidm.impl.MinMaxReactiveLimitsImpl;
import org.gridsuite.network.map.dto.ElementInfos;
import org.gridsuite.network.map.dto.InfoTypeParameters;
import org.gridsuite.network.map.dto.definition.vscconverterstation.VscConverterStationFormInfos;
import org.gridsuite.network.map.dto.definition.vscconverterstation.VscConverterStationTabInfos;
import org.gridsuite.network.map.dto.common.MinMaxReactiveLimitsMapData;

import static org.gridsuite.network.map.dto.utils.ElementUtils.*;

/**
 * @author AJELLAL Ali <ali.ajellal@rte-france.com>
 */
public final class VscConverterStationInfosMapper {
    private VscConverterStationInfosMapper() {
    }

    public static ElementInfos toData(Identifiable<?> identifiable, InfoTypeParameters infoTypeParameters) {
        switch (infoTypeParameters.getInfoType()) {
            case TAB:
                return toTabInfos(identifiable);
            case FORM:
                return toFormInfos(identifiable);
            default:
                throw new UnsupportedOperationException("TODO");
        }
    }

    private static VscConverterStationTabInfos toTabInfos(Identifiable<?> identifiable) {
        VscConverterStation vscConverterStation = (VscConverterStation) identifiable;
        Terminal terminal = vscConverterStation.getTerminal();
        VscConverterStationTabInfos.VscConverterStationTabInfosBuilder<?, ?> builder = VscConverterStationTabInfos.builder();
        builder
                .name(vscConverterStation.getOptionalName().orElse(null))
                .id(vscConverterStation.getId())
                .voltageLevelId(terminal.getVoltageLevel().getId())
                .nominalV(terminal.getVoltageLevel().getNominalV())
                .country(mapCountry(terminal.getVoltageLevel().getSubstation().orElse(null)))
                .terminalConnected(terminal.isConnected())
                .lossFactor(vscConverterStation.getLossFactor())
                .properties(getProperties(vscConverterStation))
                .voltageRegulatorOn(vscConverterStation.isVoltageRegulatorOn());

        if (!Double.isNaN(terminal.getP())) {
            builder.p(terminal.getP());
        }
        if (!Double.isNaN(vscConverterStation.getVoltageSetpoint())) {
            builder.voltageSetpoint(vscConverterStation.getVoltageSetpoint());
        }
        if (!Double.isNaN(terminal.getQ())) {
            builder.q(terminal.getQ());
        }
        if (vscConverterStation.getHvdcLine() != null) {
            builder.hvdcLineId(vscConverterStation.getHvdcLine().getId());
        }

        if (!Double.isNaN(vscConverterStation.getReactivePowerSetpoint())) {
            builder.reactivePowerSetpoint(vscConverterStation.getReactivePowerSetpoint());
        }

        return builder.build();
    }

    public static VscConverterStationFormInfos toFormInfos(Identifiable<?> identifiable) {
        VscConverterStation vscConverterStation = (VscConverterStation) identifiable;
        Terminal terminal = vscConverterStation.getTerminal();
        VscConverterStationFormInfos.VscConverterStationFormInfosBuilder<?, ?> builder = VscConverterStationFormInfos.builder()
                .name(vscConverterStation.getOptionalName().orElse(null))
                .id(vscConverterStation.getId())
                .voltageLevelId(terminal.getVoltageLevel().getId())
                .terminalConnected(terminal.isConnected())
                .lossFactor(vscConverterStation.getLossFactor())
                .voltageRegulatorOn(vscConverterStation.isVoltageRegulatorOn())
                .voltageSetpoint(nullIfNan(vscConverterStation.getVoltageSetpoint()))
                .reactivePowerSetpoint(nullIfNan(vscConverterStation.getReactivePowerSetpoint()))
                .q(nullIfNan(terminal.getQ()))
                .p(nullIfNan(terminal.getP()))
                .connectablePositionInfos(toMapConnectablePosition(vscConverterStation, 0));

        ReactiveLimits reactiveLimits = vscConverterStation.getReactiveLimits();
        if (reactiveLimits != null) {
            ReactiveLimitsKind reactiveLimitsKind = reactiveLimits.getKind();
            if (reactiveLimitsKind == ReactiveLimitsKind.MIN_MAX) {
                MinMaxReactiveLimits minMaxReactiveLimits = vscConverterStation.getReactiveLimits(MinMaxReactiveLimitsImpl.class);
                builder.minMaxReactiveLimits(MinMaxReactiveLimitsMapData.builder()
                        .maxQ(minMaxReactiveLimits.getMaxQ())
                        .minQ(minMaxReactiveLimits.getMinQ())
                        .build());
            } else if (reactiveLimitsKind == ReactiveLimitsKind.CURVE) {
                ReactiveCapabilityCurve capabilityCurve = vscConverterStation.getReactiveLimits(ReactiveCapabilityCurve.class);
                builder.reactiveCapabilityCurvePoints(getReactiveCapabilityCurvePointsMapData(capabilityCurve.getPoints()));
            }
        }

        return builder.build();
    }

}
