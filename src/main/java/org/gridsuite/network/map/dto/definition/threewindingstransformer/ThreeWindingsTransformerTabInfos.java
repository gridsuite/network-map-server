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
import org.gridsuite.network.map.model.CountryData;
import org.gridsuite.network.map.model.TapChangerData;

/**
 * @author AJELLAL Ali <ali.ajellal@rte-france.com>
 */
@SuperBuilder
@Getter
public class ThreeWindingsTransformerTabInfos extends ElementInfos {

    private String voltageLevelId1;

    private Double nominalV1;

    private String voltageLevelId2;

    private Double nominalV2;

    private String voltageLevelId3;

    private Double nominalV3;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private CountryData country;

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
    private Boolean hasLoadTapChanging1Capabilities;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean hasLoadTapChanging2Capabilities;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean hasLoadTapChanging3Capabilities;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double targetV1;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double targetV2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double targetV3;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String regulationModeName1;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String regulationModeName2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String regulationModeName3;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean isRegulatingRatio1;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean isRegulatingRatio2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean isRegulatingRatio3;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean isRegulatingPhase1;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean isRegulatingPhase2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean isRegulatingPhase3;

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
