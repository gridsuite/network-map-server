/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.mapper;

import com.powsybl.iidm.network.*;
import org.gridsuite.network.map.dto.ElementInfos;
import org.gridsuite.network.map.dto.definition.twowindingstransformer.TwoWindingsTransformerFormInfos;
import org.gridsuite.network.map.dto.definition.twowindingstransformer.TwoWindingsTransformerListInfos;
import org.gridsuite.network.map.dto.definition.twowindingstransformer.TwoWindingsTransformerTabInfos;
import org.gridsuite.network.map.dto.definition.twowindingstransformer.TwoWindingsTransformerTooltipInfos;

import static org.gridsuite.network.map.dto.utils.ElementUtils.*;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
public final class TwoWindingsTransformerInfosMapper {
    private TwoWindingsTransformerInfosMapper() {
    }

    public static ElementInfos toData(Identifiable<?> identifiable, ElementInfos.InfoType dataType) {
        switch (dataType) {
            case LIST:
                return toListInfos(identifiable);
            case TOOLTIP:
                return toTooltipInfos(identifiable);
            case TAB:
                return toTabInfos(identifiable);
            case FORM:
                return toFormInfos(identifiable);
            default:
                throw new UnsupportedOperationException("TODO");
        }
    }

    private static TwoWindingsTransformerFormInfos toFormInfos(Identifiable<?> identifiable) {
        TwoWindingsTransformer twoWT = (TwoWindingsTransformer) identifiable;
        Terminal terminal1 = twoWT.getTerminal1();
        Terminal terminal2 = twoWT.getTerminal2();

        TwoWindingsTransformerFormInfos.TwoWindingsTransformerFormInfosBuilder<?, ?> builder = TwoWindingsTransformerFormInfos.builder()
                .name(twoWT.getOptionalName().orElse(null))
                .id(twoWT.getId())
                .terminal1Connected(terminal1.isConnected())
                .terminal2Connected(terminal2.isConnected())
                .voltageLevelId1(terminal1.getVoltageLevel().getId())
                .voltageLevelName1(terminal1.getVoltageLevel().getOptionalName().orElse(null))
                .voltageLevelId2(terminal2.getVoltageLevel().getId())
                .voltageLevelName2(terminal2.getVoltageLevel().getOptionalName().orElse(null))
                .phaseTapChanger(toMapData(twoWT.getPhaseTapChanger()))
                .ratioTapChanger(toMapData(twoWT.getRatioTapChanger()))
                .r(twoWT.getR())
                .x(twoWT.getX())
                .b(twoWT.getB())
                .g(twoWT.getG())
                .ratedU1(twoWT.getRatedU1())
                .ratedU2(twoWT.getRatedU2())
                .busOrBusbarSectionId1(getBusOrBusbarSection(terminal1))
                .busOrBusbarSectionId2(getBusOrBusbarSection(terminal2))
                .ratedS(nullIfNan(twoWT.getRatedS()))
                .p1(nullIfNan(terminal1.getP()))
                .q1(nullIfNan(terminal1.getQ()))
                .p2(nullIfNan(terminal2.getP()))
                .q2(nullIfNan(terminal2.getQ()))
                .i1(nullIfNan(terminal1.getI()))
                .i2(nullIfNan(terminal2.getI()))
                .currentLimits1(toMapDataCurrentLimits(twoWT, Branch.Side.ONE))
                .currentLimits2(toMapDataCurrentLimits(twoWT, Branch.Side.TWO))
                .branchStatus(toBranchStatus(twoWT))
                .connectionDirection1(toMapConnectablePosition(twoWT, 1).getConnectionDirection())
                .connectionName1(toMapConnectablePosition(twoWT, 1).getConnectionName())
                .connectionPosition1(toMapConnectablePosition(twoWT, 1).getConnectionPosition())
                .connectionDirection2(toMapConnectablePosition(twoWT, 2).getConnectionDirection())
                .connectionName2(toMapConnectablePosition(twoWT, 2).getConnectionName())
                .connectionPosition2(toMapConnectablePosition(twoWT, 2).getConnectionPosition());
        return builder.build();
    }

