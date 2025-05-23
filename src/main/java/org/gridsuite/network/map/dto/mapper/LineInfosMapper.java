/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.mapper;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.Measurement;
import org.gridsuite.network.map.dto.ElementInfos;
import org.gridsuite.network.map.dto.InfoTypeParameters;
import org.gridsuite.network.map.dto.common.CurrentLimitsData;
import org.gridsuite.network.map.dto.definition.line.*;
import org.gridsuite.network.map.dto.utils.ElementUtils;

import java.util.Map;

import static org.gridsuite.network.map.dto.InfoTypeParameters.QUERY_PARAM_DC_POWERFACTOR;
import static org.gridsuite.network.map.dto.utils.ElementUtils.*;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
public final class LineInfosMapper {
    private LineInfosMapper() {
    }

    public static ElementInfos toData(Identifiable<?> identifiable, InfoTypeParameters infoTypeParameters) {
        String dcPowerFactorStr = infoTypeParameters.getOptionalParameters().getOrDefault(QUERY_PARAM_DC_POWERFACTOR, null);
        Double dcPowerFactor = dcPowerFactorStr == null ? null : Double.valueOf(dcPowerFactorStr);
        return switch (infoTypeParameters.getInfoType()) {
            case TAB -> toTabInfos(identifiable, dcPowerFactor);
            case FORM -> toFormInfos(identifiable);
            case MAP -> toMapInfos(identifiable, dcPowerFactor);
            case LIST -> ElementInfosMapper.toListInfos(identifiable);
            case OPERATING_STATUS -> toOperatingStatusInfos(identifiable);
            case TOOLTIP -> toTooltipInfos(identifiable, dcPowerFactor);
            default -> throw new UnsupportedOperationException("TODO");
        };
    }

    private static LineFormInfos toFormInfos(Identifiable<?> identifiable) {
        Line line = (Line) identifiable;
        Terminal terminal1 = line.getTerminal1();
        Terminal terminal2 = line.getTerminal2();
        LineFormInfos.LineFormInfosBuilder<?, ?> builder = LineFormInfos.builder()
                .name(line.getOptionalName().orElse(null))
                .id(line.getId())
                .terminal1Connected(terminal1.isConnected())
                .terminal2Connected(terminal2.isConnected())
                .voltageLevelId1(terminal1.getVoltageLevel().getId())
                .voltageLevelName1(terminal1.getVoltageLevel().getOptionalName().orElse(null))
                .voltageLevelId2(terminal2.getVoltageLevel().getId())
                .voltageLevelName2(terminal2.getVoltageLevel().getOptionalName().orElse(null))
                .p1(nullIfNan(terminal1.getP()))
                .q1(nullIfNan(terminal1.getQ()))
                .p2(nullIfNan(terminal2.getP()))
                .q2(nullIfNan(terminal2.getQ()))
                .i1(nullIfNan(terminal1.getI()))
                .i2(nullIfNan(terminal2.getI()))
                .r(line.getR())
                .x(line.getX())
                .g1(line.getG1())
                .b1(line.getB1())
                .g2(line.getG2())
                .b2(line.getB2())
                .selectedOperationalLimitsGroup1(line.getSelectedOperationalLimitsGroupId1().orElse(null))
                .selectedOperationalLimitsGroup2(line.getSelectedOperationalLimitsGroupId2().orElse(null))
                .properties(getProperties(line));

        buildCurrentLimits(line.getOperationalLimitsGroups1(), builder::currentLimits1);
        buildCurrentLimits(line.getOperationalLimitsGroups2(), builder::currentLimits2);

        builder.busOrBusbarSectionId1(getBusOrBusbarSection(terminal1))
                .busOrBusbarSectionId2(getBusOrBusbarSection(terminal2));

        builder.operatingStatus(toOperatingStatus(line));

        builder.connectablePosition1(toMapConnectablePosition(line, 1))
                .connectablePosition2(toMapConnectablePosition(line, 2));

        builder.measurementP1(toMeasurement(line, Measurement.Type.ACTIVE_POWER, 0))
                .measurementQ1(toMeasurement(line, Measurement.Type.REACTIVE_POWER, 0))
                .measurementP2(toMeasurement(line, Measurement.Type.ACTIVE_POWER, 1))
                .measurementQ2(toMeasurement(line, Measurement.Type.REACTIVE_POWER, 1));

        return builder.build();
    }

