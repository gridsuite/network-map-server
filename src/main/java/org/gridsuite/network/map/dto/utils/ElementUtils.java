/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.utils;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.*;
import com.powsybl.math.graph.TraversalType;
import org.gridsuite.network.map.dto.definition.extension.*;
import org.gridsuite.network.map.dto.definition.threewindingstransformer.ThreeWindingsTransformerTabInfos;
import org.gridsuite.network.map.model.*;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
public final class ElementUtils {
    private ElementUtils() {
    }

    public static Double nullIfNan(double d) {
        return Double.isNaN(d) ? null : d;
    }

    private static ConnectablePosition.Feeder getFeederInfos(Identifiable<?> identifiable, int index) {
        var connectablePosition = identifiable.getExtension(ConnectablePosition.class);
        if (connectablePosition == null) {
            return null;
        }

        switch (index) {
            case 0:
                return connectablePosition.getFeeder();
            case 1:
                return connectablePosition.getFeeder1();
            case 2:
                return connectablePosition.getFeeder2();
            default:
                throw new IllegalArgumentException("Invalid feeder index: " + index);
        }
    }

    public static ConnectablePositionInfos toMapConnectablePosition(Identifiable<?> branch, int index) {
        ConnectablePositionInfos.ConnectablePositionInfosBuilder builder = ConnectablePositionInfos.builder();
        ConnectablePosition.Feeder feeder = getFeederInfos(branch, index);
        if (feeder != null) {
            builder.connectionDirection(feeder.getDirection() == null ? null : feeder.getDirection())
                    .connectionPosition(feeder.getOrder().orElse(null))
                    .connectionName(feeder.getName().orElse(null));
        }
        return builder.build();
    }

    public static Optional<HvdcAngleDroopActivePowerControlInfos> toHvdcAngleDroopActivePowerControlIdentifiable(HvdcLine hvdcLine) {
        HvdcAngleDroopActivePowerControl hvdcAngleDroopActivePowerControl = hvdcLine.getExtension(HvdcAngleDroopActivePowerControl.class);
        return hvdcAngleDroopActivePowerControl == null ? Optional.empty() :
                Optional.of(HvdcAngleDroopActivePowerControlInfos.builder()
                                .droop(hvdcAngleDroopActivePowerControl.getDroop())
                                .isEnabled(hvdcAngleDroopActivePowerControl.isEnabled())
                                .p0(hvdcAngleDroopActivePowerControl.getP0()).build());
    }

    public static Optional<HvdcOperatorActivePowerRangeInfos> toHvdcOperatorActivePowerRange(HvdcLine hvdcLine) {
        HvdcOperatorActivePowerRange hvdcOperatorActivePowerRange = hvdcLine.getExtension(HvdcOperatorActivePowerRange.class);
        return hvdcOperatorActivePowerRange == null ? Optional.empty() :
                Optional.of(HvdcOperatorActivePowerRangeInfos.builder()
                        .oprFromCS1toCS2(hvdcOperatorActivePowerRange.getOprFromCS1toCS2())
                        .oprFromCS2toCS1(hvdcOperatorActivePowerRange.getOprFromCS2toCS1()).build());
    }

    public static Optional<ActivePowerControlInfos> toActivePowerControl(Identifiable<?> identifiable) {
        var activePowerControl = identifiable.getExtension(ActivePowerControl.class);
        return activePowerControl == null ? Optional.empty() :
                Optional.of(ActivePowerControlInfos.builder()
                        .participate(activePowerControl.isParticipate())
                        .droop(activePowerControl.getDroop()).build());
    }

    public static String toOperatingStatus(Identifiable<?> identifiable) {
        if (identifiable instanceof Branch<?> branch) {
            var operatingStatus = branch.getExtension(OperatingStatus.class);
            return operatingStatus == null ? null : operatingStatus.getStatus().name();
        } else if (identifiable instanceof ThreeWindingsTransformer threeWT) {
            var operatingStatus = threeWT.getExtension(OperatingStatus.class);
            return operatingStatus == null ? null : operatingStatus.getStatus().name();
        } else if (identifiable instanceof HvdcLine hvdcLine) {
            var operatingStatus = hvdcLine.getExtension(OperatingStatus.class);
            return operatingStatus == null ? null : operatingStatus.getStatus().name();
        } else {
            return null;
        }
    }

    public static Optional<GeneratorShortCircuitInfos> toGeneratorShortCircuit(Generator generator) {
        GeneratorShortCircuit generatorShortCircuit = generator.getExtension(GeneratorShortCircuit.class);
        return generatorShortCircuit == null ? Optional.empty() :
                Optional.of(GeneratorShortCircuitInfos.builder()
                        .directTransX(generatorShortCircuit.getDirectTransX())
                        .stepUpTransformerX(generatorShortCircuit.getStepUpTransformerX()).build());
    }

