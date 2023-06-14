/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.utils;

import com.powsybl.iidm.network.*;
import org.gridsuite.network.map.dto.threewindingstransformer.ThreeWindingsTransformerTabInfos;
import org.gridsuite.network.map.model.CurrentLimitsData;
import org.gridsuite.network.map.model.TapChangerData;
import org.gridsuite.network.map.model.TapChangerStepData;
import org.gridsuite.network.map.model.TemporaryLimitData;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
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
        terminal.traverse(connectedBusbarSectionFinder);
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

    public static TapChangerData toMapData(RatioTapChanger tapChanger) {
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
    }
}