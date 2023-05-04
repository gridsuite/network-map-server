/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.threewindingstransformer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.gridsuite.network.map.dto.voltagelevel.VoltageLevelListInfos;
import org.gridsuite.network.map.model.TapChangerData;

/**
 * @author AJELLAL Ali <ali.ajellal@rte-france.com>
 */
@SuperBuilder
@Getter
public class ThreeWindingsTransformerTabInfos extends AbstractThreeWindingsTransformerInfos {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private VoltageLevelListInfos voltageLevel1;

    //TODO put this into the DTO voltageLevel1
    private String voltageLevelId1;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private VoltageLevelListInfos voltageLevel2;

    //TODO put this into the DTO voltageLevel2
    private String voltageLevelId2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private VoltageLevelListInfos voltageLevel3;

    //TODO put this into the DTO voltageLevel3
    private String voltageLevelId3;
    private Boolean terminal1Connected;

    private Boolean terminal2Connected;

    private Boolean terminal3Connected;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double p1;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double q1;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double p2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double q2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double p3;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double q3;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double i1;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double i2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double i3;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double permanentLimit1;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double permanentLimit2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double permanentLimit3;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private TapChangerData phaseTapChanger1;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private TapChangerData ratioTapChanger1;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private TapChangerData phaseTapChanger2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private TapChangerData ratioTapChanger2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private TapChangerData phaseTapChanger3;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private TapChangerData ratioTapChanger3;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean loadTapChanging1Capabilities;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean loadTapChanging2Capabilities;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean loadTapChanging3Capabilities;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double targetV1;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double targetV2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double targetV3;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String regulatingMode1;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String regulatingMode2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String regulatingMode3;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean regulatingRatio1;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean regulatingRatio2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean regulatingRatio3;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean regulatingPhase1;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean regulatingPhase2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean regulatingPhase3;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double regulatingValue1;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double regulatingValue2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double regulatingValue3;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String busOrBusbarSectionId1;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String busOrBusbarSectionId2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String busOrBusbarSectionId3;

    public static ThreeWindingsTransformerTabInfos toData(Identifiable<?> identifiable) {
        ThreeWindingsTransformer threeWT = (ThreeWindingsTransformer) identifiable;

        Terminal terminal1 = threeWT.getLeg1().getTerminal();
        Terminal terminal2 = threeWT.getLeg2().getTerminal();
        Terminal terminal3 = threeWT.getLeg3().getTerminal();
        ThreeWindingsTransformerTabInfos.ThreeWindingsTransformerTabInfosBuilder builder = ThreeWindingsTransformerTabInfos.builder()
                .name(threeWT.getOptionalName().orElse(null))
                .id(threeWT.getId())
                .terminal1Connected(terminal1.isConnected())
                .terminal2Connected(terminal2.isConnected())
                .terminal3Connected(terminal3.isConnected())
                .voltageLevelId1(terminal1.getVoltageLevel().getId())
                .voltageLevelId2(terminal2.getVoltageLevel().getId())
                .voltageLevelId3(terminal3.getVoltageLevel().getId());

        builder.busOrBusbarSectionId1(getBusOrBusbarSection(terminal1))
                .busOrBusbarSectionId2(getBusOrBusbarSection(terminal2))
                .busOrBusbarSectionId3(getBusOrBusbarSection(terminal3));

        if (!Double.isNaN(terminal1.getP())) {
            builder.p1(terminal1.getP());
        }
        if (!Double.isNaN(terminal1.getQ())) {
            builder.q1(terminal1.getQ());
        }
        if (!Double.isNaN(terminal2.getP())) {
            builder.p2(terminal2.getP());
        }
        if (!Double.isNaN(terminal2.getQ())) {
            builder.q2(terminal2.getQ());
        }
        if (!Double.isNaN(terminal3.getP())) {
            builder.p3(terminal3.getP());
        }
        if (!Double.isNaN(terminal3.getQ())) {
            builder.q3(terminal3.getQ());
        }
        if (!Double.isNaN(terminal1.getI())) {
            builder.i1(terminal1.getI());
        }
        if (!Double.isNaN(terminal2.getI())) {
            builder.i2(terminal2.getI());
        }
        if (!Double.isNaN(terminal3.getI())) {
            builder.i3(terminal3.getI());
        }
        mapThreeWindingsTransformerRatioTapChangers(builder, threeWT);
        mapThreeWindingsTransformerPermanentLimits(builder, threeWT);
        return builder.build();
    }
}
