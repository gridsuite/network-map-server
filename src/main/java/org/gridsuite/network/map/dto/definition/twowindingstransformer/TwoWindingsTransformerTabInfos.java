/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.definition.twowindingstransformer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.gridsuite.network.map.dto.mapper.twowindingstransformer.AbstractTwoWindingsTransformerInfos;
import org.gridsuite.network.map.model.CurrentLimitsData;
import org.gridsuite.network.map.model.TapChangerData;


/**
 * @author AJELLAL Ali <ali.ajellal@rte-france.com>
 */
@SuperBuilder
@Getter
public class TwoWindingsTransformerTabInfos extends AbstractTwoWindingsTransformerInfos {

    private String voltageLevelId1;

    private String voltageLevelName1;

    private Double nominalVoltage1;

    private String voltageLevelId2;

    private String voltageLevelName2;

    private Double nominalVoltage2;

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
    private TapChangerData phaseTapChanger;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private TapChangerData ratioTapChanger;

    private Double g;

    private Double b;

    private Double r;

    private Double x;

    private Double ratedU1;

    private Double ratedU2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double ratedS;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String connectionName1;

    private ConnectablePosition.Direction connectionDirection1;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String connectionName2;

    private ConnectablePosition.Direction connectionDirection2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer connectionPosition1;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer connectionPosition2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String busOrBusbarSectionId1;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String busOrBusbarSectionId2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String branchStatus;


}
