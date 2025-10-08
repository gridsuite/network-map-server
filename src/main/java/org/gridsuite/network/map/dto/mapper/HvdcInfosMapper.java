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
import org.gridsuite.network.map.dto.InfoTypeParameters;
import org.gridsuite.network.map.dto.definition.extension.HvdcAngleDroopActivePowerControlInfos;
import org.gridsuite.network.map.dto.definition.extension.HvdcOperatorActivePowerRangeInfos;
import org.gridsuite.network.map.dto.definition.hvdc.HvdcMapInfos;
import org.gridsuite.network.map.dto.definition.hvdc.HvdcOperatingStatusInfos;
import org.gridsuite.network.map.dto.definition.hvdc.HvdcShuntCompensatorsInfos;
import org.gridsuite.network.map.dto.definition.hvdc.HvdcTabInfos;
import org.gridsuite.network.map.dto.utils.ElementUtils;
import org.gridsuite.network.map.dto.utils.ExtensionUtils;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static org.gridsuite.network.map.dto.utils.ElementUtils.*;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
public class HvdcInfosMapper {

    protected HvdcInfosMapper() {
    }

    public static ElementInfos toData(Identifiable<?> identifiable, InfoTypeParameters infoTypeParameters) {
        return switch (infoTypeParameters.getInfoType()) {
            case TAB -> toHvdcTabInfos(identifiable);
            case MAP -> toMapInfos(identifiable);
            case LIST -> ElementInfosMapper.toListInfos(identifiable);
            case OPERATING_STATUS -> toOperatingStatusInfos(identifiable);
            default -> throw new UnsupportedOperationException(
                    "InfoType '" + infoTypeParameters.getInfoType() + "' is not supported for HVDC Line elements"
            );
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
                .operatingStatus(ExtensionUtils.toOperatingStatus(hvdcLine))
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
                .operatingStatus(ExtensionUtils.toOperatingStatus(hvdcLine))
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

        // voltageLevels and substations properties
        builder.voltageLevelProperties1(getProperties(terminal1.getVoltageLevel()));
        builder.substationProperties1(terminal1.getVoltageLevel().getSubstation().map(ElementUtils::getProperties).orElse(null));
        builder.voltageLevelProperties2(getProperties(terminal2.getVoltageLevel()));
        builder.substationProperties2(terminal2.getVoltageLevel().getSubstation().map(ElementUtils::getProperties).orElse(null));

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

    protected static List<HvdcShuntCompensatorsInfos.ShuntCompensatorInfos> toShuntCompensatorInfos(String lccBusOrBusbarSectionId, Stream<ShuntCompensator> shuntCompensators) {
        return shuntCompensators
                .filter(shuntCompensator -> shuntCompensator.getModelType() == ShuntCompensatorModelType.LINEAR)
                .map(shuntCompensator -> HvdcShuntCompensatorsInfos.ShuntCompensatorInfos.builder()
                        .id(shuntCompensator.getId())
                        .name(shuntCompensator.getNameOrId())
                        .connectedToHvdc(Objects.equals(lccBusOrBusbarSectionId, getBusOrBusbarSection(shuntCompensator.getTerminal())))
                        .terminalConnected(shuntCompensator.getTerminal().isConnected())
                        .maxQAtNominalV(shuntCompensator.getB() * Math.pow(shuntCompensator.getTerminal().getVoltageLevel().getNominalV(), 2))
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

    protected static Optional<HvdcAngleDroopActivePowerControlInfos> toHvdcAngleDroopActivePowerControlIdentifiable(@NonNull final HvdcLine hvdcLine) {
        return Optional.ofNullable((HvdcAngleDroopActivePowerControl) hvdcLine.getExtension(HvdcAngleDroopActivePowerControl.class))
                .map(hvdcAngleDroopActivePowerControl -> HvdcAngleDroopActivePowerControlInfos.builder()
                        .droop(hvdcAngleDroopActivePowerControl.getDroop())
                        .isEnabled(hvdcAngleDroopActivePowerControl.isEnabled())
                        .p0(hvdcAngleDroopActivePowerControl.getP0()).build());
    }

    protected static Optional<HvdcOperatorActivePowerRangeInfos> toHvdcOperatorActivePowerRange(@NonNull final HvdcLine hvdcLine) {
        return Optional.ofNullable((HvdcOperatorActivePowerRange) hvdcLine.getExtension(HvdcOperatorActivePowerRange.class))
                .map(hvdcOperatorActivePowerRange -> HvdcOperatorActivePowerRangeInfos.builder()
                        .oprFromCS1toCS2(hvdcOperatorActivePowerRange.getOprFromCS1toCS2())
                        .oprFromCS2toCS1(hvdcOperatorActivePowerRange.getOprFromCS2toCS1()).build());
    }
}
