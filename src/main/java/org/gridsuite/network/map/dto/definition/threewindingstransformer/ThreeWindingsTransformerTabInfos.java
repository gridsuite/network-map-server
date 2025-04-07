/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.definition.threewindingstransformer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.powsybl.iidm.network.Country;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.gridsuite.network.map.config.nan.NullAndNaNFilter;
import org.gridsuite.network.map.dto.ElementInfosWithProperties;
import org.gridsuite.network.map.dto.common.TapChangerData;
import org.gridsuite.network.map.dto.definition.extension.MeasurementsInfos;
import org.gridsuite.network.map.dto.definition.extension.TapChangerDiscreteMeasurementsInfos;

import java.util.Optional;

/**
 * @author AJELLAL Ali <ali.ajellal@rte-france.com>
 */
@SuperBuilder
@Getter
public class ThreeWindingsTransformerTabInfos extends ElementInfosWithProperties {

    private String voltageLevelId1;

    private Double nominalV1;

    private String voltageLevelId2;

    private Double nominalV2;

    private String voltageLevelId3;

    private Double nominalV3;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Country country;

    private Boolean terminal1Connected;

    private Boolean terminal2Connected;

    private Boolean terminal3Connected;

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NullAndNaNFilter.class)
    private Double p1;

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NullAndNaNFilter.class)
    private Double q1;

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NullAndNaNFilter.class)
    private Double p2;

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NullAndNaNFilter.class)
    private Double q2;

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NullAndNaNFilter.class)
    private Double p3;

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NullAndNaNFilter.class)
    private Double q3;

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NullAndNaNFilter.class)
    private Double i1;

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NullAndNaNFilter.class)
    private Double i2;

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NullAndNaNFilter.class)
    private Double i3;

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NullAndNaNFilter.class)
    private Double permanentLimit1;

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NullAndNaNFilter.class)
    private Double permanentLimit2;

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NullAndNaNFilter.class)
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

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NullAndNaNFilter.class)
    private Double targetV1;

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NullAndNaNFilter.class)
    private Double targetV2;

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NullAndNaNFilter.class)
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

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NullAndNaNFilter.class)
    private Double regulatingValue1;

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NullAndNaNFilter.class)
    private Double regulatingValue2;

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NullAndNaNFilter.class)
    private Double regulatingValue3;

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private Optional<MeasurementsInfos> measurementP1;

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private Optional<MeasurementsInfos> measurementQ1;

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private Optional<MeasurementsInfos> measurementP2;

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private Optional<MeasurementsInfos> measurementQ2;

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private Optional<MeasurementsInfos> measurementP3;

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private Optional<MeasurementsInfos> measurementQ3;

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private Optional<TapChangerDiscreteMeasurementsInfos> measurementRatioTap1;

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private Optional<TapChangerDiscreteMeasurementsInfos> measurementPhaseTap1;

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private Optional<TapChangerDiscreteMeasurementsInfos> measurementRatioTap2;

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private Optional<TapChangerDiscreteMeasurementsInfos> measurementPhaseTap2;

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private Optional<TapChangerDiscreteMeasurementsInfos> measurementRatioTap3;

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private Optional<TapChangerDiscreteMeasurementsInfos> measurementPhaseTap3;
}
