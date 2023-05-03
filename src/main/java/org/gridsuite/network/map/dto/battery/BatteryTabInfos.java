/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.battery;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.powsybl.iidm.network.Battery;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Terminal;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * @author AJELLAL Ali <ali.ajellal@rte-france.com>
 */
@SuperBuilder
@Getter
public class BatteryTabInfos extends AbstractBatteryInfos {
    private String voltageLevelId;

    private Boolean terminalConnected;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double targetP;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double targetQ;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double p;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double q;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String busOrBusbarSectionId;

    public static BatteryTabInfos toData(Identifiable<?> identifiable) {
        Battery battery = (Battery) identifiable;
        Terminal terminal = battery.getTerminal();
        BatteryTabInfos.BatteryTabInfosBuilder builder = BatteryTabInfos.builder()
                .name(battery.getOptionalName().orElse(null))
                .id(battery.getId())
                .terminalConnected(terminal.isConnected())
                .voltageLevelId(terminal.getVoltageLevel().getId())
                .targetP(battery.getTargetP())
                .targetQ(battery.getTargetQ());
        builder.busOrBusbarSectionId(getBusOrBusbarSection(terminal));

        if (!Double.isNaN(terminal.getP())) {
            builder.p(terminal.getP());
        }
        if (!Double.isNaN(terminal.getQ())) {
            builder.q(terminal.getQ());
        }
        return builder.build();
    }

}
