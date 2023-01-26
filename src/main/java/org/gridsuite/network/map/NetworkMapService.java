/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.*;
import com.powsybl.network.store.client.NetworkStoreService;
import com.powsybl.network.store.client.PreloadingStrategy;
import com.powsybl.network.store.iidm.impl.MinMaxReactiveLimitsImpl;
import org.gridsuite.network.map.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@ComponentScan(basePackageClasses = {NetworkStoreService.class})
@Service
class NetworkMapService {

    @Autowired
    private NetworkStoreService networkStoreService;

    private Network getNetwork(UUID networkUuid, PreloadingStrategy strategy, String variantId) {
        try {
            Network network = networkStoreService.getNetwork(networkUuid, strategy);
            if (variantId != null) {
                network.getVariantManager().setWorkingVariant(variantId);
            }
            return network;
        } catch (PowsyblException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    private static VoltageLevelMapData toMapData(VoltageLevel voltageLevel) {
        var builder = VoltageLevelMapData.builder()
            .name(voltageLevel.getOptionalName().orElse(null))
            .id(voltageLevel.getId())
            .substationId(voltageLevel.getSubstation().map(Substation::getId).orElse(null))
            .nominalVoltage(voltageLevel.getNominalV())
            .topologyKind(voltageLevel.getTopologyKind());
        if (voltageLevel.getTopologyKind().equals(TopologyKind.NODE_BREAKER)) {
            builder.busbarSections(voltageLevel.getNodeBreakerView().getBusbarSectionStream().map(NetworkMapService::toMapData).collect(Collectors.toList()));
        }
        return builder.build();
    }

    private static VoltageLevelConnectableMapData toMapData(Connectable<?> connectable) {
        return VoltageLevelConnectableMapData.builder()
                .id(connectable.getId())
                .name(connectable.getOptionalName().orElse(null))
                .type(connectable.getType())
                .build();
    }

    private static SubstationMapData toMapData(Substation substation) {
        Map<String, String> properties = substation.getPropertyNames().stream()
            .map(name -> Map.entry(name, substation.getProperty(name)))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return SubstationMapData.builder()
            .name(substation.getOptionalName().orElse(null))
            .id(substation.getId())
            .countryName(substation.getCountry().map(Country::getName).orElse(null))
            .properties(properties.isEmpty() ? null : properties)
            .voltageLevels(substation.getVoltageLevelStream().map(NetworkMapService::toMapData).collect(Collectors.toList()))
            .build();
    }

    private static LineMapData toMapData(Line line) {
        Terminal terminal1 = line.getTerminal1();
        Terminal terminal2 = line.getTerminal2();
        LineMapData.LineMapDataBuilder builder = LineMapData.builder()
            .name(line.getOptionalName().orElse(null))
            .id(line.getId())
            .terminal1Connected(terminal1.isConnected())
            .terminal2Connected(terminal2.isConnected())
            .voltageLevelId1(terminal1.getVoltageLevel().getId())
            .voltageLevelName1(terminal1.getVoltageLevel().getOptionalName().orElse(null))
            .voltageLevelId2(terminal2.getVoltageLevel().getId())
            .voltageLevelName2(terminal2.getVoltageLevel().getOptionalName().orElse(null))
            .p1(nullIfNan(terminal1.getP()))
            .q1(nullIfNan(terminal1.getQ()))
            .p2(nullIfNan(terminal2.getP()))
            .q2(nullIfNan(terminal2.getQ()))
            .i1(nullIfNan(terminal1.getI()))
            .i2(nullIfNan(terminal2.getI()))
            .r(line.getR())
            .x(line.getX())
            .g1(line.getG1())
            .b1(line.getB1())
            .g2(line.getG2())
            .b2(line.getB2());
        CurrentLimits limits1 = line.getCurrentLimits1().orElse(null);
        CurrentLimits limits2 = line.getCurrentLimits2().orElse(null);

        if (limits1 != null && !Double.isNaN(limits1.getPermanentLimit())) {
            builder.permanentLimit1(limits1.getPermanentLimit());
        }
        if (limits2 != null && !Double.isNaN(limits2.getPermanentLimit())) {
            builder.permanentLimit2(limits2.getPermanentLimit());
        }
        BranchStatus branchStatus = line.getExtension(BranchStatus.class);
        if (branchStatus != null) {
            builder.branchStatus(branchStatus.getStatus().name());
        }
        var connectablePosition = line.getExtension(ConnectablePosition.class);
        if (connectablePosition != null) {
            if (connectablePosition.getFeeder1() != null) {
                builder
                        .connectionDirection1(connectablePosition.getFeeder1().getDirection())
                        .connectionName1(connectablePosition.getFeeder1().getName())
                        .connectionPosition1(connectablePosition.getFeeder1().getOrder().orElse(null));
            }

            if (connectablePosition.getFeeder2() != null) {
                builder
                        .connectionDirection2(connectablePosition.getFeeder2().getDirection())
                        .connectionName2(connectablePosition.getFeeder2().getName())
                        .connectionPosition2(connectablePosition.getFeeder2().getOrder().orElse(null));
            }
        }
        return builder.build();
    }

    //Method which enables us to generate a light version of the LineMapData object in order to optimize transfers
    //the light version is designed to only have the necessary fields for the network map to function
    private static LineMapData toBasicMapData(Line line) {
        Terminal terminal1 = line.getTerminal1();
        Terminal terminal2 = line.getTerminal2();
        LineMapData.LineMapDataBuilder builder = LineMapData.builder()
                .id(line.getId())
                .name(line.getOptionalName().orElse(null))
                .terminal1Connected(terminal1.isConnected())
                .terminal2Connected(terminal2.isConnected())
                .voltageLevelId1(terminal1.getVoltageLevel().getId())
                .voltageLevelId2(terminal2.getVoltageLevel().getId())
                .i1(nullIfNan(terminal1.getI()))
                .i2(nullIfNan(terminal2.getI()))
                .p1(nullIfNan(terminal1.getP()))
                .p2(nullIfNan(terminal2.getP()));

        CurrentLimits limits1 = line.getCurrentLimits1().orElse(null);
        CurrentLimits limits2 = line.getCurrentLimits2().orElse(null);

        if (limits1 != null && !Double.isNaN(limits1.getPermanentLimit())) {
            builder.permanentLimit1(limits1.getPermanentLimit());
        }
        if (limits2 != null && !Double.isNaN(limits2.getPermanentLimit())) {
            builder.permanentLimit2(limits2.getPermanentLimit());
        }
        return builder.build();
    }

    //Method which enables us to generate a light version of the SubstationMapData object in order to optimize transfers
    //the light version is designed to only have the necessary fields for the network map to function
    private static SubstationMapData toBasicMapData(Substation substation) {
        return SubstationMapData.builder()
                .id(substation.getId())
                .name(substation.getOptionalName().orElse(null))
                .voltageLevels(substation.getVoltageLevelStream().map(NetworkMapService::toBasicMapData).collect(Collectors.toList()))
                .build();
    }

    //Method which enables us to generate a light version of the VoltageLevelMapData object in order to optimize transfers
    //the light version is designed to only have the necessary fields for the network map to function
    private static VoltageLevelMapData toBasicMapData(VoltageLevel voltageLevel) {
        return VoltageLevelMapData.builder()
                .id(voltageLevel.getId())
                .substationId(voltageLevel.getSubstation().orElseThrow().getId())
                .nominalVoltage(voltageLevel.getNominalV())
                .build();
    }

    private static GeneratorMapData toMapData(Generator generator) {
        Terminal terminal = generator.getTerminal();

        GeneratorMapData.GeneratorMapDataBuilder builder = GeneratorMapData.builder()
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
            builder.startupCost(nullIfNan(generatorStartup.getStartupCost()));
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
                builder.reactiveCapabilityCurvePoints(toMapData(capabilityCurve.getPoints()));
            }
        }

        var connectablePosition = generator.getExtension(ConnectablePosition.class);
        if (connectablePosition != null) {
            builder
                    .connectionDirection(connectablePosition.getFeeder().getDirection())
                    .connectionName(connectablePosition.getFeeder().getName());
            connectablePosition.getFeeder().getOrder().ifPresent(builder::connectionPosition);
        }

        return builder.build();
    }

