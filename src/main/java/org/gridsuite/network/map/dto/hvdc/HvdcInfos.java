/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.hvdc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonView;
import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.extensions.HvdcAngleDroopActivePowerControl;
import com.powsybl.iidm.network.extensions.HvdcOperatorActivePowerRange;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.gridsuite.network.map.dto.ElementInfos;
import org.gridsuite.network.map.dto.View;

import static org.gridsuite.network.map.dto.utils.ElementUtils.nullIfNan;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
@SuperBuilder
@Getter
public class HvdcInfos extends ElementInfos {
    @JsonView({View.MapView.class, View.ListView.class})
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String voltageLevelId1;

    @JsonView({View.MapView.class, View.ListView.class})
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String voltageLevelId2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String substationId1;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String substationId2;

    @JsonView({View.MapView.class})
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean terminal1Connected;

    @JsonView({View.MapView.class})
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean terminal2Connected;

    @JsonView({View.MapView.class})
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double p1;

    @JsonView({View.MapView.class})
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double p2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private HvdcLine.ConvertersMode convertersMode;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String converterStationId1;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String converterStationId2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double r;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double nominalV;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double activePowerSetpoint;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double maxP;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Float k;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean isEnabled;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Float p0;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Float oprFromCS1toCS2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Float oprFromCS2toCS1;

    public static HvdcInfos toData(Identifiable<?> identifiable) {
        HvdcLine hvdcLine = (HvdcLine) identifiable;
        Terminal terminal1 = hvdcLine.getConverterStation1().getTerminal();
        Terminal terminal2 = hvdcLine.getConverterStation2().getTerminal();

        HvdcInfos.HvdcInfosBuilder builder = HvdcInfos.builder()
            .id(hvdcLine.getId())
            .name(hvdcLine.getOptionalName().orElse(null))
            .voltageLevelId1(hvdcLine.getConverterStation1().getTerminal().getVoltageLevel().getId())
            .voltageLevelId2(hvdcLine.getConverterStation2().getTerminal().getVoltageLevel().getId())
            .substationId1(terminal1.getVoltageLevel().getSubstation().map(Substation::getId).orElse(null))
            .substationId2(terminal2.getVoltageLevel().getSubstation().map(Substation::getId).orElse(null))
            .terminal1Connected(hvdcLine.getConverterStation1().getTerminal().isConnected())
            .terminal2Connected(hvdcLine.getConverterStation2().getTerminal().isConnected())
            .p1(nullIfNan(hvdcLine.getConverterStation1().getTerminal().getP()))
            .p2(nullIfNan(hvdcLine.getConverterStation2().getTerminal().getP()));

        HvdcAngleDroopActivePowerControl hvdcAngleDroopActivePowerControl = hvdcLine.getExtension(HvdcAngleDroopActivePowerControl.class);
        HvdcOperatorActivePowerRange hvdcOperatorActivePowerRange = hvdcLine.getExtension(HvdcOperatorActivePowerRange.class);
        builder
            .convertersMode(hvdcLine.getConvertersMode())
            .converterStationId1(hvdcLine.getConverterStation1().getId())
            .converterStationId2(hvdcLine.getConverterStation2().getId())
            .maxP(hvdcLine.getMaxP())
            .r(hvdcLine.getR())
            .activePowerSetpoint(hvdcLine.getActivePowerSetpoint());

        if (hvdcAngleDroopActivePowerControl != null) {
            builder.k(hvdcAngleDroopActivePowerControl.getDroop())
                .isEnabled(hvdcAngleDroopActivePowerControl.isEnabled())
                .p0(hvdcAngleDroopActivePowerControl.getP0());
        }

        if (hvdcOperatorActivePowerRange != null) {
            builder.oprFromCS1toCS2(hvdcOperatorActivePowerRange.getOprFromCS1toCS2())
                .oprFromCS2toCS1(hvdcOperatorActivePowerRange.getOprFromCS2toCS1());
        }


        return builder.build();
    }
}
