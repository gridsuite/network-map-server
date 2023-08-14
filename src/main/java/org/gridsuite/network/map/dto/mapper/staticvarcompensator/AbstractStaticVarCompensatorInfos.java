/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.mapper.staticvarcompensator;

import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.Terminal;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.gridsuite.network.map.dto.ElementInfos;
import org.gridsuite.network.map.dto.definition.staticvarcompensator.StaticVarCompensatorTabInfos;

/**
 * @author AJELLAL Ali <ali.ajellal@rte-france.com>
 */
@SuperBuilder
@Getter
public abstract class AbstractStaticVarCompensatorInfos extends ElementInfos {

    public static ElementInfos toData(Identifiable<?> identifiable, InfoType dataType) {
        switch (dataType) {
            case TAB:
                return toStaticVarCompensatorTabInfos(identifiable);
            default:
                throw new UnsupportedOperationException("TODO");
        }
    }

    public static StaticVarCompensatorTabInfos toStaticVarCompensatorTabInfos(Identifiable<?> identifiable) {
        StaticVarCompensator staticVarCompensator = (StaticVarCompensator) identifiable;
        Terminal terminal = staticVarCompensator.getTerminal();
        StaticVarCompensatorTabInfos.StaticVarCompensatorTabInfosBuilder builder = StaticVarCompensatorTabInfos.builder()
                .name(staticVarCompensator.getOptionalName().orElse(null))
                .id(staticVarCompensator.getId())
                .terminalConnected(terminal.isConnected())
                .voltageLevelId(terminal.getVoltageLevel().getId())
                .nominalVoltage(terminal.getVoltageLevel().getNominalV())
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

        return builder.build();
    }

}