    private static List<ReactiveCapabilityCurveMapData> toMapData(Collection<ReactiveCapabilityCurve.Point> points) {
        return  points.stream()
                .map(point -> ReactiveCapabilityCurveMapData.builder()
                        .p(point.getP())
                        .qmaxP(point.getMaxQ())
                        .qminP(point.getMinQ())
                        .build())
                .collect(Collectors.toList());
    }

    private static Double nullIfNan(double d) {
        return Double.isNaN(d) ? null : d;
    }

    private static TwoWindingsTransformerMapData toMapData(TwoWindingsTransformer transformer) {
        Terminal terminal1 = transformer.getTerminal1();
        Terminal terminal2 = transformer.getTerminal2();

        TwoWindingsTransformerMapData.TwoWindingsTransformerMapDataBuilder builder = TwoWindingsTransformerMapData.builder()
                .name(transformer.getOptionalName().orElse(null))
                .id(transformer.getId())
                .terminal1Connected(terminal1.isConnected())
                .terminal2Connected(terminal2.isConnected())
                .voltageLevelId1(terminal1.getVoltageLevel().getId())
                .voltageLevelName1(terminal1.getVoltageLevel().getOptionalName().orElse(null))
                .voltageLevelId2(terminal2.getVoltageLevel().getId())
                .voltageLevelName2(terminal2.getVoltageLevel().getOptionalName().orElse(null))
                .phaseTapChanger(toMapData(transformer.getPhaseTapChanger()))
                .ratioTapChanger(toMapData(transformer.getRatioTapChanger()))
                .r(transformer.getR())
                .x(transformer.getX())
                .b(transformer.getB())
                .g(transformer.getG())
                .ratedU1(transformer.getRatedU1())
                .ratedU2(transformer.getRatedU2());

        builder.ratedS(nullIfNan(transformer.getRatedS()));
        builder.p1(nullIfNan(terminal1.getP()));
        builder.q1(nullIfNan(terminal1.getQ()));
        builder.p2(nullIfNan(terminal2.getP()));
        builder.q2(nullIfNan(terminal2.getQ()));
        builder.i1(nullIfNan(terminal1.getI()));
        builder.i2(nullIfNan(terminal2.getI()));

        CurrentLimits limits1 = transformer.getCurrentLimits1().orElse(null);
        CurrentLimits limits2 = transformer.getCurrentLimits2().orElse(null);
        if (limits1 != null && !Double.isNaN(limits1.getPermanentLimit())) {
            builder.permanentLimit1(limits1.getPermanentLimit());
        }
        if (limits2 != null && !Double.isNaN(limits2.getPermanentLimit())) {
            builder.permanentLimit2(limits2.getPermanentLimit());
        }
        BranchStatus branchStatus = transformer.getExtension(BranchStatus.class);
        if (branchStatus != null) {
            builder.branchStatus(branchStatus.getStatus().name());
        }
        var connectablePosition = transformer.getExtension(ConnectablePosition.class);
        if (connectablePosition != null) {
            if (connectablePosition.getFeeder1() != null) {
                builder
                        .connectionDirection1(connectablePosition.getFeeder1().getDirection())
                        .connectionName1(connectablePosition.getFeeder1().getName())
                        .connectionPosition1(connectablePosition.getFeeder1().getOrder().orElse(null));
            }

            if (connectablePosition.getFeeder2() != null) {
                builder
                        .connectionDirection2(connectablePosition.getFeeder2().getDirection())
                        .connectionName2(connectablePosition.getFeeder2().getName())
                        .connectionPosition2(connectablePosition.getFeeder2().getOrder().orElse(null));
            }
        }
        return builder.build();
    }

    private static TapChangerData toMapData(RatioTapChanger tapChanger) {
        if (tapChanger == null) {
            return null;
        }

        TapChangerData.TapChangerDataBuilder builder = TapChangerData.builder()
                .lowTapPosition(tapChanger.getLowTapPosition())
                .highTapPosition(tapChanger.getHighTapPosition())
                .tapPosition(tapChanger.getTapPosition())
                .regulating(tapChanger.isRegulating())
                .loadTapChangingCapabilities(tapChanger.hasLoadTapChangingCapabilities())
                .regulatingTerminalConnectableId(tapChanger.getRegulationTerminal() != null ? tapChanger.getRegulationTerminal().getConnectable().getId() : null)
                .regulatingTerminalConnectableType(tapChanger.getRegulationTerminal() != null ? tapChanger.getRegulationTerminal().getConnectable().getType().name() : null)
                .regulatingTerminalVlId(tapChanger.getRegulationTerminal() != null ? tapChanger.getRegulationTerminal().getVoltageLevel().getId() : null)
                .steps(toMapDataRatioStep(tapChanger.getAllSteps()));

        builder.targetV(nullIfNan(tapChanger.getTargetV()));
        builder.targetDeadBand(nullIfNan(tapChanger.getTargetDeadband()));
        return builder.build();
    }

