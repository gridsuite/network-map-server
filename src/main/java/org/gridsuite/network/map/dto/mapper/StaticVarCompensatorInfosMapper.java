/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.mapper;

import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.Terminal;
import org.gridsuite.network.map.dto.ElementInfos;
import org.gridsuite.network.map.dto.InfoTypeParameters;
import org.gridsuite.network.map.dto.definition.staticvarcompensator.StaticVarCompensatorTabInfos;

import static org.gridsuite.network.map.dto.utils.ElementUtils.getProperties;
import static org.gridsuite.network.map.dto.utils.ElementUtils.mapCountry;

/**
 * @author AJELLAL Ali <ali.ajellal@rte-france.com>
 */
public final class StaticVarCompensatorInfosMapper {
    private StaticVarCompensatorInfosMapper() {
    }

    public static ElementInfos toData(Identifiable<?> identifiable, InfoTypeParameters infoTypeParameters) {
        switch (infoTypeParameters.getInfoType()) {
            case TAB:
                return toTabInfos(identifiable);
            case LIST:
                return ElementInfosMapper.toInfosWithType(identifiable);
            default:
                throw new UnsupportedOperationException("TODO");
        }
    }

    private static StaticVarCompensatorTabInfos toTabInfos(Identifiable<?> identifiable) {
        StaticVarCompensator staticVarCompensator = (StaticVarCompensator) identifiable;
        Terminal terminal = staticVarCompensator.getTerminal();
        StaticVarCompensatorTabInfos.StaticVarCompensatorTabInfosBuilder<?, ?> builder = StaticVarCompensatorTabInfos.builder()
                .name(staticVarCompensator.getOptionalName().orElse(null))
                .id(staticVarCompensator.getId())
                .terminalConnected(terminal.isConnected())
                .voltageLevelId(terminal.getVoltageLevel().getId())
                .nominalV(terminal.getVoltageLevel().getNominalV())
                .country(mapCountry(terminal.getVoltageLevel().getSubstation().orElse(null)))
                .properties(getProperties(staticVarCompensator))
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