    public static CoordinatedReactiveControlInfos toCoordinatedReactiveControl(Generator generator) {
        CoordinatedReactiveControlInfos.CoordinatedReactiveControlInfosBuilder builder = CoordinatedReactiveControlInfos.builder();
        CoordinatedReactiveControl coordinatedReactiveControl = generator.getExtension(CoordinatedReactiveControl.class);
        if (coordinatedReactiveControl != null) {
            builder.qPercent(coordinatedReactiveControl.getQPercent());
        } else {
            builder.qPercent(Double.NaN);
        }
        return builder.build();
    }

    public static Optional<GeneratorStartupInfos> toGeneratorStartup(Generator generator) {
        GeneratorStartup generatorStartup = generator.getExtension(GeneratorStartup.class);
        return generatorStartup == null ? Optional.empty() :
                Optional.of(GeneratorStartupInfos.builder()
                        .plannedActivePowerSetPoint(nullIfNan(generatorStartup.getPlannedActivePowerSetpoint()))
                        .marginalCost(nullIfNan(generatorStartup.getMarginalCost()))
                        .plannedOutageRate(nullIfNan(generatorStartup.getPlannedOutageRate()))
                        .forcedOutageRate(nullIfNan(generatorStartup.getForcedOutageRate())).build());
    }

    public static Optional<IdentifiableShortCircuitInfos> toIdentifiableShortCircuit(VoltageLevel voltageLevel) {
        IdentifiableShortCircuit<VoltageLevel> identifiableShortCircuit = voltageLevel.getExtension(IdentifiableShortCircuit.class);
        return identifiableShortCircuit == null ? Optional.empty() :
                Optional.of(IdentifiableShortCircuitInfos.builder()
                        .ipMin(identifiableShortCircuit.getIpMin())
                        .ipMax(identifiableShortCircuit.getIpMax()).build());
    }

    public static CurrentLimitsData toMapDataCurrentLimits(CurrentLimits limits) {
        CurrentLimitsData.CurrentLimitsDataBuilder builder = CurrentLimitsData.builder();
        boolean empty = true;
        if (!Double.isNaN(limits.getPermanentLimit())) {
            builder.permanentLimit(limits.getPermanentLimit());
            empty = false;
        }
        if (!CollectionUtils.isEmpty(limits.getTemporaryLimits())) {
            builder.temporaryLimits(toMapDataTemporaryLimit(limits.getTemporaryLimits()));
            empty = false;
        }
        return empty ? null : builder.build();
    }

    private static List<TemporaryLimitData> toMapDataTemporaryLimit(Collection<LoadingLimits.TemporaryLimit> limits) {
        return limits.stream()
                .map(l -> TemporaryLimitData.builder()
                        .name(l.getName())
                        .acceptableDuration(l.getAcceptableDuration() == Integer.MAX_VALUE ? null : l.getAcceptableDuration())
                        .value(l.getValue() == Double.MAX_VALUE ? null : l.getValue())
                        .build())
                .collect(Collectors.toList());
    }

    public static String getBusOrBusbarSection(Terminal terminal) {
        String busOrBusbarSectionId;
        if (terminal.getVoltageLevel().getTopologyKind().equals(TopologyKind.BUS_BREAKER)) {
            if (terminal.isConnected()) {
                busOrBusbarSectionId = terminal.getBusBreakerView().getBus().getId();
            } else {
                busOrBusbarSectionId = terminal.getBusBreakerView().getConnectableBus().getId();
            }
        } else {
            busOrBusbarSectionId = getBusbarSectionId(terminal);
        }
        return busOrBusbarSectionId;
    }

    public static String getBusbarSectionId(Terminal terminal) {
        BusbarSectionFinderTraverser connectedBusbarSectionFinder = new BusbarSectionFinderTraverser(terminal.isConnected());
        terminal.traverse(connectedBusbarSectionFinder, TraversalType.BREADTH_FIRST);
        return connectedBusbarSectionFinder.getFirstTraversedBbsId();
    }

