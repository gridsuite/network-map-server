/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.mapper;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.Measurement;
import com.powsybl.network.store.iidm.impl.MinMaxReactiveLimitsImpl;
import org.gridsuite.network.map.dto.ElementInfos;
import org.gridsuite.network.map.dto.InfoTypeParameters;
import org.gridsuite.network.map.dto.definition.battery.BatteryFormInfos;
import org.gridsuite.network.map.dto.definition.battery.BatteryTabInfos;
import org.gridsuite.network.map.dto.common.MinMaxReactiveLimitsMapData;
import org.gridsuite.network.map.dto.common.ReactiveCapabilityCurveMapData;

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

    public static ElementInfos toData(Identifiable<?> identifiable, InfoTypeParameters infoTypeParameters) {
        switch (infoTypeParameters.getInfoType()) {
            case TAB:
                return toTabInfos(identifiable);
            case FORM:
                return toFormInfos(identifiable);
            case LIST:
                return ElementInfosMapper.toInfosWithType(identifiable);
            default:
                throw new UnsupportedOperationException("TODO");
        }
    }

    protected static List<ReactiveCapabilityCurveMapData> getReactiveCapabilityCurvePoints(Collection<ReactiveCapabilityCurve.Point> points) {
        return points.stream()
                .map(point -> ReactiveCapabilityCurveMapData.builder()
                        .p(point.getP())
                        .maxQ(point.getMaxQ())
                        .minQ(point.getMinQ())
                        .build())
                .collect(Collectors.toList());
    }

    private static BatteryFormInfos toFormInfos(Identifiable<?> identifiable) {
        Battery battery = (Battery) identifiable;
        Terminal terminal = battery.getTerminal();
        BatteryFormInfos.BatteryFormInfosBuilder<?, ?> builder = BatteryFormInfos.builder()
                .name(battery.getOptionalName().orElse(null))
                .id(battery.getId())
                .voltageLevelId(terminal.getVoltageLevel().getId())
                .busOrBusbarSectionId(getBusOrBusbarSection(terminal))
                .targetP(battery.getTargetP())
                .targetQ(nullIfNan(battery.getTargetQ()))
                .minP(battery.getMinP())
                .maxP(battery.getMaxP())
                .p(nullIfNan(terminal.getP()))
                .terminalConnected(terminal.isConnected())
                .q(nullIfNan(terminal.getQ()))
                .properties(getProperties(battery))
                .connectablePosition(toMapConnectablePosition(battery, 0));

        ReactiveLimits reactiveLimits = battery.getReactiveLimits();
        if (reactiveLimits != null) {
            ReactiveLimitsKind limitsKind = reactiveLimits.getKind();
            if (limitsKind == ReactiveLimitsKind.MIN_MAX) {
                MinMaxReactiveLimits minMaxReactiveLimits = battery.getReactiveLimits(MinMaxReactiveLimitsImpl.class);
                builder.minMaxReactiveLimits(MinMaxReactiveLimitsMapData.builder().maxQ(minMaxReactiveLimits.getMaxQ()).minQ(minMaxReactiveLimits.getMinQ()).build());
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
            .properties(getProperties(battery))
            .q(nullIfNan(terminal.getQ()));

        builder.connectablePosition(toMapConnectablePosition(battery, 0))
               .activePowerControl(toActivePowerControl(battery));

        // voltageLevel and substation properties
        builder.voltageLevelProperties(getProperties(terminal.getVoltageLevel()));
        builder.substationProperties(getProperties(terminal.getVoltageLevel().getSubstation().orElse(null)));

        ReactiveLimits reactiveLimits = battery.getReactiveLimits();
        if (reactiveLimits != null) {
            ReactiveLimitsKind limitsKind = reactiveLimits.getKind();
            if (limitsKind == ReactiveLimitsKind.MIN_MAX) {
                MinMaxReactiveLimits minMaxReactiveLimits = battery.getReactiveLimits(MinMaxReactiveLimitsImpl.class);
                builder.minMaxReactiveLimits(MinMaxReactiveLimitsMapData.builder().maxQ(minMaxReactiveLimits.getMaxQ()).minQ(minMaxReactiveLimits.getMinQ()).build());
            } else if (limitsKind == ReactiveLimitsKind.CURVE) {
                ReactiveCapabilityCurve capabilityCurve = battery.getReactiveLimits(ReactiveCapabilityCurve.class);
                builder.reactiveCapabilityCurvePoints(getReactiveCapabilityCurvePoints(capabilityCurve.getPoints()));
            }
        }

        builder.measurementP(toMeasurement(battery, Measurement.Type.ACTIVE_POWER, 0))
            .measurementQ(toMeasurement(battery, Measurement.Type.REACTIVE_POWER, 0));

        builder.injectionObservability(toInjectionObservability(battery));

        return builder.build();
    }

}
