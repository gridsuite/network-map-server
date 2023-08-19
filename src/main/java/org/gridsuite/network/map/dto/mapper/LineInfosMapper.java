/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.mapper;

import com.powsybl.iidm.network.*;
import org.gridsuite.network.map.dto.ElementInfos;
import org.gridsuite.network.map.dto.definition.line.*;
import org.gridsuite.network.map.dto.utils.ConnectablePositionInfos;

import static org.gridsuite.network.map.dto.utils.ElementUtils.*;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
public final class LineInfosMapper {
    private LineInfosMapper() {
    }

    public static ElementInfos toData(Identifiable<?> identifiable, ElementInfos.InfoType dataType) {
        switch (dataType) {
            case TAB:
                return toTabInfos(identifiable);
            case FORM:
                return toFormInfos(identifiable);
            case MAP:
                return toMapInfos(identifiable);
            case LIST:
                return toListInfos(identifiable);
            case TOOLTIP:
                return toTooltipInfos(identifiable);
            default:
                throw new UnsupportedOperationException("TODO");
        }
    }

    private static LineFormInfos toFormInfos(Identifiable<?> identifiable) {
        Line line = (Line) identifiable;
        Terminal terminal1 = line.getTerminal1();
        Terminal terminal2 = line.getTerminal2();

        ConnectablePositionInfos connectablePositionFeeder1Infos = ConnectablePositionInfos.toConnectablePositionInfos(line, Branch.Side.ONE);
        ConnectablePositionInfos connectablePositionFeeder2Infos = ConnectablePositionInfos.toConnectablePositionInfos(line, Branch.Side.TWO);

        return LineFormInfos.builder()
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
            .currentLimits1(toMapDataCurrentLimits(line, Branch.Side.ONE))
            .currentLimits2(toMapDataCurrentLimits(line, Branch.Side.TWO))
            .branchStatus(toBranchStatus(line))
            .connectionDirection1(connectablePositionFeeder1Infos.getConnectionDirection())
            .connectionName1(connectablePositionFeeder1Infos.getConnectionName())
            .connectionPosition1(connectablePositionFeeder1Infos.getConnectionPosition())
            .connectionDirection2(connectablePositionFeeder2Infos.getConnectionDirection())
            .connectionName2(connectablePositionFeeder2Infos.getConnectionName())
            .connectionPosition2(connectablePositionFeeder2Infos.getConnectionPosition())
            .busOrBusbarSectionId1(getBusOrBusbarSection(terminal1))
            .busOrBusbarSectionId2(getBusOrBusbarSection(terminal2))
            .build();
    }

    public static LineListInfos toListInfos(Identifiable<?> identifiable) {
        Line line = (Line) identifiable;
        Terminal terminal1 = line.getTerminal1();
        Terminal terminal2 = line.getTerminal2();

        return LineListInfos.builder()
            .id(line.getId())
            .name(line.getOptionalName().orElse(null))
            .voltageLevelId1(terminal1.getVoltageLevel().getId())
            .voltageLevelName1(terminal1.getVoltageLevel().getOptionalName().orElse(null))
            .voltageLevelId2(terminal2.getVoltageLevel().getId())
            .voltageLevelName2(terminal2.getVoltageLevel().getOptionalName().orElse(null))
            .terminal1Connected(terminal1.isConnected())
            .terminal2Connected(terminal2.isConnected())
            .substationId1(terminal1.getVoltageLevel().getSubstation().map(Substation::getId).orElse(null))
            .substationId2(terminal2.getVoltageLevel().getSubstation().map(Substation::getId).orElse(null))
            .build();
    }

    private static LineMapInfos toMapInfos(Identifiable<?> identifiable) {
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
            .currentLimits1(toMapDataCurrentLimits(line, Branch.Side.ONE))
            .currentLimits2(toMapDataCurrentLimits(line, Branch.Side.TWO))
            .branchStatus(toBranchStatus(line))
            .build();
    }

    private static LineTabInfos toTabInfos(Identifiable<?> identifiable) {
        Line line = (Line) identifiable;
        Terminal terminal1 = line.getTerminal1();
        Terminal terminal2 = line.getTerminal2();

        return LineTabInfos.builder()
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
            .currentLimits1(toMapDataCurrentLimits(line, Branch.Side.ONE))
            .currentLimits2(toMapDataCurrentLimits(line, Branch.Side.TWO))
            .build();
    }

    private static LineTooltipInfos toTooltipInfos(Identifiable<?> identifiable) {
        Line line = (Line) identifiable;
        Terminal terminal1 = line.getTerminal1();
        Terminal terminal2 = line.getTerminal2();

        return LineTooltipInfos.builder()
            .id(line.getId())
            .name(line.getOptionalName().orElse(null))
            .terminal1Connected(terminal1.isConnected())
            .terminal2Connected(terminal2.isConnected())
            .voltageLevelId1(terminal1.getVoltageLevel().getId())
            .voltageLevelId2(terminal2.getVoltageLevel().getId())
            .currentLimits1(toMapDataCurrentLimits(line, Branch.Side.ONE))
            .currentLimits2(toMapDataCurrentLimits(line, Branch.Side.TWO))
            .branchStatus(toBranchStatus(line))
            .build();
    }

}
