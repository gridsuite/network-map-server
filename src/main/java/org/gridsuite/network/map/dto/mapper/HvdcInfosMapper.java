/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.mapper;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.HvdcAngleDroopActivePowerControl;
import com.powsybl.iidm.network.extensions.HvdcOperatorActivePowerRange;
import org.gridsuite.network.map.dto.ElementInfos;
import org.gridsuite.network.map.dto.definition.hvdc.HvdcListInfos;
import org.gridsuite.network.map.dto.definition.hvdc.HvdcMapInfos;
import org.gridsuite.network.map.dto.definition.hvdc.HvdcShuntCompensatorsInfos;
import org.gridsuite.network.map.dto.definition.hvdc.HvdcTabInfos;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.gridsuite.network.map.dto.utils.ElementUtils.getBusOrBusbarSection;
import static org.gridsuite.network.map.dto.utils.ElementUtils.nullIfNan;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
public final class HvdcInfosMapper {
    private HvdcInfosMapper() {
    }

    public static ElementInfos toData(Identifiable<?> identifiable, ElementInfos.InfoType dataType) {
        switch (dataType) {
            case TAB:
                return toHvdcTabInfos(identifiable);
            case MAP:
                return toMapInfos(identifiable);
            case LIST:
                return toListInfos(identifiable);
            default:
                throw new UnsupportedOperationException("TODO");
        }
    }

    private static HvdcMapInfos toMapInfos(Identifiable<?> identifiable) {
        HvdcLine hvdcLine = (HvdcLine) identifiable;
        return HvdcMapInfos.builder()
                .id(hvdcLine.getId())
                .name(hvdcLine.getOptionalName().orElse(null))
                .voltageLevelId1(hvdcLine.getConverterStation1().getTerminal().getVoltageLevel().getId())
                .voltageLevelId2(hvdcLine.getConverterStation2().getTerminal().getVoltageLevel().getId())
                .terminal1Connected(hvdcLine.getConverterStation1().getTerminal().isConnected())
                .terminal2Connected(hvdcLine.getConverterStation2().getTerminal().isConnected())
                .p1(nullIfNan(hvdcLine.getConverterStation1().getTerminal().getP()))
                .p2(nullIfNan(hvdcLine.getConverterStation2().getTerminal().getP()))
                .hvdcType(hvdcLine.getConverterStation1().getHvdcType())
                .build();
    }

    private static HvdcListInfos toListInfos(Identifiable<?> identifiable) {
        HvdcLine hvdcLine = (HvdcLine) identifiable;
        Terminal terminal1 = hvdcLine.getConverterStation1().getTerminal();
        Terminal terminal2 = hvdcLine.getConverterStation2().getTerminal();

        return HvdcListInfos.builder()
                .id(hvdcLine.getId())
                .name(hvdcLine.getOptionalName().orElse(null))
                .voltageLevelId1(terminal1.getVoltageLevel().getId())
                .voltageLevelId2(terminal2.getVoltageLevel().getId())
                .substationId1(terminal1.getVoltageLevel().getSubstation().map(Substation::getId).orElse(null))
                .substationId2(terminal2.getVoltageLevel().getSubstation().map(Substation::getId).orElse(null))
                .build();
    }

    private static HvdcTabInfos toHvdcTabInfos(Identifiable<?> identifiable) {
        HvdcLine hvdcLine = (HvdcLine) identifiable;
        HvdcTabInfos.HvdcTabInfosBuilder<?, ?> builder = HvdcTabInfos.builder();
        builder
                .name(hvdcLine.getOptionalName().orElse(null))
                .id(hvdcLine.getId());

        HvdcAngleDroopActivePowerControl hvdcAngleDroopActivePowerControl = hvdcLine.getExtension(HvdcAngleDroopActivePowerControl.class);
        HvdcOperatorActivePowerRange hvdcOperatorActivePowerRange = hvdcLine.getExtension(HvdcOperatorActivePowerRange.class);
        builder
                .convertersMode(hvdcLine.getConvertersMode())
                .converterStationId1(hvdcLine.getConverterStation1().getId())
                .converterStationId2(hvdcLine.getConverterStation2().getId())
                .maxP(hvdcLine.getMaxP())
                .r(hvdcLine.getR())
                .activePowerSetpoint(hvdcLine.getActivePowerSetpoint());

        if (hvdcAngleDroopActivePowerControl != null) {
            builder.k(hvdcAngleDroopActivePowerControl.getDroop())
                    .isEnabled(hvdcAngleDroopActivePowerControl.isEnabled())
                    .p0(hvdcAngleDroopActivePowerControl.getP0());
        }

        if (hvdcOperatorActivePowerRange != null) {
            builder.oprFromCS1toCS2(hvdcOperatorActivePowerRange.getOprFromCS1toCS2())
                    .oprFromCS2toCS1(hvdcOperatorActivePowerRange.getOprFromCS2toCS1());
        }

        return builder.build();
    }

    private static List<HvdcShuntCompensatorsInfos.ShuntCompensatorInfos> toShuntCompensatorInfos(String lccBusOrBusbarSectionId, Stream<ShuntCompensator> shuntCompensators) {
        return shuntCompensators
                .map(s -> HvdcShuntCompensatorsInfos.ShuntCompensatorInfos.builder()
                        .id(s.getId())
                        .connectedToHvdc(Objects.equals(lccBusOrBusbarSectionId, getBusOrBusbarSection(s.getTerminal())))
                        .build())
                .collect(Collectors.toList());
    }

    public static HvdcShuntCompensatorsInfos toHvdcShuntCompensatorsInfos(HvdcLine hvdcLine) {
        HvdcConverterStation.HvdcType hvdcType = hvdcLine.getConverterStation1().getHvdcType();
        HvdcShuntCompensatorsInfos.HvdcShuntCompensatorsInfosBuilder<?, ?> builder = HvdcShuntCompensatorsInfos.builder()
                .id(hvdcLine.getId());
        if (hvdcType == HvdcConverterStation.HvdcType.LCC) {
            Terminal terminalLcc1 = hvdcLine.getConverterStation1().getTerminal();
            builder.mcsOnSide1(toShuntCompensatorInfos(getBusOrBusbarSection(terminalLcc1), terminalLcc1.getVoltageLevel().getShuntCompensatorStream()));
            Terminal terminalLcc2 = hvdcLine.getConverterStation2().getTerminal();
            builder.mcsOnSide2(toShuntCompensatorInfos(getBusOrBusbarSection(terminalLcc2), terminalLcc2.getVoltageLevel().getShuntCompensatorStream()));
        }
        return builder.build();
    }
}
