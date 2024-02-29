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
import org.gridsuite.network.map.dto.definition.battery.BatteryFormInfos;
import org.gridsuite.network.map.dto.definition.battery.BatteryTabInfos;
import org.gridsuite.network.map.model.MinMaxReactiveLimitsMapData;
import org.gridsuite.network.map.model.ReactiveCapabilityCurveMapData;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.gridsuite.network.map.dto.utils.ElementUtils.*;

/**
 * @author AJELLAL Ali <ali.ajellal@rte-france.com>
 */
public final class BatteryInfosMapper {

    private BatteryInfosMapper() {
    }

    public static ElementInfos toData(Identifiable<?> identifiable, ElementInfos.ElementInfoType dataType) {
        switch (dataType.getInfoType()) {
            case TAB:
                return toTabInfos(identifiable);
            case FORM:
                return toFormInfos(identifiable);
            default:
                throw new UnsupportedOperationException("TODO");
        }
    }

    protected static List<ReactiveCapabilityCurveMapData> getReactiveCapabilityCurvePoints(Collection<ReactiveCapabilityCurve.Point> points) {
        return points.stream()
                .map(point -> ReactiveCapabilityCurveMapData.builder()
                        .p(point.getP())
                        .qmaxP(point.getMaxQ())
                        .qminP(point.getMinQ())
                        .build())
                .collect(Collectors.toList());
    }

    private static BatteryFormInfos toFormInfos(Identifiable<?> identifiable) {
        Battery battery = (Battery) identifiable;
        Terminal terminal = battery.getTerminal();
        BatteryFormInfos.BatteryFormInfosBuilder<?, ?> builder = BatteryFormInfos.builder().name(battery.getOptionalName().orElse(null)).id(battery.getId()).voltageLevelId(terminal.getVoltageLevel().getId()).targetP(battery.getTargetP()).targetQ(nullIfNan(battery.getTargetQ())).minP(battery.getMinP()).maxP(battery.getMaxP()).p(nullIfNan(terminal.getP())).q(nullIfNan(terminal.getQ())).properties(getProperties(battery));

        builder.connectablePosition(toMapConnectablePosition(battery, 0));

        ReactiveLimits reactiveLimits = battery.getReactiveLimits();
        if (reactiveLimits != null) {
            ReactiveLimitsKind limitsKind = reactiveLimits.getKind();
            if (limitsKind == ReactiveLimitsKind.MIN_MAX) {
                MinMaxReactiveLimits minMaxReactiveLimits = battery.getReactiveLimits(MinMaxReactiveLimitsImpl.class);
                builder.minMaxReactiveLimits(MinMaxReactiveLimitsMapData.builder().maximumReactivePower(minMaxReactiveLimits.getMaxQ()).minimumReactivePower(minMaxReactiveLimits.getMinQ()).build());
            } else if (limitsKind == ReactiveLimitsKind.CURVE) {
                ReactiveCapabilityCurve capabilityCurve = battery.getReactiveLimits(ReactiveCapabilityCurve.class);
                builder.reactiveCapabilityCurvePoints(getReactiveCapabilityCurvePoints(capabilityCurve.getPoints()));
            }
        }

        builder.activePowerControl(toActivePowerControl(battery));

        return builder.build();
    }

    private static BatteryTabInfos toTabInfos(Identifiable<?> identifiable) {
        Battery battery = (Battery) identifiable;
        Terminal terminal = battery.getTerminal();
        BatteryTabInfos.BatteryTabInfosBuilder<?, ?> builder = BatteryTabInfos.builder()
            .name(battery.getOptionalName().orElse(null))
            .id(battery.getId())
            .terminalConnected(terminal.isConnected())
            .voltageLevelId(terminal.getVoltageLevel().getId())
            .nominalVoltage(terminal.getVoltageLevel().getNominalV())
            .country(mapCountry(terminal.getVoltageLevel().getSubstation().orElse(null)))
            .targetP(battery.getTargetP())
            .targetQ(nullIfNan(battery.getTargetQ()))
            .minP(battery.getMinP())
            .maxP(battery.getMaxP())
            .p(nullIfNan(terminal.getP()))
            .q(nullIfNan(terminal.getQ()));

        builder.connectablePosition(toMapConnectablePosition(battery, 0))
               .activePowerControl(toActivePowerControl(battery));

        ReactiveLimits reactiveLimits = battery.getReactiveLimits();
        if (reactiveLimits != null) {
            ReactiveLimitsKind limitsKind = reactiveLimits.getKind();
            if (limitsKind == ReactiveLimitsKind.MIN_MAX) {
                MinMaxReactiveLimits minMaxReactiveLimits = battery.getReactiveLimits(MinMaxReactiveLimitsImpl.class);
                builder.minMaxReactiveLimits(MinMaxReactiveLimitsMapData.builder().maximumReactivePower(minMaxReactiveLimits.getMaxQ()).minimumReactivePower(minMaxReactiveLimits.getMinQ()).build());
            } else if (limitsKind == ReactiveLimitsKind.CURVE) {
                ReactiveCapabilityCurve capabilityCurve = battery.getReactiveLimits(ReactiveCapabilityCurve.class);
                builder.reactiveCapabilityCurvePoints(getReactiveCapabilityCurvePoints(capabilityCurve.getPoints()));
            }
        }

        return builder.build();
    }

}
