/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.utils;

import com.powsybl.commons.extensions.Extendable;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.*;
import com.powsybl.math.graph.TraversalType;
import org.apache.commons.lang3.tuple.Pair;
import org.gridsuite.network.map.dto.common.*;
import org.gridsuite.network.map.dto.definition.extension.*;
import org.gridsuite.network.map.dto.definition.threewindingstransformer.ThreeWindingsTransformerTabInfos;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.gridsuite.network.map.dto.common.CurrentLimitsData.Applicability.*;

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

    public static void buildCurrentLimits(Collection<OperationalLimitsGroup> currentLimits, String oldSelected, String newSelected, Consumer<List<CurrentLimitsData>> build) {

        ArrayList<CurrentLimitsData> currentLimitsData = new ArrayList<>(currentLimits.stream()
                .map(ElementUtils::operationalLimitsGroupToMapDataCurrentLimits)
                .toList());

        // if selected operationalLimitsGroups renamed, rename it also here
        if (!newSelected.isEmpty()) {
            Optional<CurrentLimitsData> limitsGroup = currentLimitsData.stream().filter(l -> l.getId().equals(oldSelected)).findFirst();
            if (limitsGroup.isPresent()) {
                CurrentLimitsData current = copyCurrentLimitsData(limitsGroup.get(), newSelected);
                currentLimitsData.remove(limitsGroup.get());
                currentLimitsData.add(current);
            }
        }

        if (!currentLimitsData.isEmpty()) {
            build.accept(currentLimitsData);
        }
    }

    private static String generateSetName(String basicName, String suffix, List<CurrentLimitsData> mergedList, List<CurrentLimitsData> otherList) {

        boolean nameUsed;
        String strIncrement = "";
        int increment = 1;

        do {
            String currentId = basicName + suffix + strIncrement;
            if (!mergedList.stream().filter(l -> l.getId().equals(currentId)).toList().isEmpty()
                || !otherList.stream().filter(l -> l.getId().equals(currentId)).toList().isEmpty()) {
                nameUsed = true;
                increment++;
                strIncrement = "(" + increment + ")";
            } else {
                nameUsed = false;
            }
        } while (nameUsed);

        return basicName + suffix + strIncrement;
    }

    private static CurrentLimitsData copyCurrentLimitsData(CurrentLimitsData currentLimitsData, CurrentLimitsData.Applicability applicability, String id) {
        return CurrentLimitsData.builder()
            .id(id.isEmpty() ? currentLimitsData.getId() : id)
            .applicability(applicability)
            .temporaryLimits(currentLimitsData.getTemporaryLimits())
            .permanentLimit(currentLimitsData.getPermanentLimit()).build();
    }

    private static CurrentLimitsData copyCurrentLimitsData(CurrentLimitsData currentLimitsData, CurrentLimitsData.Applicability applicability) {
        return copyCurrentLimitsData(currentLimitsData, applicability, "");
    }

    private static CurrentLimitsData copyCurrentLimitsData(CurrentLimitsData currentLimitsData, String id) {
        return CurrentLimitsData.builder()
            .id(id.isEmpty() ? currentLimitsData.getId() : id)
            .temporaryLimits(currentLimitsData.getTemporaryLimits())
            .permanentLimit(currentLimitsData.getPermanentLimit()).build();
    }

    /**
     * @return id of the selected operation limits group 1 and 2 if they have been renamed
     */
    public static Pair<String, String> mergeCurrentLimits(Collection<OperationalLimitsGroup> operationalLimitsGroups1,
                                                          Collection<OperationalLimitsGroup> operationalLimitsGroups2,
                                                          String selectedLimitsGroup1, String selectedLimitsGroup2,
                                                          Consumer<List<CurrentLimitsData>> build) {
        final String orSuffix = "_OR";
        final String exSuffix = "_EX";
        String changedSelectedLimitsGroup1 = "";
        String changedSelectedLimitsGroup2 = "";
        List<CurrentLimitsData> mergedLimitsData = new ArrayList<>();

        // Build temporary limit from side 1 and 2
        List<CurrentLimitsData> currentLimitsData1 = operationalLimitsGroups1.stream()
            .map(ElementUtils::operationalLimitsGroupToMapDataCurrentLimits).toList();
        ArrayList<CurrentLimitsData> currentLimitsData2 = new ArrayList<>(operationalLimitsGroups2.stream()
            .map(ElementUtils::operationalLimitsGroupToMapDataCurrentLimits).toList());

        // combine 2 sides in one list

        // simple case : one of the arrays are empty
        if (currentLimitsData2.isEmpty() && !currentLimitsData1.isEmpty()) {
            for (CurrentLimitsData currentLimitsData : currentLimitsData1) {
                mergedLimitsData.add(copyCurrentLimitsData(currentLimitsData, SIDE1));
            }
            build.accept(mergedLimitsData);
            return Pair.of("", "");
        } else if (currentLimitsData1.isEmpty() && !currentLimitsData2.isEmpty()) {
            for (CurrentLimitsData currentLimitsData : currentLimitsData2) {
                mergedLimitsData.add(copyCurrentLimitsData(currentLimitsData, SIDE2));
            }
            build.accept(mergedLimitsData);
            return Pair.of("", "");
        }

        // more complex case
        for (CurrentLimitsData limitsData : currentLimitsData1) {
            Optional<CurrentLimitsData> l2 = currentLimitsData2.stream().filter(l -> l.getId().equals(limitsData.getId())).findFirst();

            if (l2.isPresent()) {
                CurrentLimitsData limitsData2 = l2.get();
                // Only side one has limits
                if (limitsData.hasLimits() && !limitsData2.hasLimits()) {
                    mergedLimitsData.add(copyCurrentLimitsData(limitsData, SIDE1));
                    // only side two has limits
                } else if (limitsData2.hasLimits() && !limitsData.hasLimits()) {
                    mergedLimitsData.add(copyCurrentLimitsData(limitsData2, SIDE2));
                } else {
                    // both sides have limits and limits are equals
                    if (limitsData.limitsEquals(limitsData2)) {
                        mergedLimitsData.add(copyCurrentLimitsData(limitsData, EQUIPMENT));
                        // both side have limits and they are differents : create 2 differents limitset with basename_Or and _Ex
                    } else {
                        String currentLimitId = limitsData.getId();
                        // Side 1
                        String limitId = generateSetName(currentLimitId, orSuffix, mergedLimitsData, currentLimitsData2);
                        mergedLimitsData.add(copyCurrentLimitsData(limitsData, SIDE1, limitId));
                        // if name changed and is active limit set change also selected limit set
                        if (selectedLimitsGroup1.equals(currentLimitId)) {
                            changedSelectedLimitsGroup1 = limitId;
                        }
                        // Side 2
                        limitId = generateSetName(currentLimitId, exSuffix, mergedLimitsData, currentLimitsData2);
                        mergedLimitsData.add(copyCurrentLimitsData(limitsData2, SIDE2, limitId));
                        // if name changed and is active limit set change also selected limit set
                        if (selectedLimitsGroup2.equals(currentLimitId)) {
                            changedSelectedLimitsGroup2 = limitId;
                        }
                    }
                }
                // remove processed limits from side 2
                currentLimitsData2.remove(l2.get());
            } else {
                mergedLimitsData.add(copyCurrentLimitsData(limitsData, SIDE1));
            }
        }

        // add remaining limits from side 2
        for (CurrentLimitsData limitsData : currentLimitsData2) {
            mergedLimitsData.add(copyCurrentLimitsData(limitsData, SIDE2));
        }

        if (!mergedLimitsData.isEmpty()) {
            build.accept(mergedLimitsData);
        }

        return Pair.of(changedSelectedLimitsGroup1, changedSelectedLimitsGroup2);
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
            ThreeWindingsTransformerTabInfos.ThreeWindingsTransformerTabInfosBuilder<?, ?> builder,
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
            ThreeWindingsTransformerTabInfos.ThreeWindingsTransformerTabInfosBuilder<?, ?> builder,
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

    public static Optional<MeasurementsInfos> toMeasurement(Connectable<?> connectable, Measurement.Type type, int index) {
        var measurements = connectable.getExtension(Measurements.class);
        if (measurements == null) {
            return Optional.empty();
        } else {
            return measurements.getMeasurements(type).stream().filter(m -> ((Measurement) m).getSide() == null || ((Measurement) m).getSide().getNum() - 1 == index).findFirst()
                .map(m -> MeasurementsInfos.builder().value(((Measurement) m).getValue()).validity(((Measurement) m).isValid()).build());
        }
    }

    public static Optional<TapChangerDiscreteMeasurementsInfos> toMeasurementTapChanger(Connectable<?> connectable, DiscreteMeasurement.Type type, DiscreteMeasurement.TapChanger tapChanger) {
        var measurements = connectable.getExtension(DiscreteMeasurements.class);
        if (measurements == null) {
            return Optional.empty();
        } else {
            Optional<DiscreteMeasurement> measurement = measurements.getDiscreteMeasurements(type).stream().filter(m -> ((DiscreteMeasurement) m).getTapChanger() == tapChanger).findFirst();
            return measurement.map(m -> TapChangerDiscreteMeasurementsInfos.builder().value(m.getValueAsInt()).validity(m.isValid()).build());
        }
    }

    public static Optional<InjectionObservabilityInfos> toInjectionObservability(Injection<?> injection) {
        var observability = injection.getExtension(InjectionObservability.class);
        if (observability == null) {
            return Optional.empty();
        }

        return Optional.of(InjectionObservabilityInfos.builder()
                .qualityQ(buildQualityInfos(observability.getQualityQ()))
                .qualityP(buildQualityInfos(observability.getQualityP()))
                .qualityV(buildQualityInfos(observability.getQualityV()))
                .isObservable(observability.isObservable())
                .build());
    }

    public static Optional<BranchObservabilityInfos> toBranchObservability(Branch<?> branch) {
        var observability = branch.getExtension(BranchObservability.class);
        if (observability == null) {
            return Optional.empty();
        }

        return Optional.of(BranchObservabilityInfos.builder()
                .qualityP1(buildQualityInfos(observability.getQualityP1()))
                .qualityP2(buildQualityInfos(observability.getQualityP2()))
                .qualityQ1(buildQualityInfos(observability.getQualityQ1()))
                .qualityQ2(buildQualityInfos(observability.getQualityQ2()))
                .isObservable(observability.isObservable())
                .build());
    }

    private static ObservabilityQualityInfos buildQualityInfos(ObservabilityQuality<?> quality) {
        if (quality == null) {
            return null;
        }
        return ObservabilityQualityInfos.builder()
                .standardDeviation(quality.getStandardDeviation())
                .isRedundant(quality.isRedundant().orElse(null))
                .build();
    }

    public static Map<String, CurrentLimitsData> buildCurrentLimitsMap(Collection<OperationalLimitsGroup> operationalLimitsGroups) {
        Map<String, CurrentLimitsData> res = new HashMap<>();
        if (!CollectionUtils.isEmpty(operationalLimitsGroups)) {
            operationalLimitsGroups.forEach(operationalLimitsGroup -> operationalLimitsGroup.getCurrentLimits().ifPresent(limits -> {
                CurrentLimitsData limitsData = toMapDataCurrentLimits(limits);
                res.put(operationalLimitsGroup.getId(), limitsData);
            }));
        }
        return res;
    }
}
