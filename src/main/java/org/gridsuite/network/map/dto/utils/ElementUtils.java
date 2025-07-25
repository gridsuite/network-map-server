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
import com.powsybl.iidm.network.extensions.ConnectablePosition.Feeder;
import com.powsybl.math.graph.TraversalType;
import org.gridsuite.network.map.dto.common.*;
import org.gridsuite.network.map.dto.definition.extension.*;
import org.springframework.lang.NonNull;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Consumer;
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

    public static void setIfNotNan(@NonNull final DoubleConsumer setter, final double value) {
        if (!Double.isNaN(value)) {
            setter.accept(value);
        }
    }

    private static Feeder getFeederInfos(Identifiable<?> identifiable, int index) {
        var connectablePosition = identifiable.getExtension(ConnectablePosition.class);
        if (connectablePosition == null) {
            return null;
        }

        return switch (index) {
            case 0 -> connectablePosition.getFeeder();
            case 1 -> connectablePosition.getFeeder1();
            case 2 -> connectablePosition.getFeeder2();
            default -> throw new IllegalArgumentException("Invalid feeder index: " + index);
        };
    }

    public static ConnectablePositionInfos toMapConnectablePosition(Identifiable<?> branch, int index) {
        ConnectablePositionInfos.ConnectablePositionInfosBuilder builder = ConnectablePositionInfos.builder();
        Feeder feeder = getFeederInfos(branch, index);
        if (feeder != null) {
            builder.connectionDirection(feeder.getDirection() == null ? null : feeder.getDirection())
                    .connectionPosition(feeder.getOrder().orElse(null))
                    .connectionName(feeder.getName().orElse(null));
        }
        return builder.build();
    }

    public static void buildCurrentLimits(Collection<OperationalLimitsGroup> currentLimits, Consumer<List<CurrentLimitsData>> build) {
        List<CurrentLimitsData> currentLimitsData = currentLimits.stream()
                .map(
                        ElementUtils::operationalLimitsGroupToMapDataCurrentLimits)
                .toList();
        if (!currentLimitsData.isEmpty()) {
            build.accept(currentLimitsData);
        }
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
