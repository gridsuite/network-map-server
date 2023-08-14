/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.mapper.generator;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.*;
import com.powsybl.network.store.iidm.impl.MinMaxReactiveLimitsImpl;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.gridsuite.network.map.dto.ElementInfos;
import org.gridsuite.network.map.dto.definition.generator.GeneratorFormInfos;
import org.gridsuite.network.map.dto.definition.generator.GeneratorTabInfos;
import org.gridsuite.network.map.model.MinMaxReactiveLimitsMapData;
import org.gridsuite.network.map.model.ReactiveCapabilityCurveMapData;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.gridsuite.network.map.dto.utils.ElementUtils.getBusOrBusbarSection;
import static org.gridsuite.network.map.dto.utils.ElementUtils.nullIfNan;

/**
 * @author AJELLAL Ali <ali.ajellal@rte-france.com>
 */

@SuperBuilder
@Getter
public abstract class AbstractGeneratorInfos extends ElementInfos {
    public static ElementInfos toData(Identifiable<?> identifiable, InfoType dataType) {
        switch (dataType) {
            case TAB:
                return toGeneratorTabInfos(identifiable);
            case FORM:
                return toGeneratorFormInfos(identifiable);
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

    public static GeneratorTabInfos toGeneratorTabInfos(Identifiable<?> identifiable) {
        Generator generator = (Generator) identifiable;
        Terminal terminal = generator.getTerminal();
        GeneratorTabInfos.GeneratorTabInfosBuilder builder = GeneratorTabInfos.builder()
                .name(generator.getOptionalName().orElse(null))
                .id(generator.getId())
                .terminalConnected(terminal.isConnected())
                .voltageLevelId(terminal.getVoltageLevel().getId())
                .nominalVoltage(terminal.getVoltageLevel().getNominalV())
                .targetP(generator.getTargetP())
                .targetQ(nullIfNan(generator.getTargetQ()))
                .targetV(nullIfNan(generator.getTargetV()))
                .minP(generator.getMinP())
                .maxP(generator.getMaxP())
                .ratedS(nullIfNan(generator.getRatedS()))
                .energySource(generator.getEnergySource())
                .voltageRegulatorOn(generator.isVoltageRegulatorOn())
                .p(nullIfNan(terminal.getP()))
                .q(nullIfNan(terminal.getQ()));

        ActivePowerControl<Generator> activePowerControl = generator.getExtension(ActivePowerControl.class);
        if (activePowerControl != null) {
            builder.activePowerControlOn(activePowerControl.isParticipate());
            builder.droop(activePowerControl.getDroop());
        }

        GeneratorShortCircuit generatorShortCircuit = generator.getExtension(GeneratorShortCircuit.class);
        if (generatorShortCircuit != null) {
            builder.transientReactance(generatorShortCircuit.getDirectTransX());
            builder.stepUpTransformerReactance(generatorShortCircuit.getStepUpTransformerX());
        }

        GeneratorStartup generatorStartup = generator.getExtension(GeneratorStartup.class);
        if (generatorStartup != null) {
            builder.plannedActivePowerSetPoint(nullIfNan(generatorStartup.getPlannedActivePowerSetpoint()));
            builder.marginalCost(nullIfNan(generatorStartup.getMarginalCost()));
            builder.plannedOutageRate(nullIfNan(generatorStartup.getPlannedOutageRate()));
            builder.forcedOutageRate(nullIfNan(generatorStartup.getForcedOutageRate()));
        }

        CoordinatedReactiveControl coordinatedReactiveControl = generator.getExtension(CoordinatedReactiveControl.class);
        if (coordinatedReactiveControl != null) {
            builder.qPercent(coordinatedReactiveControl.getQPercent());
        }

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
                        .maximumReactivePower(minMaxReactiveLimits.getMaxQ())
                        .minimumReactivePower(minMaxReactiveLimits.getMinQ())
                        .build());
            } else if (limitsKind == ReactiveLimitsKind.CURVE) {
                ReactiveCapabilityCurve capabilityCurve = generator.getReactiveLimits(ReactiveCapabilityCurve.class);
                builder.reactiveCapabilityCurvePoints(getReactiveCapabilityCurvePoints(capabilityCurve.getPoints()));
            }
        }