    private static TapChangerData toMapData(PhaseTapChanger tapChanger) {
        if (tapChanger == null) {
            return null;
        }

        TapChangerData.TapChangerDataBuilder builder = TapChangerData.builder()
                .lowTapPosition(tapChanger.getLowTapPosition())
                .highTapPosition(tapChanger.getHighTapPosition())
                .tapPosition(tapChanger.getTapPosition())
                .regulating(tapChanger.isRegulating())
                .regulationMode(tapChanger.getRegulationMode())
                .regulationValue(tapChanger.getRegulationValue())
                .targetDeadBand(tapChanger.getTargetDeadband())
                .regulatingTerminalConnectableId(tapChanger.getRegulationTerminal() != null ? tapChanger.getRegulationTerminal().getConnectable().getId() : null)
                .regulatingTerminalConnectableType(tapChanger.getRegulationTerminal() != null ? tapChanger.getRegulationTerminal().getConnectable().getType().name() : null)
                .regulatingTerminalVlId(tapChanger.getRegulationTerminal() != null ? tapChanger.getRegulationTerminal().getVoltageLevel().getId() : null)
                .steps(toMapDataPhaseStep(tapChanger.getAllSteps()));

        builder.targetDeadBand(nullIfNan(tapChanger.getTargetDeadband()));
        return builder.build();
    }

    private static List<TapChangerStepData> toMapDataPhaseStep(Map<Integer, PhaseTapChangerStep> tapChangerStep) {
        if (tapChangerStep == null) {
            return List.of();
        }
        return tapChangerStep.entrySet().stream().map(p -> {
            Integer index = p.getKey();
            PhaseTapChangerStep v = p.getValue();

            return TapChangerStepData.builder().index(index)
                    .g(v.getG())
                    .b(v.getB())
                    .r(v.getR())
                    .x(v.getX())
                    .rho(v.getRho())
                    .alpha(v.getAlpha())
                    .build();
        }).collect(Collectors.toList());
    }

    private static List<TapChangerStepData> toMapDataRatioStep(Map<Integer, RatioTapChangerStep> tapChangerStep) {
        if (tapChangerStep == null) {
            return List.of();
        }
        return tapChangerStep.entrySet().stream().map(p -> {
            Integer index = p.getKey();
            RatioTapChangerStep v = p.getValue();

            return TapChangerStepData.builder().index(index)
                    .g(v.getG())
                    .b(v.getB())
                    .r(v.getR())
                    .x(v.getX())
                    .rho(v.getRho())
                    .build();
        }).collect(Collectors.toList());
    }

    private static ThreeWindingsTransformerMapData toMapData(ThreeWindingsTransformer transformer) {
        ThreeWindingsTransformer.Leg leg1 = transformer.getLeg1();
        ThreeWindingsTransformer.Leg leg2 = transformer.getLeg2();
        ThreeWindingsTransformer.Leg leg3 = transformer.getLeg3();
        Terminal terminal1 = leg1.getTerminal();
        Terminal terminal2 = leg2.getTerminal();
        Terminal terminal3 = leg3.getTerminal();
        ThreeWindingsTransformerMapData.ThreeWindingsTransformerMapDataBuilder builder = ThreeWindingsTransformerMapData.builder()
            .name(transformer.getOptionalName().orElse(null))
            .id(transformer.getId())
            .terminal1Connected(terminal1.isConnected())
            .terminal2Connected(terminal2.isConnected())
            .terminal3Connected(terminal3.isConnected())
            .voltageLevelId1(terminal1.getVoltageLevel().getId())
            .voltageLevelId2(terminal2.getVoltageLevel().getId())
            .voltageLevelId3(terminal3.getVoltageLevel().getId());
        if (!Double.isNaN(terminal1.getP())) {
            builder.p1(terminal1.getP());
        }
        if (!Double.isNaN(terminal1.getQ())) {
            builder.q1(terminal1.getQ());
        }
        if (!Double.isNaN(terminal2.getP())) {
            builder.p2(terminal2.getP());
        }
        if (!Double.isNaN(terminal2.getQ())) {
            builder.q2(terminal2.getQ());
        }
        if (!Double.isNaN(terminal3.getP())) {
            builder.p3(terminal3.getP());
        }
        if (!Double.isNaN(terminal3.getQ())) {
            builder.q3(terminal3.getQ());
        }
        if (!Double.isNaN(terminal1.getI())) {
            builder.i1(terminal1.getI());
        }
        if (!Double.isNaN(terminal2.getI())) {
            builder.i2(terminal2.getI());
        }
        if (!Double.isNaN(terminal3.getI())) {
            builder.i3(terminal3.getI());
        }
        CurrentLimits limits1 = leg1.getCurrentLimits().orElse(null);
        CurrentLimits limits2 = leg2.getCurrentLimits().orElse(null);
        CurrentLimits limits3 = leg3.getCurrentLimits().orElse(null);
        if (limits1 != null && !Double.isNaN(limits1.getPermanentLimit())) {
            builder.permanentLimit1(limits1.getPermanentLimit());
        }
        if (limits2 != null && !Double.isNaN(limits2.getPermanentLimit())) {
            builder.permanentLimit2(limits2.getPermanentLimit());
        }
        if (limits3 != null && !Double.isNaN(limits3.getPermanentLimit())) {
            builder.permanentLimit3(limits3.getPermanentLimit());
        }
        if (leg1.hasRatioTapChanger()) {
            builder.ratioTapChanger1(toMapData(leg1.getRatioTapChanger()))
                    .loadTapChanging1Capabilities(leg1.getRatioTapChanger().hasLoadTapChangingCapabilities())
                    .regulatingRatio1(leg1.getRatioTapChanger().isRegulating());
            if (!Double.isNaN(leg1.getRatioTapChanger().getTargetV())) {
                builder.targetV1(leg1.getRatioTapChanger().getTargetV());
            }
        }
        if (leg2.hasRatioTapChanger()) {
            builder.ratioTapChanger2(toMapData(leg2.getRatioTapChanger()))
                    .loadTapChanging2Capabilities(leg2.getRatioTapChanger().hasLoadTapChangingCapabilities())
                    .regulatingRatio2(leg2.getRatioTapChanger().isRegulating());
            if (!Double.isNaN(leg2.getRatioTapChanger().getTargetV())) {
                builder.targetV2(leg2.getRatioTapChanger().getTargetV());
            }
        }
        if (leg3.hasRatioTapChanger()) {
            builder.ratioTapChanger3(toMapData(leg3.getRatioTapChanger()))
                    .loadTapChanging3Capabilities(leg3.getRatioTapChanger().hasLoadTapChangingCapabilities())
                    .regulatingRatio3(leg3.getRatioTapChanger().isRegulating());
            if (!Double.isNaN(leg3.getRatioTapChanger().getTargetV())) {
                builder.targetV3(leg3.getRatioTapChanger().getTargetV());
            }
        }
        if (leg1.hasPhaseTapChanger()) {
            builder.phaseTapChanger1(toMapData(leg1.getPhaseTapChanger()))
                    .regulatingMode1(leg1.getPhaseTapChanger().getRegulationMode().name())
                    .regulatingPhase1(leg1.getPhaseTapChanger().isRegulating());
            if (!Double.isNaN(leg1.getPhaseTapChanger().getRegulationValue())) {
                builder.regulatingValue1(leg1.getPhaseTapChanger().getRegulationValue());
            }
        }
        if (leg2.hasPhaseTapChanger()) {
            builder.phaseTapChanger2(toMapData(leg2.getPhaseTapChanger()))
                    .regulatingMode2(leg2.getPhaseTapChanger().getRegulationMode().name())
                    .regulatingPhase2(leg2.getPhaseTapChanger().isRegulating());
            if (!Double.isNaN(leg2.getPhaseTapChanger().getRegulationValue())) {
                builder.regulatingValue2(leg2.getPhaseTapChanger().getRegulationValue());
            }
        }
        if (leg3.hasPhaseTapChanger()) {
            builder.phaseTapChanger3(toMapData(leg3.getPhaseTapChanger()))
                    .regulatingMode3(leg3.getPhaseTapChanger().getRegulationMode().name())
                    .regulatingPhase3(leg3.getPhaseTapChanger().isRegulating());
            if (!Double.isNaN(leg3.getPhaseTapChanger().getRegulationValue())) {
                builder.regulatingValue3(leg3.getPhaseTapChanger().getRegulationValue());
            }
        }
        return builder.build();
    }