    public static List<TapChangerStepData> toMapDataPhaseStep(Map<Integer, PhaseTapChangerStep> tapChangerStep) {
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

    public static TapChangerData toMapData(PhaseTapChanger tapChanger) {
        if (tapChanger == null) {
            return null;
        }

        TapChangerData.TapChangerDataBuilder builder = TapChangerData.builder()
                .lowTapPosition(tapChanger.getLowTapPosition())
                .highTapPosition(tapChanger.getHighTapPosition())
                .tapPosition(tapChanger.getTapPosition())
                .isRegulating(tapChanger.isRegulating())
                .regulationMode(tapChanger.getRegulationMode())
                .regulationValue(tapChanger.getRegulationValue())
                .targetDeadband(tapChanger.getTargetDeadband())
                .regulatingTerminalConnectableId(tapChanger.getRegulationTerminal() != null ? tapChanger.getRegulationTerminal().getConnectable().getId() : null)
                .regulatingTerminalConnectableType(tapChanger.getRegulationTerminal() != null ? tapChanger.getRegulationTerminal().getConnectable().getType().name() : null)
                .regulatingTerminalVlId(tapChanger.getRegulationTerminal() != null ? tapChanger.getRegulationTerminal().getVoltageLevel().getId() : null)
                .steps(toMapDataPhaseStep(tapChanger.getAllSteps()));

        builder.targetDeadband(nullIfNan(tapChanger.getTargetDeadband()));
        builder.regulationValue(nullIfNan(tapChanger.getRegulationValue()));
        return builder.build();
    }

    public static TapChangerData toMapData(RatioTapChanger tapChanger) {
        if (tapChanger == null) {
            return null;
        }

        TapChangerData.TapChangerDataBuilder builder = TapChangerData.builder()
                .lowTapPosition(tapChanger.getLowTapPosition())
                .highTapPosition(tapChanger.getHighTapPosition())
                .tapPosition(tapChanger.getTapPosition())
                .isRegulating(tapChanger.isRegulating())
                .hasLoadTapChangingCapabilities(tapChanger.hasLoadTapChangingCapabilities())
                .regulatingTerminalConnectableId(tapChanger.getRegulationTerminal() != null ? tapChanger.getRegulationTerminal().getConnectable().getId() : null)
                .regulatingTerminalConnectableType(tapChanger.getRegulationTerminal() != null ? tapChanger.getRegulationTerminal().getConnectable().getType().name() : null)
                .regulatingTerminalVlId(tapChanger.getRegulationTerminal() != null ? tapChanger.getRegulationTerminal().getVoltageLevel().getId() : null)
                .steps(toMapDataRatioStep(tapChanger.getAllSteps()));

        builder.targetV(nullIfNan(tapChanger.getTargetV()));
        builder.targetDeadband(nullIfNan(tapChanger.getTargetDeadband()));
        return builder.build();
    }

    public static List<TapChangerStepData> toMapDataRatioStep(Map<Integer, RatioTapChangerStep> tapChangerStep) {
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

    public static void mapThreeWindingsTransformerPermanentLimits(
            ThreeWindingsTransformerTabInfos.ThreeWindingsTransformerTabInfosBuilder builder,
            ThreeWindingsTransformer transformer) {
        CurrentLimits limits1 = transformer.getLeg1().getCurrentLimits().orElse(null);
        CurrentLimits limits2 = transformer.getLeg2().getCurrentLimits().orElse(null);
        CurrentLimits limits3 = transformer.getLeg3().getCurrentLimits().orElse(null);
        if (limits1 != null && !Double.isNaN(limits1.getPermanentLimit())) {
            builder.permanentLimit1(limits1.getPermanentLimit());
        }
        if (limits2 != null && !Double.isNaN(limits2.getPermanentLimit())) {
            builder.permanentLimit2(limits2.getPermanentLimit());
        }
        if (limits3 != null && !Double.isNaN(limits3.getPermanentLimit())) {
            builder.permanentLimit3(limits3.getPermanentLimit());
        }
    }

    public static void mapThreeWindingsTransformerRatioTapChangers(
            ThreeWindingsTransformerTabInfos.ThreeWindingsTransformerTabInfosBuilder builder,
            ThreeWindingsTransformer transformer) {
        ThreeWindingsTransformer.Leg leg1 = transformer.getLeg1();
        ThreeWindingsTransformer.Leg leg2 = transformer.getLeg2();
        ThreeWindingsTransformer.Leg leg3 = transformer.getLeg3();
        if (leg1.hasRatioTapChanger()) {
            builder.ratioTapChanger1(toMapData(leg1.getRatioTapChanger()))
                    .hasLoadTapChanging1Capabilities(leg1.getRatioTapChanger().hasLoadTapChangingCapabilities())
                    .isRegulatingRatio1(leg1.getRatioTapChanger().isRegulating());
            if (!Double.isNaN(leg1.getRatioTapChanger().getTargetV())) {
                builder.targetV1(leg1.getRatioTapChanger().getTargetV());
            }
        }
        if (leg2.hasRatioTapChanger()) {
            builder.ratioTapChanger2(toMapData(leg2.getRatioTapChanger()))
                    .hasLoadTapChanging2Capabilities(leg2.getRatioTapChanger().hasLoadTapChangingCapabilities())
                    .isRegulatingRatio2(leg2.getRatioTapChanger().isRegulating());
            if (!Double.isNaN(leg2.getRatioTapChanger().getTargetV())) {
                builder.targetV2(leg2.getRatioTapChanger().getTargetV());
            }
        }
        if (leg3.hasRatioTapChanger()) {
            builder.ratioTapChanger3(toMapData(leg3.getRatioTapChanger()))
                    .hasLoadTapChanging3Capabilities(leg3.getRatioTapChanger().hasLoadTapChangingCapabilities())
                    .isRegulatingRatio3(leg3.getRatioTapChanger().isRegulating());
            if (!Double.isNaN(leg3.getRatioTapChanger().getTargetV())) {
                builder.targetV3(leg3.getRatioTapChanger().getTargetV());
            }
        }
        if (leg1.hasPhaseTapChanger()) {
            builder.phaseTapChanger1(toMapData(leg1.getPhaseTapChanger()))
                    .regulationModeName1(leg1.getPhaseTapChanger().getRegulationMode().name())
                    .isRegulatingPhase1(leg1.getPhaseTapChanger().isRegulating());
            if (!Double.isNaN(leg1.getPhaseTapChanger().getRegulationValue())) {
                builder.regulatingValue1(leg1.getPhaseTapChanger().getRegulationValue());
            }
        }
        if (leg2.hasPhaseTapChanger()) {
            builder.phaseTapChanger2(toMapData(leg2.getPhaseTapChanger()))
                    .regulationModeName2(leg2.getPhaseTapChanger().getRegulationMode().name())
                    .isRegulatingPhase2(leg2.getPhaseTapChanger().isRegulating());
            if (!Double.isNaN(leg2.getPhaseTapChanger().getRegulationValue())) {
                builder.regulatingValue2(leg2.getPhaseTapChanger().getRegulationValue());
            }
        }
        if (leg3.hasPhaseTapChanger()) {
            builder.phaseTapChanger3(toMapData(leg3.getPhaseTapChanger()))
                    .regulationModeName3(leg3.getPhaseTapChanger().getRegulationMode().name())
                    .isRegulatingPhase3(leg3.getPhaseTapChanger().isRegulating());
            if (!Double.isNaN(leg3.getPhaseTapChanger().getRegulationValue())) {
                builder.regulatingValue3(leg3.getPhaseTapChanger().getRegulationValue());
            }
        }
    }

    public static Country mapCountry(Substation substation) {
        return Optional.ofNullable(substation)
                .flatMap(Substation::getCountry)
                .orElse(null);
    }

    public static Map<String, String> getProperties(Identifiable<?> identifiable) {
        Map<String, String> properties = identifiable.getPropertyNames()
            .stream()
            .collect(Collectors.toMap(Function.identity(), identifiable::getProperty));
        return properties.isEmpty() ? null : properties;
    }

    public static double computeIntensity(Terminal terminal, Double dcPowerFactor) {
        double intensity = terminal.getI();

        if (Double.isNaN(intensity) && !Double.isNaN(terminal.getP()) && dcPowerFactor != null) {
            // After a DC load flow, the current at a terminal can be undefined (NaN). In that case, we use the DC power factor,
            // the nominal voltage and the active power at the terminal in order to approximate the current following formula
            // P = sqrt(3) x Vnom x I x dcPowerFactor
            intensity = 1000. * terminal.getP() / (Math.sqrt(3) * dcPowerFactor * terminal.getVoltageLevel().getNominalV());
        }
        return intensity;
    }

    public static List<ReactiveCapabilityCurveMapData> getReactiveCapabilityCurvePointsMapData(Collection<ReactiveCapabilityCurve.Point> points) {
        return points.stream()
                .map(point -> ReactiveCapabilityCurveMapData.builder()
                        .p(point.getP())
                        .maxQ(point.getMaxQ())
                        .minQ(point.getMinQ())
                        .build())
                .collect(Collectors.toList());
    }

    public static Substation findFirstSubstation(List<Terminal> terminals) {
        return terminals.stream()
            .map(Terminal::getVoltageLevel)
            .map(VoltageLevel::getSubstation)
            .filter(Optional::isPresent)
            .findFirst()
            .flatMap(Function.identity())
            .orElse(null);
    }

}
