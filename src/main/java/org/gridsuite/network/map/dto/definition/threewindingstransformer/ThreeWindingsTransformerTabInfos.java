/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.definition.threewindingstransformer;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.gridsuite.network.map.dto.ElementInfos;
import org.gridsuite.network.map.model.TapChangerData;

/**
 * @author AJELLAL Ali <ali.ajellal@rte-france.com>
 */
@SuperBuilder
@Getter
public class ThreeWindingsTransformerTabInfos extends ElementInfos {

    private String voltageLevelId1;

    private Double nominalVoltage1;

    private String voltageLevelId2;

    private Double nominalVoltage2;

    private String voltageLevelId3;

    private Double nominalVoltage3;

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
}