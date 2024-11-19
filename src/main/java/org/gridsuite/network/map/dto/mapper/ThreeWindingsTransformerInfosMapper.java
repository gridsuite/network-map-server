/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.mapper;

import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.extensions.DiscreteMeasurement;
import com.powsybl.iidm.network.extensions.Measurement;
import org.gridsuite.network.map.dto.ElementInfos;
import org.gridsuite.network.map.dto.InfoTypeParameters;
import org.gridsuite.network.map.dto.definition.threewindingstransformer.ThreeWindingsTransformerOperatingStatusInfos;
import org.gridsuite.network.map.dto.definition.threewindingstransformer.ThreeWindingsTransformerTabInfos;
import org.gridsuite.network.map.dto.utils.ElementUtils;

import java.util.List;

import static org.gridsuite.network.map.dto.utils.ElementUtils.*;
import static org.gridsuite.network.map.dto.utils.ElementUtils.mapCountry;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
public final class ThreeWindingsTransformerInfosMapper {
    private ThreeWindingsTransformerInfosMapper() {
    }

    public static ElementInfos toData(Identifiable<?> identifiable, InfoTypeParameters infoTypeParameters) {
        switch (infoTypeParameters.getInfoType()) {
            case LIST:
                return ElementInfosMapper.toListInfos(identifiable);
            case OPERATING_STATUS:
                return toOperatingStatusInfos(identifiable);
            case TAB:
                return toTabInfos(identifiable);
            default:
                throw new UnsupportedOperationException("TODO");
        }
    }

    public static ThreeWindingsTransformerOperatingStatusInfos toOperatingStatusInfos(Identifiable<?> identifiable) {
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
                .operatingStatus(toOperatingStatus(threeWT))
                .build();
    }

    private static ThreeWindingsTransformerTabInfos toTabInfos(Identifiable<?> identifiable) {
        ThreeWindingsTransformer threeWT = (ThreeWindingsTransformer) identifiable;

        Terminal terminal1 = threeWT.getLeg1().getTerminal();
        Terminal terminal2 = threeWT.getLeg2().getTerminal();
        Terminal terminal3 = threeWT.getLeg3().getTerminal();
        Substation firstSubstationFound = ElementUtils.findFirstSubstation(List.of(terminal1, terminal2, terminal3));

        ThreeWindingsTransformerTabInfos.ThreeWindingsTransformerTabInfosBuilder<?, ?> builder = ThreeWindingsTransformerTabInfos.builder()
                .name(threeWT.getOptionalName().orElse(null))
                .id(threeWT.getId())
                .properties(getProperties(threeWT))
                .terminal1Connected(terminal1.isConnected())
                .terminal2Connected(terminal2.isConnected())
                .terminal3Connected(terminal3.isConnected())
                .voltageLevelId1(terminal1.getVoltageLevel().getId())
                .voltageLevelId2(terminal2.getVoltageLevel().getId())
                .voltageLevelId3(terminal3.getVoltageLevel().getId())
                .nominalV1(terminal1.getVoltageLevel().getNominalV())
                .nominalV2(terminal2.getVoltageLevel().getNominalV())
                .nominalV3(terminal3.getVoltageLevel().getNominalV())
                .country(mapCountry(firstSubstationFound));

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
        mapThreeWindingsTransformerRatioTapChangers(builder, threeWT);
        mapThreeWindingsTransformerPermanentLimits(builder, threeWT);

        builder.measurementP1(toMeasurement(threeWT, Measurement.Type.ACTIVE_POWER, 0))
            .measurementQ1(toMeasurement(threeWT, Measurement.Type.REACTIVE_POWER, 0))
            .measurementP2(toMeasurement(threeWT, Measurement.Type.ACTIVE_POWER, 1))
            .measurementQ2(toMeasurement(threeWT, Measurement.Type.REACTIVE_POWER, 1))
            .measurementP3(toMeasurement(threeWT, Measurement.Type.ACTIVE_POWER, 2))
            .measurementQ3(toMeasurement(threeWT, Measurement.Type.REACTIVE_POWER, 2));

        builder.measurementRatioTap1(toMeasurementTapChanger(threeWT, DiscreteMeasurement.Type.TAP_POSITION, DiscreteMeasurement.TapChanger.RATIO_TAP_CHANGER_1))
            .measurementPhaseTap1(toMeasurementTapChanger(threeWT, DiscreteMeasurement.Type.TAP_POSITION, DiscreteMeasurement.TapChanger.PHASE_TAP_CHANGER_1))
            .measurementRatioTap2(toMeasurementTapChanger(threeWT, DiscreteMeasurement.Type.TAP_POSITION, DiscreteMeasurement.TapChanger.RATIO_TAP_CHANGER_2))
            .measurementPhaseTap2(toMeasurementTapChanger(threeWT, DiscreteMeasurement.Type.TAP_POSITION, DiscreteMeasurement.TapChanger.PHASE_TAP_CHANGER_2))
            .measurementRatioTap3(toMeasurementTapChanger(threeWT, DiscreteMeasurement.Type.TAP_POSITION, DiscreteMeasurement.TapChanger.RATIO_TAP_CHANGER_3))
            .measurementPhaseTap3(toMeasurementTapChanger(threeWT, DiscreteMeasurement.Type.TAP_POSITION, DiscreteMeasurement.TapChanger.PHASE_TAP_CHANGER_3));

        return builder.build();
    }
}
