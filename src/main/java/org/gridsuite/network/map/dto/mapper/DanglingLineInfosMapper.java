/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.mapper;

import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Terminal;
import org.gridsuite.network.map.dto.ElementInfos;
import org.gridsuite.network.map.dto.definition.danglingline.DanglingLineTabInfos;

import static org.gridsuite.network.map.dto.utils.ElementUtils.getProperties;
import static org.gridsuite.network.map.dto.utils.ElementUtils.mapCountry;

/**
 * @author AJELLAL Ali <ali.ajellal@rte-france.com>
 */
public final class DanglingLineInfosMapper {
    private DanglingLineInfosMapper() {
    }

    public static ElementInfos toData(Identifiable<?> identifiable, ElementInfos.ElementInfoType dataType) {
        switch (dataType.getInfoType()) {
            case TAB:
                return toTabInfos(identifiable);
            default:
                throw new UnsupportedOperationException("TODO");
        }
    }

    private static DanglingLineTabInfos toTabInfos(Identifiable<?> identifiable) {
        DanglingLine danglingLine = (DanglingLine) identifiable;
        Terminal terminal = danglingLine.getTerminal();
        DanglingLineTabInfos.DanglingLineTabInfosBuilder<?, ?> builder = DanglingLineTabInfos.builder()
                .name(danglingLine.getOptionalName().orElse(null))
                .id(danglingLine.getId())
                .terminalConnected(terminal.isConnected())
                .voltageLevelId(terminal.getVoltageLevel().getId())
                .nominalV(terminal.getVoltageLevel().getNominalV())
                .country(mapCountry(terminal.getVoltageLevel().getSubstation().orElse(null)))
                .pairingKey(danglingLine.getPairingKey())
                .p0(danglingLine.getP0())
                .properties(getProperties(danglingLine))
                .q0(danglingLine.getQ0());

        if (!Double.isNaN(terminal.getP())) {
            builder.p(terminal.getP());
        }
        if (!Double.isNaN(terminal.getQ())) {
            builder.q(terminal.getQ());
        }
        return builder.build();
    }
}
