/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.utils;

import com.powsybl.iidm.network.*;
import com.powsybl.math.graph.TraversalType;
import org.gridsuite.network.map.dto.common.*;
import org.gridsuite.network.map.dto.definition.extension.BusbarSectionFinderTraverser;
import org.springframework.lang.NonNull;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
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

    public static void setIfNotNan(@NonNull final DoubleConsumer setter, final double value) {
        if (!Double.isNaN(value)) {
            setter.accept(value);
        }
    }

    public static void buildCurrentLimits(Collection<OperationalLimitsGroup> currentLimits, Consumer<List<CurrentLimitsData>> build) {
        List<CurrentLimitsData> currentLimitsData = currentLimits.stream()
                .map(ElementUtils::operationalLimitsGroupToMapDataCurrentLimits)
                .toList();
        if (!currentLimitsData.isEmpty()) {
            build.accept(currentLimitsData);
        }
    }

    private static CurrentLimitsData copyCurrentLimitsData(CurrentLimitsData currentLimitsData, CurrentLimitsData.Applicability applicability) {
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
            return;
        }
        if (currentLimitsData1.isEmpty() && !currentLimitsData2.isEmpty()) {
            for (CurrentLimitsData currentLimitsData : currentLimitsData2) {
                mergedLimitsData.add(copyCurrentLimitsData(currentLimitsData, SIDE2));
            }
            build.accept(mergedLimitsData);
            return;
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
                        // both side have limits and they are different : create 2 different limit sets
                    } else {
                        // Side 1
                        mergedLimitsData.add(copyCurrentLimitsData(limitsData, SIDE1));
                        // Side 2
                        mergedLimitsData.add(copyCurrentLimitsData(limitsData2, SIDE2));
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

    private static CurrentLimitsData operationalLimitsGroupToMapDataCurrentLimits(OperationalLimitsGroup operationalLimitsGroup) {
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