    private static BatteryMapData toMapData(Battery battery) {
        Terminal terminal = battery.getTerminal();
        BatteryMapData.BatteryMapDataBuilder builder = BatteryMapData.builder()
            .name(battery.getOptionalName().orElse(null))
            .id(battery.getId())
            .terminalConnected(terminal.isConnected())
            .voltageLevelId(terminal.getVoltageLevel().getId())
            .targetP(battery.getTargetP())
            .targetQ(battery.getTargetQ());
        if (!Double.isNaN(terminal.getP())) {
            builder.p(terminal.getP());
        }
        if (!Double.isNaN(terminal.getQ())) {
            builder.q(terminal.getQ());
        }
        return builder.build();
    }

    private static DanglingLineMapData toMapData(DanglingLine danglingLine) {
        Terminal terminal = danglingLine.getTerminal();
        DanglingLineMapData.DanglingLineMapDataBuilder builder = DanglingLineMapData.builder()
            .name(danglingLine.getOptionalName().orElse(null))
            .id(danglingLine.getId())
            .terminalConnected(terminal.isConnected())
            .voltageLevelId(terminal.getVoltageLevel().getId())
            .ucteXnodeCode(danglingLine.getUcteXnodeCode())
            .p0(danglingLine.getP0())
            .q0(danglingLine.getQ0());
        if (!Double.isNaN(terminal.getP())) {
            builder.p(terminal.getP());
        }
        if (!Double.isNaN(terminal.getQ())) {
            builder.q(terminal.getQ());
        }
        return builder.build();
    }

    private static HvdcLineMapData toMapData(HvdcLine hvdcLine) {
        HvdcAngleDroopActivePowerControl hvdcAngleDroopActivePowerControl = hvdcLine.getExtension(HvdcAngleDroopActivePowerControl.class);
        HvdcOperatorActivePowerRange hvdcOperatorActivePowerRange = hvdcLine.getExtension(HvdcOperatorActivePowerRange.class);
        HvdcLineMapData.HvdcLineMapDataBuilder builder = HvdcLineMapData.builder()
            .name(hvdcLine.getOptionalName().orElse(null))
            .id(hvdcLine.getId())
            .convertersMode(hvdcLine.getConvertersMode())
            .converterStationId1(hvdcLine.getConverterStation1().getId())
            .converterStationId2(hvdcLine.getConverterStation2().getId())
            .maxP(hvdcLine.getMaxP())
            .r(hvdcLine.getR())
            .activePowerSetpoint(hvdcLine.getActivePowerSetpoint());

        if (hvdcAngleDroopActivePowerControl != null) {
            builder.k(hvdcAngleDroopActivePowerControl.getDroop())
                   .isEnabled(hvdcAngleDroopActivePowerControl.isEnabled())
                   .p0(hvdcAngleDroopActivePowerControl.getP0());
        }

        if (hvdcOperatorActivePowerRange != null) {
            builder.oprFromCS1toCS2(hvdcOperatorActivePowerRange.getOprFromCS1toCS2())
                   .oprFromCS2toCS1(hvdcOperatorActivePowerRange.getOprFromCS2toCS1());
        }
        return builder.build();
    }

    private static LccConverterStationMapData toMapData(LccConverterStation lccConverterStation) {
        Terminal terminal = lccConverterStation.getTerminal();
        LccConverterStationMapData.LccConverterStationMapDataBuilder builder = LccConverterStationMapData.builder()
            .name(lccConverterStation.getOptionalName().orElse(null))
            .id(lccConverterStation.getId())
            .voltageLevelId(terminal.getVoltageLevel().getId())
            .terminalConnected(terminal.isConnected())
            .lossFactor(lccConverterStation.getLossFactor())
            .powerFactor(lccConverterStation.getPowerFactor());
        if (lccConverterStation.getHvdcLine() != null) {
            builder.hvdcLineId(lccConverterStation.getHvdcLine().getId());
        }
        if (!Double.isNaN(terminal.getP())) {
            builder.p(terminal.getP());
        }
        if (!Double.isNaN(terminal.getQ())) {
            builder.q(terminal.getQ());
        }
        return builder.build();
    }

    private static VscConverterStationMapData toMapData(VscConverterStation vscConverterStation) {
        Terminal terminal = vscConverterStation.getTerminal();
        VscConverterStationMapData.VscConverterStationMapDataBuilder builder = VscConverterStationMapData.builder()
            .name(vscConverterStation.getOptionalName().orElse(null))
            .id(vscConverterStation.getId())
            .voltageLevelId(terminal.getVoltageLevel().getId())
            .terminalConnected(terminal.isConnected())
            .lossFactor(vscConverterStation.getLossFactor())
            .voltageRegulatorOn(vscConverterStation.isVoltageRegulatorOn());
        if (vscConverterStation.getHvdcLine() != null) {
            builder.hvdcLineId(vscConverterStation.getHvdcLine().getId());
        }
        if (!Double.isNaN(terminal.getP())) {
            builder.p(terminal.getP());
        }
        if (!Double.isNaN(terminal.getQ())) {
            builder.q(terminal.getQ());
        }
        if (!Double.isNaN(vscConverterStation.getVoltageSetpoint())) {
            builder.voltageSetpoint(vscConverterStation.getVoltageSetpoint());
        }
        if (!Double.isNaN(vscConverterStation.getReactivePowerSetpoint())) {
            builder.reactivePowerSetpoint(vscConverterStation.getReactivePowerSetpoint());
        }
        return builder.build();
    }

