/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.load;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.LoadType;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
@SuperBuilder
@Getter
public class LoadTabInfos extends AbstractLoadInfos {

    private LoadType type;

    private String voltageLevelId;

    private Double nominalVoltage;

    private Boolean terminalConnected;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double p;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double q;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double p0;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double q0;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String connectionName;

    private ConnectablePosition.Direction connectionDirection;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer connectionPosition;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String busOrBusbarSectionId;

    public static LoadTabInfos toData(Identifiable<?> identifiable) {
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
