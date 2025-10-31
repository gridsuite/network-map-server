/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.mapper;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.ThreeWindingsTransformer.Leg;
import com.powsybl.iidm.network.extensions.DiscreteMeasurement.TapChanger;
import com.powsybl.iidm.network.extensions.DiscreteMeasurement.Type;
import com.powsybl.iidm.network.extensions.Measurement;
import org.gridsuite.network.map.dto.ElementInfos;
import org.gridsuite.network.map.dto.InfoTypeParameters;
import org.gridsuite.network.map.dto.common.TapChangerData;
import org.gridsuite.network.map.dto.definition.threewindingstransformer.ThreeWindingsTransformerOperatingStatusInfos;
import org.gridsuite.network.map.dto.definition.threewindingstransformer.ThreeWindingsTransformerTabInfos;
import org.gridsuite.network.map.dto.definition.threewindingstransformer.ThreeWindingsTransformerTabInfos.ThreeWindingsTransformerTabInfosBuilder;
import org.gridsuite.network.map.dto.utils.ElementUtils;
import org.gridsuite.network.map.dto.utils.ExtensionUtils;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;

import static org.gridsuite.network.map.dto.utils.ElementUtils.*;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
public final class ThreeWindingsTransformerInfosMapper {
    private ThreeWindingsTransformerInfosMapper() {
    }

    public static ElementInfos toData(Identifiable<?> identifiable, InfoTypeParameters infoTypeParameters) {
        return switch (infoTypeParameters.getInfoType()) {
            case LIST -> ElementInfosMapper.toListInfos(identifiable);
            case OPERATING_STATUS -> toOperatingStatusInfos(identifiable);
            case TAB -> toTabInfos(identifiable);
            default -> throw handleUnsupportedInfoType(infoTypeParameters.getInfoType(), "ThreeWindingsTransformer");
        };
    }

    private static ThreeWindingsTransformerOperatingStatusInfos toOperatingStatusInfos(Identifiable<?> identifiable) {
        ThreeWindingsTransformer threeWT = (ThreeWindingsTransformer) identifiable;
        Terminal terminal1 = threeWT.getLeg1().getTerminal();
        Terminal terminal2 = threeWT.getLeg2().getTerminal();
        Terminal terminal3 = threeWT.getLeg3().getTerminal();

        return ThreeWindingsTransformerOperatingStatusInfos.builder()
                .id(threeWT.getId())
                .name(threeWT.getOptionalName().orElse(null))
                .voltageLevelId1(terminal1.getVoltageLevel().getId())
                .voltageLevelId2(terminal2.getVoltageLevel().getId())
                .voltageLevelId3(terminal3.getVoltageLevel().getId())
                .operatingStatus(ExtensionUtils.toOperatingStatus(threeWT, List.of(terminal1.isConnected(), terminal2.isConnected(), terminal3.isConnected())))
                .build();
    }

    private static ThreeWindingsTransformerTabInfos toTabInfos(Identifiable<?> identifiable) {
        ThreeWindingsTransformer threeWT = (ThreeWindingsTransformer) identifiable;

        final Leg leg1 = threeWT.getLeg1();
        final Leg leg2 = threeWT.getLeg2();
        final Leg leg3 = threeWT.getLeg3();
        final Terminal terminal1 = leg1.getTerminal();
        final Terminal terminal2 = leg2.getTerminal();
        final Terminal terminal3 = leg3.getTerminal();

        ThreeWindingsTransformerTabInfosBuilder<?, ?> builder = ThreeWindingsTransformerTabInfos.builder()
                .name(threeWT.getOptionalName().orElse(null))
                .id(threeWT.getId())
                .properties(getProperties(threeWT))
                .country(mapCountry(findFirstSubstation(List.of(terminal1, terminal2, terminal3))))
                .measurementP1(ExtensionUtils.toMeasurement(threeWT, Measurement.Type.ACTIVE_POWER, 0))
                .measurementP2(ExtensionUtils.toMeasurement(threeWT, Measurement.Type.ACTIVE_POWER, 1))
                .measurementP3(ExtensionUtils.toMeasurement(threeWT, Measurement.Type.ACTIVE_POWER, 2))
                .measurementQ1(ExtensionUtils.toMeasurement(threeWT, Measurement.Type.REACTIVE_POWER, 0))
                .measurementQ2(ExtensionUtils.toMeasurement(threeWT, Measurement.Type.REACTIVE_POWER, 1))
                .measurementQ3(ExtensionUtils.toMeasurement(threeWT, Measurement.Type.REACTIVE_POWER, 2))
                .measurementRatioTap1(ExtensionUtils.toMeasurementTapChanger(threeWT, Type.TAP_POSITION, TapChanger.RATIO_TAP_CHANGER_1))
                .measurementRatioTap2(ExtensionUtils.toMeasurementTapChanger(threeWT, Type.TAP_POSITION, TapChanger.RATIO_TAP_CHANGER_2))
                .measurementRatioTap3(ExtensionUtils.toMeasurementTapChanger(threeWT, Type.TAP_POSITION, TapChanger.RATIO_TAP_CHANGER_3))
                .measurementPhaseTap1(ExtensionUtils.toMeasurementTapChanger(threeWT, Type.TAP_POSITION, TapChanger.PHASE_TAP_CHANGER_1))
                .measurementPhaseTap2(ExtensionUtils.toMeasurementTapChanger(threeWT, Type.TAP_POSITION, TapChanger.PHASE_TAP_CHANGER_2))
                .measurementPhaseTap3(ExtensionUtils.toMeasurementTapChanger(threeWT, Type.TAP_POSITION, TapChanger.PHASE_TAP_CHANGER_3));

        processTerminal(terminal1,
                builder::terminal1Connected, builder::voltageLevelId1, builder::nominalV1, builder::voltageLevelProperties1, builder::substationProperties1,
                builder::p1, builder::q1, builder::i1);
        processTerminal(terminal2,
                builder::terminal2Connected, builder::voltageLevelId2, builder::nominalV2, builder::voltageLevelProperties2, builder::substationProperties2,
                builder::p2, builder::q2, builder::i2);
        processTerminal(terminal3,
                builder::terminal3Connected, builder::voltageLevelId3, builder::nominalV3, builder::voltageLevelProperties3, builder::substationProperties3,
                builder::p3, builder::q3, builder::i3);

        processLeg(leg1,
                builder::ratioTapChanger1, builder::hasLoadTapChanging1Capabilities, builder::isRegulatingRatio1, builder::targetV1,
                builder::phaseTapChanger1, builder::regulationModeName1, builder::isRegulatingPhase1, builder::regulatingValue1,
                builder::permanentLimit1);
        processLeg(leg2,
                builder::ratioTapChanger2, builder::hasLoadTapChanging2Capabilities, builder::isRegulatingRatio2, builder::targetV2,
                builder::phaseTapChanger2, builder::regulationModeName2, builder::isRegulatingPhase2, builder::regulatingValue2,
                builder::permanentLimit2);
        processLeg(leg3,
                builder::ratioTapChanger3, builder::hasLoadTapChanging3Capabilities, builder::isRegulatingRatio3, builder::targetV3,
                builder::phaseTapChanger3, builder::regulationModeName3, builder::isRegulatingPhase3, builder::regulatingValue3,
                builder::permanentLimit3);

        return builder.build();
    }

