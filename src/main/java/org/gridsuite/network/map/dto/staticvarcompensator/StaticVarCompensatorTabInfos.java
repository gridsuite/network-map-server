/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.staticvarcompensator;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.powsybl.iidm.network.*;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * @author AJELLAL Ali <ali.ajellal@rte-france.com>
 */
@SuperBuilder
@Getter
public class StaticVarCompensatorTabInfos extends AbstractStaticVarCompensatorInfos {

    private String voltageLevelId;

    private Boolean terminalConnected;

    private StaticVarCompensator.RegulationMode regulationMode;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double p;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double q;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double voltageSetpoint;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double reactivePowerSetpoint;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String busOrBusbarSectionId;

    public static StaticVarCompensatorTabInfos toData(Identifiable<?> identifiable) {
        StaticVarCompensator staticVarCompensator = (StaticVarCompensator) identifiable;
        Terminal terminal = staticVarCompensator.getTerminal();
        StaticVarCompensatorTabInfos.StaticVarCompensatorTabInfosBuilder builder = StaticVarCompensatorTabInfos.builder()
                .name(staticVarCompensator.getOptionalName().orElse(null))
                .id(staticVarCompensator.getId())
                .terminalConnected(terminal.isConnected())
                .voltageLevelId(terminal.getVoltageLevel().getId())
                .regulationMode(staticVarCompensator.getRegulationMode());

        if (!Double.isNaN(terminal.getQ())) {
            builder.q(terminal.getQ());
        }
        if (!Double.isNaN(terminal.getP())) {
            builder.p(terminal.getP());
        }
        if (!Double.isNaN(staticVarCompensator.getVoltageSetpoint())) {
            builder.voltageSetpoint(staticVarCompensator.getVoltageSetpoint());
        }
        if (!Double.isNaN(staticVarCompensator.getReactivePowerSetpoint())) {
            builder.reactivePowerSetpoint(staticVarCompensator.getReactivePowerSetpoint());
        }
        builder.busOrBusbarSectionId(getBusOrBusbarSection(terminal));

        return builder.build();
    }
}
