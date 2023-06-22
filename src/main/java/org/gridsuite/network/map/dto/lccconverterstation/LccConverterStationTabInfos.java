/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.lccconverterstation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.powsybl.iidm.network.*;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * @author AJELLAL Ali <ali.ajellal@rte-france.com>
 */
@SuperBuilder
@Getter
public class LccConverterStationTabInfos extends AbstractLccConverterStationInfos {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Float powerFactor;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Float lossFactor;

    private String voltageLevelId;

    private Double nominalVoltage;

    private Boolean terminalConnected;

    private String hvdcLineId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double p;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double q;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String busOrBusbarSectionId;

    public static LccConverterStationTabInfos toData(Identifiable<?> identifiable) {
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
