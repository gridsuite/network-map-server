/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.definition.tieline;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.powsybl.iidm.network.Country;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.gridsuite.network.map.config.nan.NullAndNaNFilter;
import org.gridsuite.network.map.dto.ElementInfosWithProperties;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@SuperBuilder
@Getter
public class TieLineTabInfos extends ElementInfosWithProperties {

    private String voltageLevelId1;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String voltageLevelName1;

    private Double nominalVoltage1;

    private String voltageLevelId2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String voltageLevelName2;

    private Double nominalVoltage2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Country country1;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Country country2;

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

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NullAndNaNFilter.class)
    private Double r;

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NullAndNaNFilter.class)
    private Double x;

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NullAndNaNFilter.class)
    private Double g1;

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NullAndNaNFilter.class)
    private Double b1;

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NullAndNaNFilter.class)
    private Double g2;

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NullAndNaNFilter.class)
    private Double b2;
}
