/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.shuntcompensator;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.ShuntCompensatorLinearModel;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * @author AJELLAL Ali <ali.ajellal@rte-france.com>
 */
@SuperBuilder
@Getter
public class ShuntCompensatorTabInfos extends AbstractShuntCompensator {
    private String voltageLevelId;

    private Double nominalVoltage;

    private Boolean terminalConnected;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double q;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double targetV;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double targetDeadband;

    private Integer sectionCount;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double bPerSection;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double qAtNominalV;

    private Integer maximumSectionCount;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String connectionName;

    private ConnectablePosition.Direction connectionDirection;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer connectionPosition;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String busOrBusbarSectionId;

    public static ShuntCompensatorTabInfos toData(Identifiable<?> identifiable) {
        ShuntCompensator shuntCompensator = (ShuntCompensator) identifiable;

        Terminal terminal = shuntCompensator.getTerminal();
        ShuntCompensatorTabInfos.ShuntCompensatorTabInfosBuilder builder = ShuntCompensatorTabInfos.builder()
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

        var connectablePosition = shuntCompensator.getExtension(ConnectablePosition.class);
        if (connectablePosition != null) {
            builder
                    .connectionDirection(connectablePosition.getFeeder().getDirection())
                    .connectionName(connectablePosition.getFeeder().getName().orElse(null));
            connectablePosition.getFeeder().getOrder().ifPresent(builder::connectionPosition);
        }

        return builder.build();
    }
}
