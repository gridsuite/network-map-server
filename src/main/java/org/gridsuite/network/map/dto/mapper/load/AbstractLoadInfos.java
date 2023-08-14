/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.mapper.load;

import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.gridsuite.network.map.dto.ElementInfos;
import org.gridsuite.network.map.dto.definition.load.LoadFormInfos;
import org.gridsuite.network.map.dto.definition.load.LoadTabInfos;

import static org.gridsuite.network.map.dto.utils.ElementUtils.getBusOrBusbarSection;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
@SuperBuilder
@Getter
public abstract class AbstractLoadInfos extends ElementInfos {

    public static ElementInfos toData(Identifiable<?> identifiable, InfoType dataType) {
        switch (dataType) {
            case TAB:
                return toLoadTabInfos(identifiable);
            case FORM:
                return toLoadFormInfos(identifiable);
            default:
                throw new UnsupportedOperationException("TODO");
        }
    }

    public static LoadFormInfos toLoadFormInfos(Identifiable<?> identifiable) {
        Load load = (Load) identifiable;
        Terminal terminal = load.getTerminal();
        LoadFormInfos.LoadFormInfosBuilder builder = LoadFormInfos.builder()
                .name(load.getOptionalName().orElse(null))
                .id(load.getId())
                .type(load.getLoadType())
                .terminalConnected(terminal.isConnected())
                .voltageLevelId(terminal.getVoltageLevel().getId())
                .p0(load.getP0())
                .q0(load.getQ0());

        if (!Double.isNaN(terminal.getP())) {
            builder.p(terminal.getP());
        }
        if (!Double.isNaN(terminal.getQ())) {
            builder.q(terminal.getQ());
        }

        builder.busOrBusbarSectionId(getBusOrBusbarSection(terminal));

        var connectablePosition = load.getExtension(ConnectablePosition.class);
        if (connectablePosition != null && connectablePosition.getFeeder() != null) {
            builder
                    .connectionDirection(connectablePosition.getFeeder().getDirection())
                    .connectionName(connectablePosition.getFeeder().getName().orElse(null));
            connectablePosition.getFeeder().getOrder().ifPresent(builder::connectionPosition);
        }

        return builder.build();
    }

    public static LoadTabInfos toLoadTabInfos(Identifiable<?> identifiable) {
        Load load = (Load) identifiable;
        Terminal terminal = load.getTerminal();
        LoadTabInfos.LoadTabInfosBuilder builder = LoadTabInfos.builder()
                .name(load.getOptionalName().orElse(null))
                .id(load.getId())
                .type(load.getLoadType())
                .terminalConnected(terminal.isConnected())
                .voltageLevelId(terminal.getVoltageLevel().getId())
                .nominalVoltage(terminal.getVoltageLevel().getNominalV())
                .p0(load.getP0())
                .q0(load.getQ0());

        if (!Double.isNaN(terminal.getP())) {
            builder.p(terminal.getP());
        }
        if (!Double.isNaN(terminal.getQ())) {
            builder.q(terminal.getQ());
        }
        var connectablePosition = load.getExtension(ConnectablePosition.class);
        if (connectablePosition != null) {
            builder
                    .connectionDirection(connectablePosition.getFeeder().getDirection())
                    .connectionName(connectablePosition.getFeeder().getName().orElse(null));
            connectablePosition.getFeeder().getOrder().ifPresent(builder::connectionPosition);
        }

        return builder.build();
    }
}