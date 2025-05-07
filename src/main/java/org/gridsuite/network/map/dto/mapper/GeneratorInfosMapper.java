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
import org.gridsuite.network.map.dto.common.MinMaxReactiveLimitsMapData;
import org.gridsuite.network.map.dto.common.ReactiveCapabilityCurveMapData;
import org.gridsuite.network.map.dto.definition.generator.GeneratorFormInfos;
import org.gridsuite.network.map.dto.definition.generator.GeneratorTabInfos;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.gridsuite.network.map.dto.utils.ElementUtils.*;

/**
 * @author AJELLAL Ali <ali.ajellal@rte-france.com>
 */

public final class GeneratorInfosMapper {

    private GeneratorInfosMapper() {
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

    private static List<ReactiveCapabilityCurveMapData> getReactiveCapabilityCurvePoints(Collection<ReactiveCapabilityCurve.Point> points) {
        return points.stream()
                .map(point -> ReactiveCapabilityCurveMapData.builder()
                        .p(point.getP())
                        .maxQ(point.getMaxQ())
                        .minQ(point.getMinQ())
                        .build())
                .collect(Collectors.toList());
    }

    private static GeneratorTabInfos toTabInfos(Identifiable<?> identifiable) {
        Generator generator = (Generator) identifiable;
        Terminal terminal = generator.getTerminal();
        GeneratorTabInfos.GeneratorTabInfosBuilder<?, ?> builder = GeneratorTabInfos.builder()
                .name(generator.getOptionalName().orElse(null))
                .id(generator.getId())
                .terminalConnected(terminal.isConnected())
                .voltageLevelId(terminal.getVoltageLevel().getId())
                .nominalVoltage(terminal.getVoltageLevel().getNominalV())
                .country(mapCountry(terminal.getVoltageLevel().getSubstation().orElse(null)))
                .targetP(generator.getTargetP())
                .targetQ(generator.getTargetQ())
                .targetV(generator.getTargetV())
                .minP(generator.getMinP())
                .maxP(generator.getMaxP())
                .ratedS(generator.getRatedS())
                .energySource(generator.getEnergySource())
                .voltageRegulatorOn(generator.isVoltageRegulatorOn())
                .p(terminal.getP())
                .properties(getProperties(generator))
                .q(terminal.getQ());

        builder.activePowerControl(toActivePowerControl(generator))
                .coordinatedReactiveControl(toCoordinatedReactiveControl(generator))
                .generatorShortCircuit(toGeneratorShortCircuit(generator))
                .generatorStartup(toGeneratorStartup(generator));

        Terminal regulatingTerminal = generator.getRegulatingTerminal();
        //If there is no regulating terminal in file, regulating terminal voltage level is equal to generator voltage level
        if (regulatingTerminal != null && !regulatingTerminal.getVoltageLevel().equals(terminal.getVoltageLevel())) {
            builder.regulatingTerminalVlName(regulatingTerminal.getVoltageLevel().getOptionalName().orElse(null));
            builder.regulatingTerminalConnectableId(regulatingTerminal.getConnectable().getId());
            builder.regulatingTerminalConnectableType(regulatingTerminal.getConnectable().getType().name());
            builder.regulatingTerminalVlId(regulatingTerminal.getVoltageLevel().getId());
        }
        ReactiveLimits reactiveLimits = generator.getReactiveLimits();
        if (reactiveLimits != null) {
            ReactiveLimitsKind limitsKind = reactiveLimits.getKind();
            if (limitsKind == ReactiveLimitsKind.MIN_MAX) {
                MinMaxReactiveLimits minMaxReactiveLimits = generator.getReactiveLimits(MinMaxReactiveLimitsImpl.class);
                builder.minMaxReactiveLimits(MinMaxReactiveLimitsMapData.builder()
                        .maxQ(minMaxReactiveLimits.getMaxQ())
                        .minQ(minMaxReactiveLimits.getMinQ())
                        .build());
            } else if (limitsKind == ReactiveLimitsKind.CURVE) {
                ReactiveCapabilityCurve capabilityCurve = generator.getReactiveLimits(ReactiveCapabilityCurve.class);
                builder.reactiveCapabilityCurvePoints(getReactiveCapabilityCurvePoints(capabilityCurve.getPoints()));
            }
        }

        builder.connectablePosition(toMapConnectablePosition(generator, 0));

        builder.measurementP(toMeasurement(generator, Measurement.Type.ACTIVE_POWER, 0))
            .measurementQ(toMeasurement(generator, Measurement.Type.REACTIVE_POWER, 0));

        builder.isCondenser(generator.isCondenser());

        // substation attrubutes
        builder.substationId(terminal.getVoltageLevel().getSubstation().map(Substation::getId).orElse(null));
        builder.substationName(terminal.getVoltageLevel().getSubstation().map(Substation::getOptionalName).orElse(null).orElse(null));
        builder.substationProperties(getProperties(terminal.getVoltageLevel().getSubstation().orElse(null)));

        // voltage level attributes
        builder.voltageLevelName(terminal.getVoltageLevel().getOptionalName().orElse(null));
        builder.voltageLevelProperties(getProperties(terminal.getVoltageLevel()));
        builder.lowVoltageLimit(terminal.getVoltageLevel().getLowVoltageLimit());
        builder.highVoltageLimit(terminal.getVoltageLevel().getHighVoltageLimit());
        builder.voltageLevelShortCircuit(toIdentifiableShortCircuit(terminal.getVoltageLevel()));
        builder.injectionObservability(toInjectionObservability(generator));

        return builder.build();
    }

    private static GeneratorFormInfos toFormInfos(Identifiable<?> identifiable) {
        Generator generator = (Generator) identifiable;
        Terminal terminal = generator.getTerminal();
        GeneratorFormInfos.GeneratorFormInfosBuilder<?, ?> builder = GeneratorFormInfos.builder()
                .name(generator.getOptionalName().orElse(null))
                .id(generator.getId())
                .terminalConnected(terminal.isConnected())
                .voltageLevelId(terminal.getVoltageLevel().getId())
                .targetP(generator.getTargetP())
                .targetQ(generator.getTargetQ())
                .targetV(generator.getTargetV())
                .minP(generator.getMinP())
                .maxP(generator.getMaxP())
                .ratedS(generator.getRatedS())
                .energySource(generator.getEnergySource())
                .voltageRegulatorOn(generator.isVoltageRegulatorOn())
                .p(terminal.getP())
                .q(terminal.getQ())
                .properties(getProperties(generator));
        builder.busOrBusbarSectionId(getBusOrBusbarSection(terminal))
                .activePowerControl(toActivePowerControl(generator));

        builder.generatorShortCircuit(toGeneratorShortCircuit(generator))
                .generatorStartup(toGeneratorStartup(generator))
                .coordinatedReactiveControl(toCoordinatedReactiveControl(generator));

        Terminal regulatingTerminalForm = generator.getRegulatingTerminal();
        //If there is no regulating terminal in file, regulating terminal voltage level is equal to generator voltage level
        if (regulatingTerminalForm != null && !regulatingTerminalForm.getVoltageLevel().equals(terminal.getVoltageLevel())) {
            builder.regulatingTerminalVlId(regulatingTerminalForm.getVoltageLevel().getId());
            builder.regulatingTerminalConnectableType(regulatingTerminalForm.getConnectable().getType().name());
            builder.regulatingTerminalConnectableId(regulatingTerminalForm.getConnectable().getId());
            builder.regulatingTerminalVlName(regulatingTerminalForm.getVoltageLevel().getOptionalName().orElse(null));
        }
        ReactiveLimits reactiveLimits = generator.getReactiveLimits();
        if (reactiveLimits != null) {
            ReactiveLimitsKind limitsKind = reactiveLimits.getKind();
            if (limitsKind == ReactiveLimitsKind.MIN_MAX) {
                MinMaxReactiveLimits minMaxReactiveLimitsForm = generator.getReactiveLimits(MinMaxReactiveLimitsImpl.class);
                builder.minMaxReactiveLimits(MinMaxReactiveLimitsMapData.builder()
                        .maxQ(minMaxReactiveLimitsForm.getMaxQ())
                        .minQ(minMaxReactiveLimitsForm.getMinQ())
                        .build());
            } else if (limitsKind == ReactiveLimitsKind.CURVE) {
                ReactiveCapabilityCurve capabilityCurve = generator.getReactiveLimits(ReactiveCapabilityCurve.class);
                builder.reactiveCapabilityCurvePoints(getReactiveCapabilityCurvePoints(capabilityCurve.getPoints()));
            }
        }

        builder.connectablePosition(toMapConnectablePosition(generator, 0));
        return builder.build();
    }
}
