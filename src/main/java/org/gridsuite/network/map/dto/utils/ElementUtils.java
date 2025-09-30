/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.utils;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import org.gridsuite.network.map.dto.common.ReactiveCapabilityCurveMapData;
import org.gridsuite.network.map.dto.common.TapChangerData;
import org.gridsuite.network.map.dto.common.TapChangerStepData;
import org.gridsuite.network.map.dto.definition.extension.BusbarSectionFinderTraverser;
import org.gridsuite.network.map.dto.definition.extension.ConnectablePositionInfos;
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

    public static void setIfNotNan(@NonNull final DoubleConsumer setter, final double value) {
        if (!Double.isNaN(value)) {
            setter.accept(value);
        }
    }

    public enum FeederSide {
        INJECTION_SINGLE_SIDE,
        BRANCH_SIDE_ONE,
        BRANCH_SIDE_TWO;

        public static FeederSide from(Optional<ThreeSides> connectableSide) {
            return connectableSide.map(threeSides -> threeSides == ThreeSides.ONE ? BRANCH_SIDE_ONE : BRANCH_SIDE_TWO).orElse(INJECTION_SINGLE_SIDE);
        }
    }

    private static ConnectablePosition.Feeder getFeederInfos(Identifiable<?> identifiable, FeederSide side) {
        ConnectablePosition<?> connectablePosition = (ConnectablePosition<?>) identifiable.getExtension(ConnectablePosition.class);
        if (connectablePosition == null) {
            return null;
        }

        switch (side) {
            case INJECTION_SINGLE_SIDE:
                return connectablePosition.getFeeder();
            case BRANCH_SIDE_ONE:
                return connectablePosition.getFeeder1();
            case BRANCH_SIDE_TWO:
                return connectablePosition.getFeeder2();
            default:
                throw new IllegalArgumentException("Invalid feeder side: " + side);
        }
    }

    public static ConnectablePositionInfos getConnectablePosition(Identifiable<?> identifiable, FeederSide side) {
        ConnectablePosition.Feeder feeder = getFeederInfos(identifiable, side);
        return buildConnectablePositionInfos(feeder);
    }

    public static ConnectablePositionInfos buildConnectablePositionInfos(ConnectablePosition.Feeder feeder) {
        ConnectablePositionInfos.ConnectablePositionInfosBuilder builder = ConnectablePositionInfos.builder();
        if (feeder != null) {
            builder.connectionDirection(feeder.getDirection() == null ? null : feeder.getDirection())
                    .connectionPosition(feeder.getOrder().orElse(null))
                    .connectionName(feeder.getName().orElse(null));
        }
        return builder.build();
    }

    public static String getBusOrBusbarSection(Terminal terminal) {
        if (terminal.getVoltageLevel().getTopologyKind().equals(TopologyKind.BUS_BREAKER)) {
            if (terminal.isConnected()) {
                return terminal.getBusBreakerView().getBus().getId();
            } else {
                return terminal.getBusBreakerView().getConnectableBus().getId();
            }
        } else {
            // NODE_BREAKER: explore all paths and choose the busbar with the closed disconnector
            return BusbarSectionFinderTraverser.findBusbarId(terminal);
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
