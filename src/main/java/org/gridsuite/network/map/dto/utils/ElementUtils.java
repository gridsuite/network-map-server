/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.utils;

import com.powsybl.iidm.network.*;
import com.powsybl.math.graph.TraversalType;
import org.gridsuite.network.map.dto.common.ReactiveCapabilityCurveMapData;
import org.gridsuite.network.map.dto.common.TapChangerData;
import org.gridsuite.network.map.dto.common.TapChangerStepData;
import org.gridsuite.network.map.dto.definition.extension.BusbarSectionFinderTraverser;
import org.springframework.lang.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.DoubleConsumer;
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

    public static ConnectablePositionInfos toMapConnectablePosition(Identifiable<?> identifiable, int index) {
        ConnectablePosition.Feeder feeder = getFeederInfos(identifiable, index);
        return convertFeederToConnectablePositionInfos(feeder);
    }

    public static ConnectablePositionInfos convertFeederToConnectablePositionInfos(ConnectablePosition.Feeder feeder) {
        ConnectablePositionInfos.ConnectablePositionInfosBuilder builder = ConnectablePositionInfos.builder();
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

    public static void buildCurrentLimits(Collection<OperationalLimitsGroup> currentLimits, Consumer<List<CurrentLimitsData>> build) {
        List<CurrentLimitsData> currentLimitsData = currentLimits.stream()
                .map(ElementUtils::operationalLimitsGroupToMapDataCurrentLimits)
                .toList();
        if (!currentLimitsData.isEmpty()) {
            build.accept(currentLimitsData);
        }
    }

    private static CurrentLimitsData copyCurrentLimitsData(CurrentLimitsData currentLimitsData, Applicability applicability) {
        return CurrentLimitsData.builder()
            .id(currentLimitsData.getId())
            .applicability(applicability)
            .temporaryLimits(currentLimitsData.getTemporaryLimits())
            .permanentLimit(currentLimitsData.getPermanentLimit()).build();
    }

    /**
     * @return id of the selected operation limits group 1 and 2 if they have been renamed
     */
    public static void mergeCurrentLimits(Collection<OperationalLimitsGroup> operationalLimitsGroups1,
                                                          Collection<OperationalLimitsGroup> operationalLimitsGroups2,
                                                          Consumer<List<CurrentLimitsData>> build) {
        List<CurrentLimitsData> mergedLimitsData = new ArrayList<>();

        // Build temporary limit from side 1 and 2
        List<CurrentLimitsData> currentLimitsData1 = operationalLimitsGroups1.stream()
            .map(currentLimitsData -> ElementUtils.operationalLimitsGroupToMapDataCurrentLimits(currentLimitsData, SIDE1))
            .filter(Objects::nonNull)
            .toList();
        ArrayList<CurrentLimitsData> currentLimitsData2 = new ArrayList<>(operationalLimitsGroups2.stream()
            .map(currentLimitsData -> ElementUtils.operationalLimitsGroupToMapDataCurrentLimits(currentLimitsData, SIDE2))
            .filter(Objects::nonNull)
            .toList());

        // combine 2 sides in one list

        // simple case: one of the arrays are empty
        if (currentLimitsData2.isEmpty() && !currentLimitsData1.isEmpty()) {
            mergedLimitsData.addAll(currentLimitsData1);
            build.accept(mergedLimitsData);
            return;
        }
        if (currentLimitsData1.isEmpty() && !currentLimitsData2.isEmpty()) {
            mergedLimitsData.addAll(currentLimitsData2);
            build.accept(mergedLimitsData);
            return;
        }

        // more complex case
        for (CurrentLimitsData limitsData : currentLimitsData1) {
            Optional<CurrentLimitsData> l2 = currentLimitsData2.stream().filter(l -> l.getId().equals(limitsData.getId())).findFirst();
            if (l2.isPresent()) {
                CurrentLimitsData limitsData2 = l2.get();
                // both sides have limits and limits are equals
                if (limitsData.limitsEquals(limitsData2)) {
                    mergedLimitsData.add(copyCurrentLimitsData(limitsData, EQUIPMENT));
                    // both sides have limits and are different: create 2 different limit sets
                } else {
                    // Side 1
                    mergedLimitsData.add(limitsData);
                    // Side 2
                    mergedLimitsData.add(limitsData2);
                }
                // remove processed limits from side 2
                currentLimitsData2.remove(l2.get());
            } else {
                // only one side has limits
                mergedLimitsData.add(limitsData);
            }
        }

        // add remaining limits from side 2
        mergedLimitsData.addAll(currentLimitsData2);

        if (!mergedLimitsData.isEmpty()) {
            build.accept(mergedLimitsData);
        }
    }

    public static Optional<StandbyAutomatonInfos> toStandbyAutomaton(StaticVarCompensator staticVarCompensator) {
        StandbyAutomaton standbyAutomatonInfos = staticVarCompensator.getExtension(StandbyAutomaton.class);
        return standbyAutomatonInfos == null ? Optional.empty() :
                Optional.of(StandbyAutomatonInfos.builder()
                        .standby(standbyAutomatonInfos.isStandby())
                        .b0(nullIfNan(standbyAutomatonInfos.getB0()))
                        .lowVoltageSetpoint(nullIfNan(standbyAutomatonInfos.getLowVoltageSetpoint()))
                        .highVoltageSetpoint(nullIfNan(standbyAutomatonInfos.getHighVoltageSetpoint()))
                        .highVoltageThreshold(nullIfNan(standbyAutomatonInfos.getHighVoltageThreshold()))
                        .lowVoltageThreshold(nullIfNan(standbyAutomatonInfos.getLowVoltageThreshold())).build());
    }

    public static Optional<ActivePowerControlInfos> toActivePowerControl(Identifiable<?> identifiable) {
        var activePowerControl = identifiable.getExtension(ActivePowerControl.class);
        if (activePowerControl == null) {
            return Optional.empty();
        } else {
            return Optional.of(ActivePowerControlInfos.builder()
                        .participate(activePowerControl.isParticipate())
                        .droop(activePowerControl.getDroop())
                        .maxTargetP(activePowerControl.getMaxTargetP().isPresent() ? activePowerControl.getMaxTargetP().getAsDouble() : null)
                        .build());
        }
    }

    public static String toOperatingStatus(Extendable<?> extendable) {
        if (extendable instanceof Branch<?>
                || extendable instanceof ThreeWindingsTransformer
                || extendable instanceof HvdcLine
                || extendable instanceof BusbarSection) {
            var operatingStatus = extendable.getExtension(OperatingStatus.class);
            return operatingStatus == null ? null : operatingStatus.getStatus().name();
        }
        return null;
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

    public static CurrentLimitsData operationalLimitsGroupToMapDataCurrentLimits(OperationalLimitsGroup operationalLimitsGroup) {
        return operationalLimitsGroupToMapDataCurrentLimits(operationalLimitsGroup, null);
    }

    @Nullable
    public static CurrentLimitsData operationalLimitsGroupToMapDataCurrentLimits(OperationalLimitsGroup operationalLimitsGroup, Applicability applicability) {
        if (operationalLimitsGroup == null || operationalLimitsGroup.getCurrentLimits().isEmpty()) {
            return null;
        }
        CurrentLimitsData.CurrentLimitsDataBuilder builder = CurrentLimitsData.builder();
        boolean containsLimitsData = false;

        CurrentLimits currentLimits = operationalLimitsGroup.getCurrentLimits().get();
        builder.id(operationalLimitsGroup.getId());
        if (!Double.isNaN(currentLimits.getPermanentLimit())) {
            builder.permanentLimit(currentLimits.getPermanentLimit());
            containsLimitsData = true;
        }
        if (!CollectionUtils.isEmpty(currentLimits.getTemporaryLimits())) {
            builder.temporaryLimits(toMapDataTemporaryLimit(currentLimits.getTemporaryLimits()));
            containsLimitsData = true;
        }
        builder.applicability(applicability);

        return containsLimitsData ? builder.build() : null;
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
        if (terminal.getVoltageLevel().getTopologyKind().equals(TopologyKind.BUS_BREAKER)) {
            if (terminal.isConnected()) {
                return terminal.getBusBreakerView().getBus().getId();
            } else {
                return terminal.getBusBreakerView().getConnectableBus().getId();
            }
        } else {
            final BusbarSectionFinderTraverser connectedBusbarSectionFinder = new BusbarSectionFinderTraverser(terminal.isConnected());
            terminal.traverse(connectedBusbarSectionFinder, TraversalType.BREADTH_FIRST);
            return connectedBusbarSectionFinder.getFirstTraversedBbsId();
        }
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

    public static List<ReactiveCapabilityCurveMapData> getReactiveCapabilityCurvePointsMapData(Collection<ReactiveCapabilityCurve.Point> points) {
        return points.stream()
                .map(point -> ReactiveCapabilityCurveMapData.builder()
                        .p(point.getP())
                        .maxQ(point.getMaxQ())
                        .minQ(point.getMinQ())
                        .build())
                .toList();
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
