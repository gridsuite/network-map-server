/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.mapper;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.DiscreteMeasurement;
import com.powsybl.iidm.network.extensions.Measurement;
import org.gridsuite.network.map.dto.ElementInfos;
import org.gridsuite.network.map.dto.InfoTypeParameters;
import org.gridsuite.network.map.dto.definition.twowindingstransformer.*;
import org.gridsuite.network.map.dto.utils.ElementUtils;

import java.util.List;

import static org.gridsuite.network.map.dto.InfoTypeParameters.QUERY_PARAM_DC_POWERFACTOR;
import static org.gridsuite.network.map.dto.utils.ElementUtils.*;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
public final class TwoWindingsTransformerInfosMapper {
    private TwoWindingsTransformerInfosMapper() {
    }

    public static ElementInfos toData(Identifiable<?> identifiable, InfoTypeParameters infoTypeParameters) {
        String dcPowerFactorStr = infoTypeParameters.getOptionalParameters().getOrDefault(QUERY_PARAM_DC_POWERFACTOR, null);
        Double dcPowerFactor = dcPowerFactorStr == null ? null : Double.valueOf(dcPowerFactorStr);
        return switch (infoTypeParameters.getInfoType()) {
            case LIST -> ElementInfosMapper.toListInfos(identifiable);
            case OPERATING_STATUS -> toOperatingStatusInfos(identifiable);
            case TOOLTIP -> toTooltipInfos(identifiable, dcPowerFactor);
            case TAB -> toTabInfos(identifiable, dcPowerFactor);
            case FORM -> toFormInfos(identifiable);
            default -> throw new UnsupportedOperationException("TODO");
        };
    }

    private static TwoWindingsTransformerFormInfos toFormInfos(Identifiable<?> identifiable) {
        TwoWindingsTransformer twoWT = (TwoWindingsTransformer) identifiable;
        Terminal terminal1 = twoWT.getTerminal1();
        Terminal terminal2 = twoWT.getTerminal2();

        TwoWindingsTransformerFormInfos.TwoWindingsTransformerFormInfosBuilder<?, ?> builder = TwoWindingsTransformerFormInfos.builder()
                .name(twoWT.getOptionalName().orElse(null))
                .id(twoWT.getId())
                .terminal1Connected(terminal1.isConnected())
                .terminal2Connected(terminal2.isConnected())
                .voltageLevelId1(terminal1.getVoltageLevel().getId())
                .voltageLevelName1(terminal1.getVoltageLevel().getOptionalName().orElse(null))
                .voltageLevelId2(terminal2.getVoltageLevel().getId())
                .voltageLevelName2(terminal2.getVoltageLevel().getOptionalName().orElse(null))
                .phaseTapChanger(toMapData(twoWT.getPhaseTapChanger()))
                .ratioTapChanger(toMapData(twoWT.getRatioTapChanger()))
                .r(twoWT.getR())
                .x(twoWT.getX())
                .b(twoWT.getB())
                .g(twoWT.getG())
                .ratedU1(twoWT.getRatedU1())
                .ratedU2(twoWT.getRatedU2())
                .selectedOperationalLimitsGroup1(twoWT.getSelectedOperationalLimitsGroupId1().orElse(null))
                .selectedOperationalLimitsGroup2(twoWT.getSelectedOperationalLimitsGroupId2().orElse(null))
                .properties(getProperties(twoWT));

        builder.busOrBusbarSectionId1(getBusOrBusbarSection(terminal1))
                .busOrBusbarSectionId2(getBusOrBusbarSection(terminal2));
        builder.ratedS(nullIfNan(twoWT.getRatedS()));
        builder.p1(nullIfNan(terminal1.getP()));
        builder.q1(nullIfNan(terminal1.getQ()));
        builder.p2(nullIfNan(terminal2.getP()));
        builder.q2(nullIfNan(terminal2.getQ()));
        builder.i1(nullIfNan(terminal1.getI()));
        builder.i2(nullIfNan(terminal2.getI()));

        buildCurrentLimits(twoWT.getOperationalLimitsGroups1(), builder::currentLimits1);
        buildCurrentLimits(twoWT.getOperationalLimitsGroups2(), builder::currentLimits2);

        builder.operatingStatus(toOperatingStatus(twoWT));
        builder.connectablePosition1(toMapConnectablePosition(twoWT, 1))
                .connectablePosition2(toMapConnectablePosition(twoWT, 2));

        builder.measurementP1(toMeasurement(twoWT, Measurement.Type.ACTIVE_POWER, 0))
                .measurementQ1(toMeasurement(twoWT, Measurement.Type.REACTIVE_POWER, 0))
                .measurementP2(toMeasurement(twoWT, Measurement.Type.ACTIVE_POWER, 1))
                .measurementQ2(toMeasurement(twoWT, Measurement.Type.REACTIVE_POWER, 1));

        return builder.build();
    }

