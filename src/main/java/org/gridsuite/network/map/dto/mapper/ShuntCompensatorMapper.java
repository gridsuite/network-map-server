/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.mapper;

import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.ShuntCompensatorLinearModel;
import com.powsybl.iidm.network.Terminal;
import org.gridsuite.network.map.dto.ElementInfos;
import org.gridsuite.network.map.dto.definition.shuntcompensator.ShuntCompensatorFormInfos;
import org.gridsuite.network.map.dto.definition.shuntcompensator.ShuntCompensatorTabInfos;

import static org.gridsuite.network.map.dto.utils.ElementUtils.getBusOrBusbarSection;
import static org.gridsuite.network.map.dto.utils.ElementUtils.toMapConnectablePosition;

/**
 * @author AJELLAL Ali <ali.ajellal@rte-france.com>
 */

public final class ShuntCompensatorMapper {

    private ShuntCompensatorMapper() {
    }

    public static ElementInfos toData(Identifiable<?> identifiable, ElementInfos.ElementInfoType dataType) {
        switch (dataType.getInfoType()) {
            case TAB:
                return toTabInfos(identifiable);
            case FORM:
                return toFormInfos(identifiable);
            default:
                throw new UnsupportedOperationException("TODO");
        }
    }

    private static ShuntCompensatorFormInfos toFormInfos(Identifiable<?> identifiable) {
        ShuntCompensator shuntCompensator = (ShuntCompensator) identifiable;

        Terminal terminal = shuntCompensator.getTerminal();
        ShuntCompensatorFormInfos.ShuntCompensatorFormInfosBuilder<?, ?> builder = ShuntCompensatorFormInfos.builder()
                .name(shuntCompensator.getOptionalName().orElse(null))
                .id(shuntCompensator.getId())
                .maximumSectionCount(shuntCompensator.getMaximumSectionCount())
                .sectionCount(shuntCompensator.getSectionCount())
                .terminalConnected(terminal.isConnected())
                .voltageLevelId(terminal.getVoltageLevel().getId());

        builder.busOrBusbarSectionId(getBusOrBusbarSection(terminal));

        Double bPerSection = null;
        if (shuntCompensator.getModel() instanceof ShuntCompensatorLinearModel) {
            bPerSection = shuntCompensator.getModel(ShuntCompensatorLinearModel.class).getBPerSection();
        }
        if (bPerSection != null) {
            builder.bPerSection(bPerSection);
            builder.qAtNominalV(Math.abs(Math.pow(terminal.getVoltageLevel().getNominalV(), 2) * bPerSection));
        }
        //TODO handle shuntCompensator non linear model
        if (!Double.isNaN(terminal.getQ())) {
            builder.q(terminal.getQ());
        }
        if (!Double.isNaN(shuntCompensator.getTargetV())) {
            builder.targetV(shuntCompensator.getTargetV());
        }
        if (!Double.isNaN(shuntCompensator.getTargetDeadband())) {
            builder.targetDeadband(shuntCompensator.getTargetDeadband());
        }

        builder.connectablePosition(toMapConnectablePosition(shuntCompensator, 0));

        return builder.build();
    }

    private static ShuntCompensatorTabInfos toTabInfos(Identifiable<?> identifiable) {
        ShuntCompensator shuntCompensator = (ShuntCompensator) identifiable;

        Terminal terminal = shuntCompensator.getTerminal();
        ShuntCompensatorTabInfos.ShuntCompensatorTabInfosBuilder<?, ?> builder = ShuntCompensatorTabInfos.builder()
                .name(shuntCompensator.getOptionalName().orElse(null))
                .id(shuntCompensator.getId())
                .maximumSectionCount(shuntCompensator.getMaximumSectionCount())
                .sectionCount(shuntCompensator.getSectionCount())
                .terminalConnected(terminal.isConnected())
                .voltageLevelId(terminal.getVoltageLevel().getId())
                .nominalVoltage(terminal.getVoltageLevel().getNominalV());

        Double bPerSection = null;
        if (shuntCompensator.getModel() instanceof ShuntCompensatorLinearModel) {
            bPerSection = shuntCompensator.getModel(ShuntCompensatorLinearModel.class).getBPerSection();
        }
        if (bPerSection != null) {
            Double qAtNominalV = Math.abs(Math.pow(terminal.getVoltageLevel().getNominalV(), 2) * bPerSection);
            builder.maxQAtNominalV(qAtNominalV * shuntCompensator.getMaximumSectionCount());
            builder.maxSusceptance(bPerSection * shuntCompensator.getMaximumSectionCount());
        }
        //TODO handle shuntCompensator non linear model
        if (!Double.isNaN(terminal.getQ())) {
            builder.q(terminal.getQ());
        }

        builder.connectablePosition(toMapConnectablePosition(shuntCompensator, 0));

        return builder.build();
    }

}