    public static LineOperatingStatusInfos toOperatingStatusInfos(Identifiable<?> identifiable) {
        Line line = (Line) identifiable;
        Terminal terminal1 = line.getTerminal1();
        Terminal terminal2 = line.getTerminal2();

        return LineOperatingStatusInfos.builder()
                .id(line.getId())
                .name(line.getOptionalName().orElse(null))
                .voltageLevelId1(terminal1.getVoltageLevel().getId())
                .voltageLevelName1(terminal1.getVoltageLevel().getOptionalName().orElse(null))
                .voltageLevelId2(terminal2.getVoltageLevel().getId())
                .voltageLevelName2(terminal2.getVoltageLevel().getOptionalName().orElse(null))
                .terminal1Connected(terminal1.isConnected())
                .terminal2Connected(terminal2.isConnected())
                .operatingStatus(toOperatingStatus(line))
                .build();
    }

    private static LineMapInfos toMapInfos(Identifiable<?> identifiable, Double dcPowerFactor) {
        Line line = (Line) identifiable;
        Terminal terminal1 = line.getTerminal1();
        Terminal terminal2 = line.getTerminal2();

        LineMapInfos.LineMapInfosBuilder<?, ?> builder = LineMapInfos.builder()
                .id(line.getId())
                .name(line.getOptionalName().orElse(null))
                .terminal1Connected(terminal1.isConnected())
                .terminal2Connected(terminal2.isConnected())
                .voltageLevelId1(terminal1.getVoltageLevel().getId())
                .voltageLevelName1(terminal1.getVoltageLevel().getOptionalName().orElse(null))
                .voltageLevelId2(terminal2.getVoltageLevel().getId())
                .voltageLevelName2(terminal2.getVoltageLevel().getOptionalName().orElse(null))
                .p1(nullIfNan(terminal1.getP()))
                .p2(nullIfNan(terminal2.getP()))
                .i1(nullIfNan(ElementUtils.computeIntensity(terminal1, dcPowerFactor)))
                .i2(nullIfNan(ElementUtils.computeIntensity(terminal2, dcPowerFactor)));

        line.getCurrentLimits1().ifPresent(limits1 -> builder.currentLimits1(toMapDataCurrentLimits(limits1)));
        line.getCurrentLimits2().ifPresent(limits2 -> builder.currentLimits2(toMapDataCurrentLimits(limits2)));
        builder.operatingStatus(toOperatingStatus(line));

        return builder.build();
    }