    private static LoadMapData toMapData(Load load) {
        Terminal terminal = load.getTerminal();
        LoadMapData.LoadMapDataBuilder builder = LoadMapData.builder()
            .name(load.getOptionalName().orElse(null))
            .id(load.getId())
            .type(load.getLoadType())
            .terminalConnected(terminal.isConnected())
            .voltageLevelId(terminal.getVoltageLevel().getId())
            .p0(load.getP0())
            .q0(load.getQ0());
        if (!Double.isNaN(terminal.getP())) {
            builder.p(terminal.getP());
        }
        if (!Double.isNaN(terminal.getQ())) {
            builder.q(terminal.getQ());
        }

        var connectablePosition = load.getExtension(ConnectablePosition.class);
        if (connectablePosition != null) {
            builder
                    .connectionDirection(connectablePosition.getFeeder().getDirection())
                    .connectionName(connectablePosition.getFeeder().getName());
            connectablePosition.getFeeder().getOrder().ifPresent(builder::connectionPosition);
        }

        return builder.build();
    }

    private static ShuntCompensatorMapData toMapData(ShuntCompensator shuntCompensator) {
        Terminal terminal = shuntCompensator.getTerminal();
        ShuntCompensatorMapData.ShuntCompensatorMapDataBuilder builder = ShuntCompensatorMapData.builder()
                .name(shuntCompensator.getOptionalName().orElse(null))
                .id(shuntCompensator.getId())
                .maximumSectionCount(shuntCompensator.getMaximumSectionCount())
                .sectionCount(shuntCompensator.getSectionCount())
                .terminalConnected(terminal.isConnected())
                .voltageLevelId(terminal.getVoltageLevel().getId());
        if (shuntCompensator.getModel() instanceof  ShuntCompensatorLinearModel) {
            builder.bPerSection(shuntCompensator.getModel(ShuntCompensatorLinearModel.class).getBPerSection());
        }
        //TODO handle shuntCompensator non linear model
        if (!Double.isNaN(terminal.getQ())) {
            builder.q(terminal.getQ());
        }
        if (!Double.isNaN(shuntCompensator.getTargetV())) {
            builder.targetV(shuntCompensator.getTargetV());
        }
        if (!Double.isNaN(shuntCompensator.getTargetDeadband())) {
            builder.targetDeadband(shuntCompensator.getTargetDeadband());
        }

        var connectablePosition = shuntCompensator.getExtension(ConnectablePosition.class);
        if (connectablePosition != null) {
            builder
                    .connectionDirection(connectablePosition.getFeeder().getDirection())
                    .connectionName(connectablePosition.getFeeder().getName());
            connectablePosition.getFeeder().getOrder().ifPresent(builder::connectionPosition);
        }

        return builder.build();
    }

    private static StaticVarCompensatorMapData toMapData(StaticVarCompensator staticVarCompensator) {
        Terminal terminal = staticVarCompensator.getTerminal();
        StaticVarCompensatorMapData.StaticVarCompensatorMapDataBuilder builder = StaticVarCompensatorMapData.builder()
            .name(staticVarCompensator.getOptionalName().orElse(null))
            .id(staticVarCompensator.getId())
            .terminalConnected(terminal.isConnected())
            .voltageLevelId(terminal.getVoltageLevel().getId())
            .regulationMode(staticVarCompensator.getRegulationMode());
        if (!Double.isNaN(terminal.getP())) {
            builder.p(terminal.getP());
        }
        if (!Double.isNaN(terminal.getQ())) {
            builder.q(terminal.getQ());
        }
        if (!Double.isNaN(staticVarCompensator.getVoltageSetpoint())) {
            builder.voltageSetpoint(staticVarCompensator.getVoltageSetpoint());
        }
        if (!Double.isNaN(staticVarCompensator.getReactivePowerSetpoint())) {
            builder.reactivePowerSetpoint(staticVarCompensator.getReactivePowerSetpoint());
        }
        return builder.build();
    }

    private static BusMapData toMapData(Bus bus) {
        return BusMapData.builder()
            .name(bus.getOptionalName().orElse(null))
            .id(bus.getId())
            .build();
    }

    private static BusbarSectionMapData toMapData(BusbarSection busbarSection) {
        BusbarSectionMapData.BusbarSectionMapDataBuilder builder = BusbarSectionMapData.builder()
            .name(busbarSection.getOptionalName().orElse(null))
            .id(busbarSection.getId());
        var busbarSectionPosition = busbarSection.getExtension(BusbarSectionPosition.class);
        if (busbarSectionPosition != null) {
            builder
                .vertPos(busbarSectionPosition.getBusbarIndex())
                .horizPos(busbarSectionPosition.getSectionIndex());
        }
        return builder.build();
    }

    public List<SubstationMapData> getSubstations(UUID networkUuid, String variantId, List<String> substationsId) {
        Network network = getNetwork(networkUuid, substationsId == null ? PreloadingStrategy.COLLECTION : PreloadingStrategy.NONE, variantId);
        if (substationsId == null) {
            return network.getSubstationStream()
                .map(NetworkMapService::toMapData).collect(Collectors.toList());
        } else {
            List<SubstationMapData> res = new ArrayList<>();
            substationsId.stream().forEach(id -> res.add(toMapData(network.getSubstation(id))));
            return res;
        }
    }