        var connectablePosition = generator.getExtension(ConnectablePosition.class);
        if (connectablePosition != null) {
            builder
                    .connectionDirection(connectablePosition.getFeeder().getDirection())
                    .connectionName(connectablePosition.getFeeder().getName().orElse(null));
            connectablePosition.getFeeder().getOrder().ifPresent(builder::connectionPosition);
        }

        return builder.build();
    }

    public static GeneratorFormInfos toGeneratorFormInfos(Identifiable<?> identifiable) {
        Generator generator = (Generator) identifiable;
        Terminal terminal = generator.getTerminal();
        GeneratorFormInfos.GeneratorFormInfosBuilder builder = GeneratorFormInfos.builder()
                .name(generator.getOptionalName().orElse(null))
                .id(generator.getId())
                .terminalConnected(terminal.isConnected())
                .voltageLevelId(terminal.getVoltageLevel().getId())
                .targetP(generator.getTargetP())
                .targetQ(nullIfNan(generator.getTargetQ()))
                .targetV(nullIfNan(generator.getTargetV()))
                .minP(generator.getMinP())
                .maxP(generator.getMaxP())
                .ratedS(nullIfNan(generator.getRatedS()))
                .energySource(generator.getEnergySource())
                .voltageRegulatorOn(generator.isVoltageRegulatorOn())
                .p(nullIfNan(terminal.getP()))
                .q(nullIfNan(terminal.getQ()));
        builder.busOrBusbarSectionId(getBusOrBusbarSection(terminal));

        ActivePowerControl<Generator> activePowerControl = generator.getExtension(ActivePowerControl.class);
        if (activePowerControl != null) {
            builder.activePowerControlOn(activePowerControl.isParticipate());
            builder.droop(activePowerControl.getDroop());
        }

        GeneratorShortCircuit generatorShortCircuit = generator.getExtension(GeneratorShortCircuit.class);
        if (generatorShortCircuit != null) {
            builder.transientReactance(generatorShortCircuit.getDirectTransX());
            builder.stepUpTransformerReactance(generatorShortCircuit.getStepUpTransformerX());
        }

        GeneratorStartup generatorStartup = generator.getExtension(GeneratorStartup.class);
        if (generatorStartup != null) {
            builder.plannedActivePowerSetPoint(nullIfNan(generatorStartup.getPlannedActivePowerSetpoint()));
            builder.marginalCost(nullIfNan(generatorStartup.getMarginalCost()));
            builder.plannedOutageRate(nullIfNan(generatorStartup.getPlannedOutageRate()));
            builder.forcedOutageRate(nullIfNan(generatorStartup.getForcedOutageRate()));
        }

        CoordinatedReactiveControl coordinatedReactiveControl = generator.getExtension(CoordinatedReactiveControl.class);
        if (coordinatedReactiveControl != null) {
            builder.qPercent(coordinatedReactiveControl.getQPercent());
        } else {
            builder.qPercent(Double.NaN);
        }

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
                        .maximumReactivePower(minMaxReactiveLimits.getMaxQ())
                        .minimumReactivePower(minMaxReactiveLimits.getMinQ())
                        .build());
            } else if (limitsKind == ReactiveLimitsKind.CURVE) {
                ReactiveCapabilityCurve capabilityCurve = generator.getReactiveLimits(ReactiveCapabilityCurve.class);
                builder.reactiveCapabilityCurvePoints(getReactiveCapabilityCurvePoints(capabilityCurve.getPoints()));
            }
        }

        var connectablePosition = generator.getExtension(ConnectablePosition.class);
        if (connectablePosition != null) {
            builder
                    .connectionDirection(connectablePosition.getFeeder().getDirection())
                    .connectionName(connectablePosition.getFeeder().getName().orElse(null));
            connectablePosition.getFeeder().getOrder().ifPresent(builder::connectionPosition);
        }

        return builder.build();
    }
}