    private static LineTabInfos toTabInfos(Identifiable<?> identifiable, Double dcPowerFactor) {
        Line line = (Line) identifiable;
        Terminal terminal1 = line.getTerminal1();
        Terminal terminal2 = line.getTerminal2();
        LineTabInfos.LineTabInfosBuilder<?, ?> builder = LineTabInfos.builder()
                .name(line.getOptionalName().orElse(null))
                .id(line.getId())
                .terminal1Connected(terminal1.isConnected())
                .terminal2Connected(terminal2.isConnected())
                .voltageLevelId1(terminal1.getVoltageLevel().getId())
                .voltageLevelName1(terminal1.getVoltageLevel().getOptionalName().orElse(null))
                .nominalVoltage1(terminal1.getVoltageLevel().getNominalV())
                .voltageLevelId2(terminal2.getVoltageLevel().getId())
                .voltageLevelName2(terminal2.getVoltageLevel().getOptionalName().orElse(null))
                .nominalVoltage2(terminal2.getVoltageLevel().getNominalV())
                .country1(mapCountry(terminal1.getVoltageLevel().getSubstation().orElse(null)))
                .country2(mapCountry(terminal2.getVoltageLevel().getSubstation().orElse(null)))
                .p1(nullIfNan(terminal1.getP()))
                .q1(nullIfNan(terminal1.getQ()))
                .i1(nullIfNan(ElementUtils.computeIntensity(terminal1, dcPowerFactor)))
                .p2(nullIfNan(terminal2.getP()))
                .q2(nullIfNan(terminal2.getQ()))
                .i2(nullIfNan(ElementUtils.computeIntensity(terminal2, dcPowerFactor)))
                .r(line.getR())
                .x(line.getX())
                .g1(line.getG1())
                .b1(line.getB1())
                .g2(line.getG2())
                .properties(getProperties(line))
                .b2(line.getB2());

        Map<String, CurrentLimitsData> mapOperationalLimitsGroup1 = buildCurrentLimitsMap(line.getOperationalLimitsGroups1());
        builder.operationalLimitsGroup1(mapOperationalLimitsGroup1);
        builder.operationalLimitsGroup1Names(mapOperationalLimitsGroup1.keySet().stream().toList());
        builder.selectedOperationalLimitsGroup1(line.getSelectedOperationalLimitsGroupId1().orElse(null));

        Map<String, CurrentLimitsData> mapOperationalLimitsGroup2 = buildCurrentLimitsMap(line.getOperationalLimitsGroups2());
        builder.operationalLimitsGroup2(mapOperationalLimitsGroup2);
        builder.operationalLimitsGroup2Names(mapOperationalLimitsGroup2.keySet().stream().toList());
        builder.selectedOperationalLimitsGroup2(line.getSelectedOperationalLimitsGroupId2().orElse(null));

        // voltageLevels and substations properties
        builder.voltageLevelProperties1(getProperties(terminal1.getVoltageLevel()));
        builder.substationProperties1(terminal1.getVoltageLevel().getSubstation().map(ElementUtils::getProperties).orElse(null));
        builder.voltageLevelProperties2(getProperties(terminal2.getVoltageLevel()));
        builder.substationProperties2(terminal2.getVoltageLevel().getSubstation().map(ElementUtils::getProperties).orElse(null));

        builder.measurementP1(toMeasurement(line, Measurement.Type.ACTIVE_POWER, 0))
            .measurementQ1(toMeasurement(line, Measurement.Type.REACTIVE_POWER, 0))
            .measurementP2(toMeasurement(line, Measurement.Type.ACTIVE_POWER, 1))
            .measurementQ2(toMeasurement(line, Measurement.Type.REACTIVE_POWER, 1));

        builder.branchObservability(toBranchObservability(line));

        return builder.build();
    }

    private static LineTooltipInfos toTooltipInfos(Identifiable<?> identifiable, Double dcPowerFactor) {
        Line line = (Line) identifiable;
        Terminal terminal1 = line.getTerminal1();
        Terminal terminal2 = line.getTerminal2();

        LineTooltipInfos.LineTooltipInfosBuilder<?, ?> builder = LineTooltipInfos.builder()
                .id(line.getId())
                .name(line.getOptionalName().orElse(null))
                .terminal1Connected(terminal1.isConnected())
                .terminal2Connected(terminal2.isConnected())
                .voltageLevelId1(terminal1.getVoltageLevel().getId())
                .voltageLevelId2(terminal2.getVoltageLevel().getId())
                .i1(nullIfNan(ElementUtils.computeIntensity(terminal1, dcPowerFactor)))
                .i2(nullIfNan(ElementUtils.computeIntensity(terminal2, dcPowerFactor)))
                .r(line.getR())
                .x(line.getX())
                .b1(line.getB1())
                .b2(line.getB2());

        line.getCurrentLimits1().ifPresent(limits1 -> builder.currentLimits1(toMapDataCurrentLimits(limits1)));
        line.getCurrentLimits2().ifPresent(limits2 -> builder.currentLimits2(toMapDataCurrentLimits(limits2)));

        return builder.build();
    }

}