    public SubstationMapData getSubstation(UUID networkUuid, String variantId, String substationId) {
        Substation substation = getNetwork(networkUuid, PreloadingStrategy.NONE, variantId).getSubstation(substationId);
        if (substation == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return toMapData(substation);
    }

    public List<LineMapData> getLines(UUID networkUuid, String variantId, List<String> substationsId) {
        Network network = getNetwork(networkUuid, substationsId == null ? PreloadingStrategy.COLLECTION : PreloadingStrategy.NONE, variantId);
        if (substationsId == null) {
            return network.getLineStream()
                .map(NetworkMapService::toMapData).collect(Collectors.toList());
        } else {
            Set<LineMapData> res = new LinkedHashSet<>();
            substationsId.stream().forEach(id ->
                network.getSubstation(id).getVoltageLevelStream().forEach(v ->
                    v.getConnectables(Line.class).forEach(l -> res.add(toMapData(l)))));
            return res.stream().collect(Collectors.toList());
        }
    }

    public LineMapData getLine(UUID networkUuid, String variantId, String lineId) {
        Line line = getNetwork(networkUuid, PreloadingStrategy.NONE, variantId).getLine(lineId);
        if (line == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return toMapData(line);
    }

    public List<GeneratorMapData> getGenerators(UUID networkUuid, String variantId, List<String> substationsId) {
        Network network = getNetwork(networkUuid, substationsId == null ? PreloadingStrategy.COLLECTION : PreloadingStrategy.NONE, variantId);
        if (substationsId == null) {
            return network.getGeneratorStream()
                .map(NetworkMapService::toMapData).collect(Collectors.toList());
        } else {
            Set<GeneratorMapData> res = new LinkedHashSet<>();
            substationsId.stream().forEach(id ->
                network.getSubstation(id).getVoltageLevelStream().forEach(v ->
                    v.getConnectables(Generator.class).forEach(g -> res.add(toMapData(g)))));
            return res.stream().collect(Collectors.toList());
        }
    }

    public GeneratorMapData getGenerator(UUID networkUuid, String variantId, String generatorId) {
        Generator generator = getNetwork(networkUuid, PreloadingStrategy.NONE, variantId).getGenerator(generatorId);
        if (generator == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return toMapData(generator);
    }

    public List<TwoWindingsTransformerMapData> getTwoWindingsTransformers(UUID networkUuid, String variantId, List<String> substationsId) {
        Network network = getNetwork(networkUuid, substationsId == null ? PreloadingStrategy.COLLECTION : PreloadingStrategy.NONE, variantId);
        if (substationsId == null) {
            return network.getTwoWindingsTransformerStream()
                .map(NetworkMapService::toMapData).collect(Collectors.toList());
        } else {
            Set<TwoWindingsTransformerMapData> res = new LinkedHashSet<>();
            substationsId.stream().forEach(id ->
                network.getSubstation(id).getVoltageLevelStream().forEach(v ->
                    v.getConnectables(TwoWindingsTransformer.class).forEach(t -> res.add(toMapData(t)))));
            return res.stream().collect(Collectors.toList());
        }
    }

    public TwoWindingsTransformerMapData getTwoWindingsTransformer(UUID networkUuid, String variantId, String twoWindingsTransformerId) {
        TwoWindingsTransformer twoWindingsTransformer = getNetwork(networkUuid, PreloadingStrategy.NONE, variantId).getTwoWindingsTransformer(twoWindingsTransformerId);
        if (twoWindingsTransformer == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return toMapData(twoWindingsTransformer);
    }

    public List<ThreeWindingsTransformerMapData> getThreeWindingsTransformers(UUID networkUuid, String variantId, List<String> substationsId) {
        Network network = getNetwork(networkUuid, substationsId == null ? PreloadingStrategy.COLLECTION : PreloadingStrategy.NONE, variantId);
        if (substationsId == null) {
            return network.getThreeWindingsTransformerStream()
                .map(NetworkMapService::toMapData).collect(Collectors.toList());
        } else {
            Set<ThreeWindingsTransformerMapData> res = new LinkedHashSet<>();
            substationsId.stream().forEach(id ->
                network.getSubstation(id).getVoltageLevelStream().forEach(v ->
                    v.getConnectables(ThreeWindingsTransformer.class).forEach(t -> res.add(toMapData(t)))));
            return res.stream().collect(Collectors.toList());
        }
    }

    public AllMapData getAll(UUID networkUuid, String variantId, List<String> substationsId) {
        Network network = getNetwork(networkUuid, substationsId == null ? PreloadingStrategy.COLLECTION : PreloadingStrategy.NONE, variantId);

        if (substationsId == null) {
            return AllMapData.builder()
                .substations(network.getSubstationStream().map(NetworkMapService::toMapData).collect(Collectors.toList()))
                .lines(network.getLineStream().map(NetworkMapService::toMapData).collect(Collectors.toList()))
                .generators(network.getGeneratorStream().map(NetworkMapService::toMapData).collect(Collectors.toList()))
                .twoWindingsTransformers(network.getTwoWindingsTransformerStream().map(NetworkMapService::toMapData).collect(Collectors.toList()))
                .threeWindingsTransformers(network.getThreeWindingsTransformerStream().map(NetworkMapService::toMapData).collect(Collectors.toList()))
                .batteries(network.getBatteryStream().map(NetworkMapService::toMapData).collect(Collectors.toList()))
                .danglingLines(network.getDanglingLineStream().map(NetworkMapService::toMapData).collect(Collectors.toList()))
                .hvdcLines(network.getHvdcLineStream().map(NetworkMapService::toMapData).collect(Collectors.toList()))
                .lccConverterStations(network.getLccConverterStationStream().map(NetworkMapService::toMapData).collect(Collectors.toList()))
                .loads(network.getLoadStream().map(NetworkMapService::toMapData).collect(Collectors.toList()))
                .shuntCompensators(network.getShuntCompensatorStream().map(NetworkMapService::toMapData).collect(Collectors.toList()))
                .staticVarCompensators(network.getStaticVarCompensatorStream().map(NetworkMapService::toMapData).collect(Collectors.toList()))
                .vscConverterStations(network.getVscConverterStationStream().map(NetworkMapService::toMapData).collect(Collectors.toList()))
                .build();
        } else {
            Set<SubstationMapData> substationsMap = new LinkedHashSet<>();
            Set<LineMapData> linesMap = new LinkedHashSet<>();
            Set<GeneratorMapData> generatorsMap = new LinkedHashSet<>();
            Set<TwoWindingsTransformerMapData> twoWindingsTransformersMap = new LinkedHashSet<>();
            Set<ThreeWindingsTransformerMapData> threeWindingsTransformersMap = new LinkedHashSet<>();
            Set<BatteryMapData> batteriesMap = new LinkedHashSet<>();
            Set<DanglingLineMapData> danglingLinesMap = new LinkedHashSet<>();
            Set<HvdcLineMapData> hvdcLinesMap = new LinkedHashSet<>();
            Set<LccConverterStationMapData> lccConverterStationsMap = new LinkedHashSet<>();
            Set<LoadMapData> loadsMap = new LinkedHashSet<>();
            Set<ShuntCompensatorMapData> shuntCompensatorsMap = new LinkedHashSet<>();
            Set<StaticVarCompensatorMapData> staticVarCompensatorsMap = new LinkedHashSet<>();
            Set<VscConverterStationMapData> vscConverterStationsMap = new LinkedHashSet<>();

            substationsId.stream().forEach(id -> {
                Substation substation = network.getSubstation(id);
                substationsMap.add(toMapData(substation));
                substation.getVoltageLevelStream().forEach(v ->
                    v.getConnectables().forEach(c -> {
                        switch (c.getType()) {
                            case LINE:
                                linesMap.add(toMapData((Line) c));
                                break;
                            case TWO_WINDINGS_TRANSFORMER:
                                twoWindingsTransformersMap.add(toMapData((TwoWindingsTransformer) c));
                                break;
                            case THREE_WINDINGS_TRANSFORMER:
                                threeWindingsTransformersMap.add(toMapData((ThreeWindingsTransformer) c));
                                break;
                            case GENERATOR:
                                generatorsMap.add(toMapData((Generator) c));
                                break;
                            case BATTERY:
                                batteriesMap.add(toMapData((Battery) c));
                                break;
                            case LOAD:
                                loadsMap.add(toMapData((Load) c));
                                break;
                            case SHUNT_COMPENSATOR:
                                shuntCompensatorsMap.add(toMapData((ShuntCompensator) c));
                                break;
                            case DANGLING_LINE:
                                danglingLinesMap.add(toMapData((DanglingLine) c));
                                break;
                            case STATIC_VAR_COMPENSATOR:
                                staticVarCompensatorsMap.add(toMapData((StaticVarCompensator) c));
                                break;
                            case HVDC_CONVERTER_STATION: {
                                HvdcConverterStation<?> hdvcConverter = (HvdcConverterStation<?>) c;
                                HvdcLine hvdcLine = hdvcConverter.getHvdcLine();
                                if (hvdcLine != null) {
                                    hvdcLinesMap.add(toMapData(hvdcLine));
                                }
                                if (hdvcConverter.getHvdcType() == HvdcConverterStation.HvdcType.LCC) {
                                    lccConverterStationsMap.add(toMapData((LccConverterStation) hdvcConverter));
                                } else {
                                    vscConverterStationsMap.add(toMapData((VscConverterStation) hdvcConverter));
                                }
                            }
                            break;
                            default:
                        }
                    })
                );
            });
            return AllMapData.builder()
                .substations(substationsMap.stream().collect(Collectors.toList()))
                .lines(linesMap.stream().collect(Collectors.toList()))
                .generators(generatorsMap.stream().collect(Collectors.toList()))
                .twoWindingsTransformers(twoWindingsTransformersMap.stream().collect(Collectors.toList()))
                .threeWindingsTransformers(threeWindingsTransformersMap.stream().collect(Collectors.toList()))
                .batteries(batteriesMap.stream().collect(Collectors.toList()))
                .danglingLines(danglingLinesMap.stream().collect(Collectors.toList()))
                .hvdcLines(hvdcLinesMap.stream().collect(Collectors.toList()))
                .lccConverterStations(lccConverterStationsMap.stream().collect(Collectors.toList()))
                .loads(loadsMap.stream().collect(Collectors.toList()))
                .shuntCompensators(shuntCompensatorsMap.stream().collect(Collectors.toList()))
                .staticVarCompensators(staticVarCompensatorsMap.stream().collect(Collectors.toList()))
                .vscConverterStations(vscConverterStationsMap.stream().collect(Collectors.toList()))
                .build();
        }
    }

    public List<BatteryMapData> getBatteries(UUID networkUuid, String variantId, List<String> substationsId) {
        Network network = getNetwork(networkUuid, substationsId == null ? PreloadingStrategy.COLLECTION : PreloadingStrategy.NONE, variantId);
        if (substationsId == null) {
            return network.getBatteryStream()
                .map(NetworkMapService::toMapData).collect(Collectors.toList());
        } else {
            Set<BatteryMapData> res = new LinkedHashSet<>();
            substationsId.stream().forEach(id ->
                network.getSubstation(id).getVoltageLevelStream().forEach(v ->
                    v.getConnectables(Battery.class).forEach(b -> res.add(toMapData(b)))));
            return res.stream().collect(Collectors.toList());
        }
    }

    public List<DanglingLineMapData> getDanglingLines(UUID networkUuid, String variantId, List<String> substationsId) {
        Network network = getNetwork(networkUuid, substationsId == null ? PreloadingStrategy.COLLECTION : PreloadingStrategy.NONE, variantId);
        if (substationsId == null) {
            return network.getDanglingLineStream()
                .map(NetworkMapService::toMapData).collect(Collectors.toList());
        } else {
            Set<DanglingLineMapData> res = new LinkedHashSet<>();
            substationsId.stream().forEach(id ->
                network.getSubstation(id).getVoltageLevelStream().forEach(v ->
                    v.getConnectables(DanglingLine.class).forEach(d -> res.add(toMapData(d)))));
            return res.stream().collect(Collectors.toList());
        }
    }

    public List<HvdcLineMapData> getHvdcLines(UUID networkUuid, String variantId, List<String> substationsId) {
        Network network = getNetwork(networkUuid, substationsId == null ? PreloadingStrategy.COLLECTION : PreloadingStrategy.NONE, variantId);
        if (substationsId == null) {
            return network.getHvdcLineStream()
                .map(NetworkMapService::toMapData).collect(Collectors.toList());
        } else {
            Set<HvdcLineMapData> res = new LinkedHashSet<>();
            substationsId.stream().forEach(id ->
                network.getSubstation(id).getVoltageLevelStream().forEach(v ->
                    v.getConnectables(HvdcConverterStation.class).forEach(h -> {
                        HvdcLine hvdcLine = h.getHvdcLine();
                        if (hvdcLine != null) {
                            res.add(toMapData(hvdcLine));
                        }
                    })));
            return res.stream().collect(Collectors.toList());
        }
    }

    public List<LccConverterStationMapData> getLccConverterStations(UUID networkUuid, String variantId, List<String> substationsId) {
        Network network = getNetwork(networkUuid, substationsId == null ? PreloadingStrategy.COLLECTION : PreloadingStrategy.NONE, variantId);
        if (substationsId == null) {
            return network.getLccConverterStationStream()
                .map(NetworkMapService::toMapData).collect(Collectors.toList());
        } else {
            Set<LccConverterStationMapData> res = new LinkedHashSet<>();
            substationsId.stream().forEach(id ->
                network.getSubstation(id).getVoltageLevelStream().forEach(v ->
                    v.getConnectables(LccConverterStation.class).forEach(l -> res.add(toMapData(l)))));
            return res.stream().collect(Collectors.toList());
        }
    }

    public List<LoadMapData> getLoads(UUID networkUuid, String variantId, List<String> substationsId) {
        Network network = getNetwork(networkUuid, substationsId == null ? PreloadingStrategy.COLLECTION : PreloadingStrategy.NONE, variantId);
        if (substationsId == null) {
            return network.getLoadStream()
                .map(NetworkMapService::toMapData).collect(Collectors.toList());
        } else {
            Set<LoadMapData> res = new LinkedHashSet<>();
            substationsId.stream().forEach(id ->
                network.getSubstation(id).getVoltageLevelStream().forEach(v ->
                    v.getConnectables(Load.class).forEach(l -> res.add(toMapData(l)))));
            return res.stream().collect(Collectors.toList());
        }
    }

    public LoadMapData getLoad(UUID networkUuid, String variantId, String loadId) {
        Load load = getNetwork(networkUuid, PreloadingStrategy.NONE, variantId).getLoad(loadId);
        if (load == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return toMapData(load);
    }

    public List<ShuntCompensatorMapData> getShuntCompensators(UUID networkUuid, String variantId, List<String> substationsId) {
        Network network = getNetwork(networkUuid, substationsId == null ? PreloadingStrategy.COLLECTION : PreloadingStrategy.NONE, variantId);
        if (substationsId == null) {
            return network.getShuntCompensatorStream()
                .map(NetworkMapService::toMapData).collect(Collectors.toList());
        } else {
            Set<ShuntCompensatorMapData> res = new LinkedHashSet<>();
            substationsId.stream().forEach(id ->
                network.getSubstation(id).getVoltageLevelStream().forEach(v ->
                    v.getConnectables(ShuntCompensator.class).forEach(s -> res.add(toMapData(s)))));
            return res.stream().collect(Collectors.toList());
        }
    }

    public ShuntCompensatorMapData getShuntCompensator(UUID networkUuid, String variantId, String shuntCompensatorId) {
        ShuntCompensator shuntCompensator = getNetwork(networkUuid, PreloadingStrategy.NONE, variantId).getShuntCompensator(shuntCompensatorId);
        if (shuntCompensator == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return toMapData(shuntCompensator);
    }

    public List<StaticVarCompensatorMapData> getStaticVarCompensators(UUID networkUuid, String variantId, List<String> substationsId) {
        Network network = getNetwork(networkUuid, substationsId == null ? PreloadingStrategy.COLLECTION : PreloadingStrategy.NONE, variantId);
        if (substationsId == null) {
            return network.getStaticVarCompensatorStream()
                .map(NetworkMapService::toMapData).collect(Collectors.toList());
        } else {
            Set<StaticVarCompensatorMapData> res = new LinkedHashSet<>();
            substationsId.stream().forEach(id ->
                network.getSubstation(id).getVoltageLevelStream().forEach(v ->
                    v.getConnectables(StaticVarCompensator.class).forEach(s -> res.add(toMapData(s)))));
            return res.stream().collect(Collectors.toList());
        }
    }

    public List<VscConverterStationMapData> getVscConverterStations(UUID networkUuid, String variantId, List<String> substationsId) {
        Network network = getNetwork(networkUuid, substationsId == null ? PreloadingStrategy.COLLECTION : PreloadingStrategy.NONE, variantId);
        if (substationsId == null) {
            return network.getVscConverterStationStream()
                .map(NetworkMapService::toMapData).collect(Collectors.toList());
        } else {
            Set<VscConverterStationMapData> res = new LinkedHashSet<>();
            substationsId.stream().forEach(id ->
                network.getSubstation(id).getVoltageLevelStream().forEach(v ->
                    v.getConnectables(VscConverterStation.class).forEach(s -> res.add(toMapData(s)))));
            return res.stream().collect(Collectors.toList());
        }
    }

    public List<VoltageLevelMapData> getVoltageLevels(UUID networkUuid, String variantId, List<String> substationsId) {
        Network network = getNetwork(networkUuid, substationsId == null ? PreloadingStrategy.COLLECTION : PreloadingStrategy.NONE, variantId);
        return substationsId == null ?
            network.getVoltageLevelStream().map(NetworkMapService::toMapData).collect(Collectors.toList()) :
            substationsId.stream().flatMap(id -> network.getSubstation(id).getVoltageLevelStream().map(NetworkMapService::toMapData)).collect(Collectors.toList());
    }

    public List<VoltageLevelsEquipmentsMapData> getVoltageLevelsAndConnectable(UUID networkUuid, String variantId, List<String> substationsId) {
        Network network = getNetwork(networkUuid, substationsId == null ? PreloadingStrategy.COLLECTION : PreloadingStrategy.NONE, variantId);
        List<VoltageLevel> voltageLevels =  substationsId == null ?
                network.getVoltageLevelStream().collect(Collectors.toList()) :
                substationsId.stream().flatMap(id -> network.getSubstation(id).getVoltageLevelStream()).collect(Collectors.toList());

        return voltageLevels.stream().map(vl -> {
            List<VoltageLevelConnectableMapData> equipments = new ArrayList<>();
            vl.getConnectables().forEach(connectable -> equipments.add(toMapData(connectable)));
            return VoltageLevelsEquipmentsMapData.builder().voltageLevel(toMapData(vl)).equipments(equipments).build();
        }).collect(Collectors.toList());
    }

    public VoltageLevelMapData getVoltageLevel(UUID networkUuid, String variantId, String voltageLevelId) {
        VoltageLevel voltageLevel = getNetwork(networkUuid, PreloadingStrategy.NONE, variantId).getVoltageLevel(voltageLevelId);
        if (voltageLevel == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return toMapData(voltageLevel);
    }

    public List<BusMapData> getVoltageLevelBuses(UUID networkUuid, String voltageLevelId, String variantId) {
        Network network = getNetwork(networkUuid, PreloadingStrategy.NONE, variantId);
        return network.getVoltageLevel(voltageLevelId).getBusBreakerView().getBusStream()
            .map(NetworkMapService::toMapData).collect(Collectors.toList());
    }

    public List<BusbarSectionMapData> getVoltageLevelBusbarSections(UUID networkUuid, String voltageLevelId, String variantId) {
        Network network = getNetwork(networkUuid, PreloadingStrategy.NONE, variantId);
        return network.getVoltageLevel(voltageLevelId).getNodeBreakerView().getBusbarSectionStream()
            .map(NetworkMapService::toMapData).collect(Collectors.toList());
    }

    public List<SubstationMapData> getMapSubstations(UUID networkUuid, String variantId, List<String> substationsId) {
        Network network = getNetwork(networkUuid, substationsId == null ? PreloadingStrategy.COLLECTION : PreloadingStrategy.NONE, variantId);

        if (substationsId == null) {
            return network.getSubstationStream().map(NetworkMapService::toBasicMapData).collect(Collectors.toList());
        } else {
            return substationsId.stream().map(id -> toBasicMapData(network.getSubstation(id))).collect(Collectors.toList());
        }
    }

    public List<LineMapData> getMapLines(UUID networkUuid, String variantId, List<String> substationsId) {
        Network network = getNetwork(networkUuid, substationsId == null ? PreloadingStrategy.COLLECTION : PreloadingStrategy.NONE, variantId);

        if (substationsId == null) {
            return network.getLineStream().map(NetworkMapService::toBasicMapData).collect(Collectors.toList());
        } else {
            Set<LineMapData> lines = new LinkedHashSet<>();
            substationsId.forEach(id ->
                    network.getSubstation(id).getVoltageLevelStream().forEach(v ->
                            v.getConnectables(Line.class).forEach(l -> lines.add(toBasicMapData(l)))));
            return new ArrayList<>(lines);

        }
    }
}
