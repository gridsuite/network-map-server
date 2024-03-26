/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.mapper;

import com.powsybl.iidm.network.*;
import org.gridsuite.network.map.dto.ElementInfos;
import org.gridsuite.network.map.dto.definition.tieline.TieLineMapInfos;
import org.gridsuite.network.map.dto.utils.ElementUtils;

import java.util.Objects;

import static org.gridsuite.network.map.dto.utils.ElementUtils.*;

/**
 * @author Rehili Ghazwa <ghazwa.rehili at rte-france.com>
 */
public final class TieLineInfosMapper {
    private TieLineInfosMapper() {
    }

    public static ElementInfos toData(Identifiable<?> identifiable, ElementInfos.ElementInfoType dataType) {
        if (Objects.requireNonNull(dataType.getInfoType()) == ElementInfos.InfoType.MAP) {
            return toMapInfos(identifiable, dataType.getDcPowerFactor());
        }
        throw new UnsupportedOperationException("TODO");
    }

    private static TieLineMapInfos toMapInfos(Identifiable<?> identifiable, Double dcPowerFactor) {
        TieLine tieLine = (TieLine) identifiable;
        Terminal terminal1 = tieLine.getTerminal1();
        Terminal terminal2 = tieLine.getTerminal2();

        TieLineMapInfos.TieLineMapInfosBuilder<?, ?> builder = TieLineMapInfos.builder()
                .id(tieLine.getId())
                .name(tieLine.getOptionalName().orElse(null))
                .terminal1Connected(terminal1.isConnected())
                .terminal2Connected(terminal2.isConnected())
                .voltageLevelId1(terminal1.getVoltageLevel().getId())
                .voltageLevelName1(terminal1.getVoltageLevel().getOptionalName().orElse(null))
                .voltageLevelId2(terminal2.getVoltageLevel().getId())
                .voltageLevelName2(terminal2.getVoltageLevel().getOptionalName().orElse(null))
                .p1(nullIfNan(terminal1.getP()))
                .p2(nullIfNan(terminal2.getP()))
                .i1(nullIfNan(ElementUtils.computeIntensity(terminal1, dcPowerFactor)))
                .i2(nullIfNan(ElementUtils.computeIntensity(terminal2, dcPowerFactor)))
                .operatingStatus(toOperatingStatus(tieLine));

        tieLine.getCurrentLimits1().ifPresent(limits1 -> builder.currentLimits1(toMapDataCurrentLimits(limits1)));
        tieLine.getCurrentLimits2().ifPresent(limits2 -> builder.currentLimits2(toMapDataCurrentLimits(limits2)));

        return builder.build();
    }
}
