/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.mapper;

import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.VscConverterStation;
import org.gridsuite.network.map.dto.ElementInfos;
import org.gridsuite.network.map.dto.definition.vscconverterstation.VscConverterStationTabInfos;

/**
 * @author AJELLAL Ali <ali.ajellal@rte-france.com>
 */
public final class VscConverterStationInfosMapper {
    private VscConverterStationInfosMapper() {
    }

    public static ElementInfos toData(Identifiable<?> identifiable, ElementInfos.ElementInfoType dataType) {
        switch (dataType.getInfoType()) {
            case TAB:
                return toTabInfos(identifiable);
            default:
                throw new UnsupportedOperationException("TODO");
        }
    }

    private static VscConverterStationTabInfos toTabInfos(Identifiable<?> identifiable) {
        VscConverterStation vscConverterStation = (VscConverterStation) identifiable;
        Terminal terminal = vscConverterStation.getTerminal();
        VscConverterStationTabInfos.VscConverterStationTabInfosBuilder<?, ?> builder = VscConverterStationTabInfos.builder()
                .name(vscConverterStation.getOptionalName().orElse(null))
                .id(vscConverterStation.getId())
                .voltageLevelId(terminal.getVoltageLevel().getId())
                .nominalVoltage(terminal.getVoltageLevel().getNominalV())
                .terminalConnected(terminal.isConnected())
                .lossFactor(vscConverterStation.getLossFactor())
                .voltageRegulatorOn(vscConverterStation.isVoltageRegulatorOn());

        if (!Double.isNaN(terminal.getP())) {
            builder.p(terminal.getP());
        }
        if (!Double.isNaN(vscConverterStation.getVoltageSetpoint())) {
            builder.voltageSetpoint(vscConverterStation.getVoltageSetpoint());
        }
        if (!Double.isNaN(terminal.getQ())) {
            builder.q(terminal.getQ());
        }
        if (vscConverterStation.getHvdcLine() != null) {
            builder.hvdcLineId(vscConverterStation.getHvdcLine().getId());
        }

        if (!Double.isNaN(vscConverterStation.getReactivePowerSetpoint())) {
            builder.reactivePowerSetpoint(vscConverterStation.getReactivePowerSetpoint());
        }

        return builder.build();
    }

}
