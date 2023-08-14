/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.mapper.threewindingstransformer;

import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.gridsuite.network.map.dto.ElementInfos;
import org.gridsuite.network.map.dto.definition.threewindingstransformer.ThreeWindingsTransformerListInfos;
import org.gridsuite.network.map.dto.definition.threewindingstransformer.ThreeWindingsTransformerTabInfos;

import static org.gridsuite.network.map.dto.utils.ElementUtils.mapThreeWindingsTransformerPermanentLimits;
import static org.gridsuite.network.map.dto.utils.ElementUtils.mapThreeWindingsTransformerRatioTapChangers;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
@SuperBuilder
@Getter
public abstract class AbstractThreeWindingsTransformerInfos extends ElementInfos {
    public static ElementInfos toData(Identifiable<?> identifiable, InfoType dataType) {
        switch (dataType) {
            case LIST:
                return toThreeWindingsTransformerListInfos(identifiable);
            case TAB:
                return toThreeWindingsTransformerTabInfos(identifiable);
            default:
                throw new UnsupportedOperationException("TODO");
        }
    }

    public static ThreeWindingsTransformerListInfos toThreeWindingsTransformerListInfos(Identifiable<?> identifiable) {
        ThreeWindingsTransformer threeWT = (ThreeWindingsTransformer) identifiable;
        Terminal terminal1 = threeWT.getLeg1().getTerminal();
        Terminal terminal2 = threeWT.getLeg2().getTerminal();
        Terminal terminal3 = threeWT.getLeg3().getTerminal();

        return ThreeWindingsTransformerListInfos.builder()
                .id(threeWT.getId())
                .name(threeWT.getOptionalName().orElse(null))
                .voltageLevelId1(terminal1.getVoltageLevel().getId())
                .voltageLevelId2(terminal2.getVoltageLevel().getId())
                .voltageLevelId3(terminal3.getVoltageLevel().getId())
                .substationId1(terminal1.getVoltageLevel().getSubstation().map(Substation::getId).orElse(null))
                .substationId2(terminal2.getVoltageLevel().getSubstation().map(Substation::getId).orElse(null))
                .substationId3(terminal3.getVoltageLevel().getSubstation().map(Substation::getId).orElse(null))
                .build();
    }

    public static ThreeWindingsTransformerTabInfos toThreeWindingsTransformerTabInfos(Identifiable<?> identifiable) {
        ThreeWindingsTransformer threeWT = (ThreeWindingsTransformer) identifiable;

        Terminal terminal1 = threeWT.getLeg1().getTerminal();
        Terminal terminal2 = threeWT.getLeg2().getTerminal();
        Terminal terminal3 = threeWT.getLeg3().getTerminal();
        ThreeWindingsTransformerTabInfos.ThreeWindingsTransformerTabInfosBuilder builder = ThreeWindingsTransformerTabInfos.builder()
                .name(threeWT.getOptionalName().orElse(null))
                .id(threeWT.getId())
                .terminal1Connected(terminal1.isConnected())
                .terminal2Connected(terminal2.isConnected())
                .terminal3Connected(terminal3.isConnected())
                .voltageLevelId1(terminal1.getVoltageLevel().getId())
                .voltageLevelId2(terminal2.getVoltageLevel().getId())
                .voltageLevelId3(terminal3.getVoltageLevel().getId())
                .nominalVoltage1(terminal1.getVoltageLevel().getNominalV())
                .nominalVoltage2(terminal2.getVoltageLevel().getNominalV())
                .nominalVoltage3(terminal3.getVoltageLevel().getNominalV());

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
        return builder.build();
    }
}
