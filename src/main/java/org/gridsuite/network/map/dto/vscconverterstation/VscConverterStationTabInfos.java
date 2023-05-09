/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.vscconverterstation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.VscConverterStation;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
/**
 * @author AJELLAL Ali <ali.ajellal@rte-france.com>
 */

@SuperBuilder
@Getter
public class VscConverterStationTabInfos extends AbstractVscConverterStationInfos {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Float lossFactor;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double voltageSetpoint;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double reactivePowerSetpoint;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean voltageRegulatorOn;
    private String voltageLevelId;

    private Boolean terminalConnected;

    private String hvdcLineId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double p;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double q;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String busOrBusbarSectionId;

    public static VscConverterStationTabInfos toData(Identifiable<?> identifiable) {
        VscConverterStation vscConverterStation = (VscConverterStation) identifiable;
        Terminal terminal = vscConverterStation.getTerminal();
        VscConverterStationTabInfos.VscConverterStationTabInfosBuilder builder = VscConverterStationTabInfos.builder()
                .name(vscConverterStation.getOptionalName().orElse(null))
                .id(vscConverterStation.getId())
                .voltageLevelId(terminal.getVoltageLevel().getId())
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