    private static void processTerminal(@NonNull final Terminal terminal,
                                        @NonNull final Consumer<Boolean> setTerminalConnected,
                                        @NonNull final Consumer<String> setVoltageLevelId,
                                        @NonNull final DoubleConsumer setNominalV,
                                        @NonNull final Consumer<Map<String, String>> setVoltageLevelProperties,
                                        @NonNull final Consumer<Map<String, String>> setSubstationProperties,
                                        @NonNull final DoubleConsumer p, @NonNull final DoubleConsumer q, @NonNull final DoubleConsumer i) {
        setTerminalConnected.accept(terminal.isConnected());
        final VoltageLevel voltageLevel = terminal.getVoltageLevel();
        setVoltageLevelId.accept(voltageLevel.getId());
        setNominalV.accept(voltageLevel.getNominalV());
        setVoltageLevelProperties.accept(getProperties(voltageLevel));
        setSubstationProperties.accept(voltageLevel.getSubstation().map(ElementUtils::getProperties).orElse(null));
        setIfNotNan(p, terminal.getP());
        setIfNotNan(q, terminal.getQ());
        setIfNotNan(i, terminal.getI());
    }

    private static void processLeg(@NonNull final Leg leg,
                                   @NonNull final Consumer<TapChangerData> setRatioTapChanger,
                                   @NonNull final Consumer<Boolean> setHasLoadTapChangingCapabilities,
                                   @NonNull final Consumer<Boolean> isRegulatingRatio,
                                   @NonNull final DoubleConsumer setTargetV,
                                   @NonNull final Consumer<TapChangerData> setPhaseTapChanger,
                                   @NonNull final Consumer<String> setRegulationModeName,
                                   @NonNull final Consumer<Boolean> isRegulatingPhase,
                                   @NonNull final DoubleConsumer setRegulatingValue,
                                   @NonNull final DoubleConsumer setPermanentLimit) {
        if (leg.hasRatioTapChanger()) {
            final RatioTapChanger ratioTapChanger = leg.getRatioTapChanger();
            setRatioTapChanger.accept(toMapData(ratioTapChanger));
            setHasLoadTapChangingCapabilities.accept(ratioTapChanger.hasLoadTapChangingCapabilities());
            isRegulatingRatio.accept(ratioTapChanger.isRegulating());
            setIfNotNan(setTargetV, ratioTapChanger.getTargetV());
        }
        if (leg.hasPhaseTapChanger()) {
            final PhaseTapChanger phaseTapChanger = leg.getPhaseTapChanger();
            setPhaseTapChanger.accept(toMapData(phaseTapChanger));
            setRegulationModeName.accept(phaseTapChanger.getRegulationMode().name());
            isRegulatingPhase.accept(phaseTapChanger.isRegulating());
            setIfNotNan(setRegulatingValue, phaseTapChanger.getRegulationValue());
        }
        leg.getCurrentLimits().map(CurrentLimits::getPermanentLimit).ifPresent(value -> setIfNotNan(setPermanentLimit, value));
    }
}
