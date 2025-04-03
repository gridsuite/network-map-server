/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.definition.twowindingstransformer;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.gridsuite.network.map.config.nan.NullAndNaNFilter;
import org.gridsuite.network.map.dto.ElementInfosWithProperties;
import org.gridsuite.network.map.dto.common.CurrentLimitsData;
import org.gridsuite.network.map.dto.common.TapChangerData;
import org.gridsuite.network.map.dto.definition.extension.ConnectablePositionInfos;
import org.gridsuite.network.map.dto.definition.extension.MeasurementsInfos;
import org.gridsuite.network.map.dto.definition.extension.TwoWindingsTransformerToBeEstimatedInfos;

import java.util.List;
import java.util.Optional;

/**
 * @author AJELLAL Ali <ali.ajellal@rte-france.com>
 */
@SuperBuilder
@Getter
public class TwoWindingsTransformerFormInfos extends ElementInfosWithProperties {

    private String voltageLevelId1;

    private String voltageLevelName1;

    private String voltageLevelId2;

    private String voltageLevelName2;

    private Boolean terminal1Connected;

    private Boolean terminal2Connected;

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NullAndNaNFilter.class)
    private Double p1;

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NullAndNaNFilter.class)
    private Double q1;

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NullAndNaNFilter.class)
    private Double p2;

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NullAndNaNFilter.class)
    private Double q2;

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NullAndNaNFilter.class)
    private Double i1;

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NullAndNaNFilter.class)
    private Double i2;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<CurrentLimitsData> currentLimits1;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<CurrentLimitsData> currentLimits2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String selectedOperationalLimitsGroup1;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String selectedOperationalLimitsGroup2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private TapChangerData phaseTapChanger;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private TapChangerData ratioTapChanger;

    private Double g;

    private Double b;

    private Double r;

    private Double x;

    private Double ratedU1;

    private Double ratedU2;

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NullAndNaNFilter.class)
    private Double ratedS;

    private ConnectablePositionInfos connectablePosition1;
    private ConnectablePositionInfos connectablePosition2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String busOrBusbarSectionId1;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String busOrBusbarSectionId2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String operatingStatus;

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private Optional<MeasurementsInfos> measurementP1;

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private Optional<MeasurementsInfos> measurementQ1;

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private Optional<MeasurementsInfos> measurementP2;

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private Optional<MeasurementsInfos> measurementQ2;

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private Optional<TwoWindingsTransformerToBeEstimatedInfos> toBeEstimated;
}
