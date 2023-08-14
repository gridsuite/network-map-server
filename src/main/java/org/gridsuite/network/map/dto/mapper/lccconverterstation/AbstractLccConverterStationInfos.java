/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.mapper.lccconverterstation;

import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.LccConverterStation;
import com.powsybl.iidm.network.Terminal;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.gridsuite.network.map.dto.ElementInfos;
import org.gridsuite.network.map.dto.definition.lccconverterstation.LccConverterStationTabInfos;

/**
 * @author AJELLAL Ali <ali.ajellal@rte-france.com>
 */

@SuperBuilder
@Getter
public abstract class AbstractLccConverterStationInfos extends ElementInfos {
    public static ElementInfos toData(Identifiable<?> identifiable, InfoType dataType) {
        switch (dataType) {
            case TAB:
                return toLccConverterStationTabInfos(identifiable);
            default:
                throw new UnsupportedOperationException("TODO");
        }
    }

    public static LccConverterStationTabInfos toLccConverterStationTabInfos(Identifiable<?> identifiable) {
        LccConverterStation lccConverterStation = (LccConverterStation) identifiable;
        Terminal terminal = lccConverterStation.getTerminal();
        LccConverterStationTabInfos.LccConverterStationTabInfosBuilder builder = LccConverterStationTabInfos.builder()
                .name(lccConverterStation.getOptionalName().orElse(null))
                .id(lccConverterStation.getId())
                .voltageLevelId(terminal.getVoltageLevel().getId())
                .nominalVoltage(terminal.getVoltageLevel().getNominalV())
                .terminalConnected(terminal.isConnected())
                .lossFactor(lccConverterStation.getLossFactor())
                .powerFactor(lccConverterStation.getPowerFactor());

        if (!Double.isNaN(terminal.getP())) {
            builder.p(terminal.getP());
        }
        if (!Double.isNaN(terminal.getQ())) {
            builder.q(terminal.getQ());
        }
        if (lccConverterStation.getHvdcLine() != null) {
            builder.hvdcLineId(lccConverterStation.getHvdcLine().getId());
        }

        return builder.build();
    }

}