    private static TwoWindingsTransformerTabInfos toTabInfos(Identifiable<?> identifiable) {
        TwoWindingsTransformer twoWT = (TwoWindingsTransformer) identifiable;
        Terminal terminal1 = twoWT.getTerminal1();
        Terminal terminal2 = twoWT.getTerminal2();

        TwoWindingsTransformerTabInfos.TwoWindingsTransformerTabInfosBuilder<?, ?> builder = TwoWindingsTransformerTabInfos.builder()
                .name(twoWT.getOptionalName().orElse(null))
                .id(twoWT.getId())
                .terminal1Connected(terminal1.isConnected())
                .terminal2Connected(terminal2.isConnected())
                .voltageLevelId1(terminal1.getVoltageLevel().getId())
                .voltageLevelName1(terminal1.getVoltageLevel().getOptionalName().orElse(null))
                .nominalVoltage1(terminal1.getVoltageLevel().getNominalV())
                .voltageLevelId2(terminal2.getVoltageLevel().getId())
                .voltageLevelName2(terminal2.getVoltageLevel().getOptionalName().orElse(null))
                .nominalVoltage2(terminal2.getVoltageLevel().getNominalV())
                .phaseTapChanger(toMapData(twoWT.getPhaseTapChanger()))
                .ratioTapChanger(toMapData(twoWT.getRatioTapChanger()))
                .r(twoWT.getR())
                .x(twoWT.getX())
                .b(twoWT.getB())
                .g(twoWT.getG())
                .ratedU1(twoWT.getRatedU1())
                .ratedU2(twoWT.getRatedU2())
                .ratedS(nullIfNan(twoWT.getRatedS()))
                .p1(nullIfNan(terminal1.getP()))
                .q1(nullIfNan(terminal1.getQ()))
                .p2(nullIfNan(terminal2.getP()))
                .q2(nullIfNan(terminal2.getQ()))
                .i1(nullIfNan(terminal1.getI()))
                .i2(nullIfNan(terminal2.getI()))
                .currentLimits1(toMapDataCurrentLimits(twoWT, Branch.Side.ONE))
                .currentLimits2(toMapDataCurrentLimits(twoWT, Branch.Side.TWO))
                .branchStatus(toBranchStatus(twoWT))
                .connectionDirection1(toMapConnectablePosition(twoWT, 1).getConnectionDirection())
                .connectionName1(toMapConnectablePosition(twoWT, 1).getConnectionName())
                .connectionPosition1(toMapConnectablePosition(twoWT, 1).getConnectionPosition())
                .connectionDirection2(toMapConnectablePosition(twoWT, 2).getConnectionDirection())
                .connectionName2(toMapConnectablePosition(twoWT, 2).getConnectionName())
                .connectionPosition2(toMapConnectablePosition(twoWT, 2).getConnectionPosition());
        return builder.build();
    }

    public static TwoWindingsTransformerListInfos toListInfos(Identifiable<?> identifiable) {
        TwoWindingsTransformer twoWT = (TwoWindingsTransformer) identifiable;
        Terminal terminal1 = twoWT.getTerminal1();
        Terminal terminal2 = twoWT.getTerminal2();

        return TwoWindingsTransformerListInfos.builder()
                .id(twoWT.getId())
                .name(twoWT.getOptionalName().orElse(null))
                .voltageLevelId1(terminal1.getVoltageLevel().getId())
                .voltageLevelId2(terminal2.getVoltageLevel().getId())
                .substationId1(terminal1.getVoltageLevel().getSubstation().map(Substation::getId).orElse(null))
                .substationId2(terminal2.getVoltageLevel().getSubstation().map(Substation::getId).orElse(null))
                .build();
    }

    private static TwoWindingsTransformerTooltipInfos toTooltipInfos(Identifiable<?> identifiable) {
        TwoWindingsTransformer twoWindingsTransformer = (TwoWindingsTransformer) identifiable;
        Terminal terminal1 = twoWindingsTransformer.getTerminal1();
        Terminal terminal2 = twoWindingsTransformer.getTerminal2();

        TwoWindingsTransformerTooltipInfos.TwoWindingsTransformerTooltipInfosBuilder<?, ?> builder = TwoWindingsTransformerTooltipInfos.builder()
                .id(twoWindingsTransformer.getId())
                .name(twoWindingsTransformer.getOptionalName().orElse(null))
                .voltageLevelId1(terminal1.getVoltageLevel().getId())
                .voltageLevelId2(terminal2.getVoltageLevel().getId())
                .currentLimits1(toMapDataCurrentLimits(twoWindingsTransformer, Branch.Side.ONE))
                .currentLimits2(toMapDataCurrentLimits(twoWindingsTransformer, Branch.Side.TWO))
                .branchStatus(toBranchStatus(twoWindingsTransformer));

        return builder.build();
    }

}
