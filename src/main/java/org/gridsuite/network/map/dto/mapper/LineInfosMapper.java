/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.mapper;

import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.extensions.Measurement.Type;
import org.gridsuite.network.map.dto.ElementInfos;
import org.gridsuite.network.map.dto.InfoTypeParameters;
import org.gridsuite.network.map.dto.definition.branch.line.*;
import org.gridsuite.network.map.dto.definition.branch.line.LineTabInfos.LineTabInfosBuilder;
import org.gridsuite.network.map.dto.utils.ExtensionUtils;

import java.util.Optional;

import static org.gridsuite.network.map.dto.InfoTypeParameters.QUERY_PARAM_DC_POWERFACTOR;
import static org.gridsuite.network.map.dto.InfoTypeParameters.QUERY_PARAM_LOAD_OPERATIONAL_LIMIT_GROUPS;
import static org.gridsuite.network.map.dto.utils.ElementUtils.*;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
public final class LineInfosMapper extends BranchInfosMapper {
    private LineInfosMapper() {
        super();
    }

    public static ElementInfos toData(Identifiable<?> identifiable, InfoTypeParameters infoTypeParameters) {
        String dcPowerFactorStr = infoTypeParameters.getOptionalParameters().getOrDefault(QUERY_PARAM_DC_POWERFACTOR, null);
        Double dcPowerFactor = dcPowerFactorStr == null ? null : Double.valueOf(dcPowerFactorStr);
        boolean loadOperationalLimitGroups = Optional.ofNullable(infoTypeParameters.getOptionalParameters().get(QUERY_PARAM_LOAD_OPERATIONAL_LIMIT_GROUPS))
            .map(Boolean::valueOf).orElse(false);
        return switch (infoTypeParameters.getInfoType()) {
            case TAB -> toTabInfos(identifiable, dcPowerFactor, loadOperationalLimitGroups);
            case FORM -> toFormInfos(identifiable);
            case MAP -> toMapInfos(identifiable, dcPowerFactor);
            case LIST -> ElementInfosMapper.toListInfos(identifiable);
            case OPERATING_STATUS -> toOperatingStatusInfos(identifiable);
            case TOOLTIP -> toTooltipInfos(identifiable, dcPowerFactor);
            default -> throw handleUnsupportedInfoType(infoTypeParameters.getInfoType(), "Line");
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
                .properties(getProperties(line))
                .currentLimits(mergeCurrentLimits(line.getOperationalLimitsGroups1(), line.getOperationalLimitsGroups2()))
                .selectedOperationalLimitsGroup1(line.getSelectedOperationalLimitsGroupId1().orElse(null))
                .selectedOperationalLimitsGroup2(line.getSelectedOperationalLimitsGroupId2().orElse(null));

        builder.busOrBusbarSectionId1(getBusOrBusbarSection(terminal1))
                .busOrBusbarSectionId2(getBusOrBusbarSection(terminal2));

        builder.operatingStatus(ExtensionUtils.toOperatingStatus(line, false));

        builder.connectablePosition1(ExtensionUtils.toMapConnectablePosition(line, 1))
                .connectablePosition2(ExtensionUtils.toMapConnectablePosition(line, 2));

        builder.measurementP1(ExtensionUtils.toMeasurement(line, Type.ACTIVE_POWER, 0))
                .measurementQ1(ExtensionUtils.toMeasurement(line, Type.REACTIVE_POWER, 0))
                .measurementP2(ExtensionUtils.toMeasurement(line, Type.ACTIVE_POWER, 1))
                .measurementQ2(ExtensionUtils.toMeasurement(line, Type.REACTIVE_POWER, 1));

        return builder.build();
    }

    private static LineOperatingStatusInfos toOperatingStatusInfos(Identifiable<?> identifiable) {
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
                .operatingStatus(ExtensionUtils.toOperatingStatus(line, terminal1.isConnected() || terminal2.isConnected()))
                .build();
    }

    private static LineMapInfos toMapInfos(Identifiable<?> identifiable, Double dcPowerFactor) {
        Line line = (Line) identifiable;
        Terminal terminal1 = line.getTerminal1();
        Terminal terminal2 = line.getTerminal2();

        return LineMapInfos.builder()
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
                .i1(nullIfNan(computeIntensity(terminal1, dcPowerFactor)))
                .i2(nullIfNan(computeIntensity(terminal2, dcPowerFactor)))
                .operatingStatus(ExtensionUtils.toOperatingStatus(line, terminal1.isConnected() || terminal2.isConnected()))
                .build();
    }

    private static LineTabInfos toTabInfos(Identifiable<?> identifiable, Double dcPowerFactor, boolean loadOperationalLimitGroups) {
        final Line line = (Line) identifiable;
        return toTabBuilder((LineTabInfosBuilder<LineTabInfos, ?>) LineTabInfos.builder(), line, dcPowerFactor, loadOperationalLimitGroups)
                .g1(line.getG1())
                .b1(line.getB1())
                .g2(line.getG2())
                .b2(line.getB2())
                .build();
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
                .i1(nullIfNan(computeIntensity(terminal1, dcPowerFactor)))
                .i2(nullIfNan(computeIntensity(terminal2, dcPowerFactor)))
                .r(line.getR())
                .x(line.getX())
                .b1(line.getB1())
                .b2(line.getB2());

        line.getSelectedOperationalLimitsGroup1().ifPresent(limitsGrp -> limitsGrp.getCurrentLimits().ifPresent(limits -> builder.currentLimits1(toMapDataCurrentLimits(limits, limitsGrp.getId(), null))));
        line.getSelectedOperationalLimitsGroup2().ifPresent(limitsGrp -> limitsGrp.getCurrentLimits().ifPresent(limits -> builder.currentLimits2(toMapDataCurrentLimits(limits, limitsGrp.getId(), null))));

        return builder.build();
    }
}
