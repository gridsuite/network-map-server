/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.mapper;

import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.Identifiable;
import org.gridsuite.network.map.dto.ElementInfos;
import org.gridsuite.network.map.dto.InfoTypeParameters;
import org.gridsuite.network.map.dto.definition.hvdc.HvdcFormInfos;
import org.gridsuite.network.map.dto.definition.hvdc.hvdcvsc.HvdcVscFormInfos;

import static org.gridsuite.network.map.dto.utils.ElementUtils.*;

/**
 * @author Ghazwa Rehili <ghazwa.rehili at rte-france.com>
 */
public final class HvdcVscInfosMapper extends HvdcInfosMapper {
    private HvdcVscInfosMapper() {
    }

    public static ElementInfos toData(Identifiable<?> identifiable, InfoTypeParameters infoTypeParameters) {
        return switch (infoTypeParameters.getInfoType()) {
            case FORM -> toFormInfos(identifiable);
            case LIST -> ElementInfosMapper.toListInfos(identifiable);
            default -> throw new UnsupportedOperationException("TODO");
        };
    }

    private static HvdcFormInfos toFormInfos(Identifiable<?> identifiable) {
        HvdcLine hvdcLine = (HvdcLine) identifiable;
        HvdcFormInfos.HvdcFormInfosBuilder<?, ?> builder = HvdcVscFormInfos.builder()
                .id(hvdcLine.getId())
                .name(hvdcLine.getOptionalName().orElse(null))
                .nominalV(hvdcLine.getNominalV())
                .r(hvdcLine.getR())
                .maxP(hvdcLine.getMaxP())
                .activePowerSetpoint(hvdcLine.getActivePowerSetpoint())
                .convertersMode(hvdcLine.getConvertersMode())
                .properties(getProperties(hvdcLine))
                .converterStation1(VscConverterStationInfosMapper.toFormInfos(hvdcLine.getConverterStation1()))
                .converterStation2(VscConverterStationInfosMapper.toFormInfos(hvdcLine.getConverterStation2()))
                .hvdcAngleDroopActivePowerControl(toHvdcAngleDroopActivePowerControlIdentifiable(hvdcLine))
                .hvdcOperatorActivePowerRange(toHvdcOperatorActivePowerRange(hvdcLine))
                .operatingStatus(toOperatingStatus(hvdcLine));

        return builder.build();
    }
}
