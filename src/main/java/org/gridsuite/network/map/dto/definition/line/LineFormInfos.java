/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.definition.line;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.gridsuite.network.map.dto.ElementInfoWithProperties;
import org.gridsuite.network.map.dto.definition.extension.ConnectablePositionInfos;
import org.gridsuite.network.map.dto.common.CurrentLimitsData;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
@SuperBuilder
@Getter
public class LineFormInfos extends ElementInfoWithProperties {

    private String voltageLevelId1;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String voltageLevelName1;

    private String voltageLevelId2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String voltageLevelName2;

    private Boolean terminal1Connected;

    private Boolean terminal2Connected;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double p1;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double q1;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double p2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double q2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double i1;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double i2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private CurrentLimitsData currentLimits1;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private CurrentLimitsData currentLimits2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String operatingStatus;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double r;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double x;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double g1;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double b1;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double g2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double b2;

    private ConnectablePositionInfos connectablePosition1;
    private ConnectablePositionInfos connectablePosition2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String busOrBusbarSectionId1;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String busOrBusbarSectionId2;

}
