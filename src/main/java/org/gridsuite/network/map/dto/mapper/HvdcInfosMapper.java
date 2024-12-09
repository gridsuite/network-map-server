/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.mapper;

import com.powsybl.iidm.network.*;
import org.gridsuite.network.map.dto.ElementInfos;
import org.gridsuite.network.map.dto.InfoTypeParameters;
import org.gridsuite.network.map.dto.definition.hvdc.*;
import org.gridsuite.network.map.dto.definition.lccconverterstation.LccConverterStationFormInfos;
import org.gridsuite.network.map.dto.definition.vscconverterstation.VscConverterStationFormInfos;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static org.gridsuite.network.map.dto.utils.ElementUtils.*;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
public final class HvdcInfosMapper {
    private HvdcInfosMapper() {
    }

    public static ElementInfos toData(Identifiable<?> identifiable, InfoTypeParameters infoTypeParameters) {
        return switch (infoTypeParameters.getInfoType()) {
            case TAB -> toHvdcTabInfos(identifiable);
            case MAP -> toMapInfos(identifiable);
            case LIST -> ElementInfosMapper.toListInfos(identifiable);
            case OPERATING_STATUS -> toOperatingStatusInfos(identifiable);
            case FORM -> toFormInfos(identifiable);
            default -> throw new UnsupportedOperationException("TODO");
        };
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
                .operatingStatus(toOperatingStatus(hvdcLine))
                .build();
    }

    private static HvdcOperatingStatusInfos toOperatingStatusInfos(Identifiable<?> identifiable) {
        HvdcLine hvdcLine = (HvdcLine) identifiable;
        Terminal terminal1 = hvdcLine.getConverterStation1().getTerminal();
        Terminal terminal2 = hvdcLine.getConverterStation2().getTerminal();

        return HvdcOperatingStatusInfos.builder()
                .id(hvdcLine.getId())
                .name(hvdcLine.getOptionalName().orElse(null))
                .voltageLevelId1(terminal1.getVoltageLevel().getId())
                .voltageLevelId2(terminal2.getVoltageLevel().getId())
                .operatingStatus(toOperatingStatus(hvdcLine))
                .build();
    }

    private static HvdcTabInfos toHvdcTabInfos(Identifiable<?> identifiable) {
        HvdcLine hvdcLine = (HvdcLine) identifiable;
        HvdcTabInfos.HvdcTabInfosBuilder<?, ?> builder = HvdcTabInfos.builder();
        Terminal terminal1 = hvdcLine.getConverterStation1().getTerminal();
        Terminal terminal2 = hvdcLine.getConverterStation2().getTerminal();
        builder
                .name(hvdcLine.getOptionalName().orElse(null))
                .id(hvdcLine.getId())
                .voltageLevelId1(terminal1.getVoltageLevel().getId())
                .voltageLevelId2(terminal2.getVoltageLevel().getId())
                .country1(mapCountry(terminal1.getVoltageLevel().getSubstation().orElse(null)))
                .country2(mapCountry(terminal2.getVoltageLevel().getSubstation().orElse(null)))
                .i1(nullIfNan(terminal1.getI()))
                .i2(nullIfNan(terminal2.getI()));

        builder
                .convertersMode(hvdcLine.getConvertersMode())
                .converterStationId1(hvdcLine.getConverterStation1().getId())
                .converterStationId2(hvdcLine.getConverterStation2().getId())
                .maxP(hvdcLine.getMaxP())
                .r(hvdcLine.getR())
                .properties(getProperties(hvdcLine))
                .activePowerSetpoint(hvdcLine.getActivePowerSetpoint());

        builder.hvdcAngleDroopActivePowerControl(toHvdcAngleDroopActivePowerControlIdentifiable(hvdcLine));
        builder.hvdcOperatorActivePowerRange(toHvdcOperatorActivePowerRange(hvdcLine));

        return builder.build();
    }

    public static List<HvdcShuntCompensatorsInfos.ShuntCompensatorInfos> toShuntCompensatorInfos(String lccBusOrBusbarSectionId, Stream<ShuntCompensator> shuntCompensators) {
        return shuntCompensators
                .map(s -> HvdcShuntCompensatorsInfos.ShuntCompensatorInfos.builder()
                        .id(s.getId())
                        .name(s.getNameOrId())
                        .connectedToHvdc(Objects.equals(lccBusOrBusbarSectionId, getBusOrBusbarSection(s.getTerminal())))
                        .maxQAtNominalV(s.getG())
                        .build())
                .toList();
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

    private static HvdcFormInfos toFormInfos(Identifiable<?> identifiable) {
        HvdcLine hvdcLine = (HvdcLine) identifiable;
        HvdcFormInfos.HvdcFormInfosBuilder<?, ?> builder = HvdcFormInfos.builder()
                .id(hvdcLine.getId())
                .name(hvdcLine.getOptionalName().orElse(null))
                .nominalV(hvdcLine.getNominalV())
                .r(hvdcLine.getR())
                .maxP(hvdcLine.getMaxP())
                .activePowerSetpoint(hvdcLine.getActivePowerSetpoint())
                .convertersMode(hvdcLine.getConvertersMode())
                .properties(getProperties(hvdcLine));
        if (hvdcLine.getConverterStation1() instanceof LccConverterStation lccConverterStation1
                && hvdcLine.getConverterStation2() instanceof LccConverterStation lccConverterStation2) {
            builder.lccConverterStation1(getLccConverterStationData(lccConverterStation1))
                    .lccConverterStation2(getLccConverterStationData(lccConverterStation2));
        } else if (hvdcLine.getConverterStation1() instanceof VscConverterStation converterStation1
                && hvdcLine.getConverterStation2() instanceof VscConverterStation converterStation2) {
            builder.converterStation1(getVscConverterStationData(converterStation1))
                    .converterStation2(getVscConverterStationData(converterStation2))
                    .hvdcAngleDroopActivePowerControl(toHvdcAngleDroopActivePowerControlIdentifiable(hvdcLine))
                    .hvdcOperatorActivePowerRange(toHvdcOperatorActivePowerRange(hvdcLine))
                    .operatingStatus(toOperatingStatus(hvdcLine));
        }
        return builder.build();
    }

    private static VscConverterStationFormInfos getVscConverterStationData(VscConverterStation converterStation) {
        if (converterStation.getHvdcType() == HvdcConverterStation.HvdcType.VSC) {
            return VscConverterStationInfosMapper.toFormInfos(converterStation);
        }
        return null;
    }

    private static LccConverterStationFormInfos getLccConverterStationData(LccConverterStation converterStation) {
        if (converterStation.getHvdcType() == HvdcConverterStation.HvdcType.LCC) {
            return LccConverterStationInfosMapper.toFormInfos(converterStation);
        }
        return null;
    }
}