    private static TwoWindingsTransformerTabInfos toTabInfos(Identifiable<?> identifiable, Double dcPowerFactor) {
        TwoWindingsTransformer twoWT = (TwoWindingsTransformer) identifiable;
        Terminal terminal1 = twoWT.getTerminal1();
        Terminal terminal2 = twoWT.getTerminal2();
        Substation firstSubstationFound = ElementUtils.findFirstSubstation(List.of(terminal1, terminal2));

        TwoWindingsTransformerTabInfos.TwoWindingsTransformerTabInfosBuilder<?, ?> builder = TwoWindingsTransformerTabInfos.builder()
                .name(twoWT.getOptionalName().orElse(null))
                .id(twoWT.getId())
                .terminal1Connected(terminal1.isConnected())
                .terminal2Connected(terminal2.isConnected())
                .voltageLevelId1(terminal1.getVoltageLevel().getId())
                .voltageLevelName1(terminal1.getVoltageLevel().getOptionalName().orElse(null))
                .nominalVoltage1(terminal1.getVoltageLevel().getNominalV())
                .properties(getProperties(twoWT))
                .voltageLevelId2(terminal2.getVoltageLevel().getId())
                .voltageLevelName2(terminal2.getVoltageLevel().getOptionalName().orElse(null))
                .nominalVoltage2(terminal2.getVoltageLevel().getNominalV())
                .country(mapCountry(firstSubstationFound))
                .phaseTapChanger(toMapData(twoWT.getPhaseTapChanger()))
                .ratioTapChanger(toMapData(twoWT.getRatioTapChanger()))
                .r(twoWT.getR())
                .x(twoWT.getX())
                .b(twoWT.getB())
                .g(twoWT.getG())
                .ratedU1(twoWT.getRatedU1())
                .ratedU2(twoWT.getRatedU2());
        builder.ratedS(nullIfNan(twoWT.getRatedS()));
        builder.p1(nullIfNan(terminal1.getP()));
        builder.q1(nullIfNan(terminal1.getQ()));
        builder.i1(nullIfNan(ElementUtils.computeIntensity(terminal1, dcPowerFactor)));
        builder.p2(nullIfNan(terminal2.getP()));
        builder.q2(nullIfNan(terminal2.getQ()));
        builder.i2(nullIfNan(ElementUtils.computeIntensity(terminal2, dcPowerFactor)));

        builder.operatingStatus(toOperatingStatus(twoWT));
        CurrentLimits limits1 = twoWT.getCurrentLimits1().orElse(null);
        CurrentLimits limits2 = twoWT.getCurrentLimits2().orElse(null);
        if (limits1 != null) {
            builder.currentLimits1(toMapDataCurrentLimits(limits1));
        }
        if (limits2 != null) {
            builder.currentLimits2(toMapDataCurrentLimits(limits2));
        }
        builder.connectablePosition1(toMapConnectablePosition(twoWT, 1))
                .connectablePosition2(toMapConnectablePosition(twoWT, 2));

        builder.measurementP1(toMeasurement(twoWT, Measurement.Type.ACTIVE_POWER, 0))
            .measurementQ1(toMeasurement(twoWT, Measurement.Type.REACTIVE_POWER, 0))
            .measurementP2(toMeasurement(twoWT, Measurement.Type.ACTIVE_POWER, 1))
            .measurementQ2(toMeasurement(twoWT, Measurement.Type.REACTIVE_POWER, 1));

        builder.measurementRatioTap(toMeasurementTapChanger(twoWT, DiscreteMeasurement.Type.TAP_POSITION, DiscreteMeasurement.TapChanger.RATIO_TAP_CHANGER))
            .measurementPhaseTap(toMeasurementTapChanger(twoWT, DiscreteMeasurement.Type.TAP_POSITION, DiscreteMeasurement.TapChanger.PHASE_TAP_CHANGER));

        builder.branchObservability(toBranchObservability(twoWT));

        return builder.build();
    }

    public static TwoWindingsTransformerOperatingStatusInfos toOperatingStatusInfos(Identifiable<?> identifiable) {
        TwoWindingsTransformer twoWT = (TwoWindingsTransformer) identifiable;
        Terminal terminal1 = twoWT.getTerminal1();
        Terminal terminal2 = twoWT.getTerminal2();

        return TwoWindingsTransformerOperatingStatusInfos.builder()
                .id(twoWT.getId())
                .name(twoWT.getOptionalName().orElse(null))
                .voltageLevelId1(terminal1.getVoltageLevel().getId())
                .voltageLevelId2(terminal2.getVoltageLevel().getId())
                .operatingStatus(toOperatingStatus(twoWT))
                .build();
    }

    private static TwoWindingsTransformerTooltipInfos toTooltipInfos(Identifiable<?> identifiable, Double dcPowerFactor) {
        TwoWindingsTransformer twoWindingsTransformer = (TwoWindingsTransformer) identifiable;
        Terminal terminal1 = twoWindingsTransformer.getTerminal1();
        Terminal terminal2 = twoWindingsTransformer.getTerminal2();

        TwoWindingsTransformerTooltipInfos.TwoWindingsTransformerTooltipInfosBuilder<?, ?> builder = TwoWindingsTransformerTooltipInfos.builder()
                .id(twoWindingsTransformer.getId())
                .name(twoWindingsTransformer.getOptionalName().orElse(null))
                .voltageLevelId1(terminal1.getVoltageLevel().getId())
                .voltageLevelId2(terminal2.getVoltageLevel().getId())
                .i1(nullIfNan(ElementUtils.computeIntensity(terminal1, dcPowerFactor)))
                .i2(nullIfNan(ElementUtils.computeIntensity(terminal2, dcPowerFactor)))
                .r(twoWindingsTransformer.getR())
                .x(twoWindingsTransformer.getX())
                .b(twoWindingsTransformer.getB());

        twoWindingsTransformer.getCurrentLimits1().ifPresent(limits1 -> builder.currentLimits1(toMapDataCurrentLimits(limits1)));
        twoWindingsTransformer.getCurrentLimits2().ifPresent(limits2 -> builder.currentLimits2(toMapDataCurrentLimits(limits2)));

        return builder.build();
    }

}
