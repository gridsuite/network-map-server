/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@Builder
@Getter
@EqualsAndHashCode
public class LineMapData {

    private String id;

    private String voltageLevelId1;

    private String voltageLevelId2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String name;

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
    private Double permanentLimit1;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double permanentLimit2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String branchStatus;

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

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String connectionName1;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ConnectablePosition.Direction connectionDirection1;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String connectionName2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ConnectablePosition.Direction connectionDirection2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer connectionPosition1;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer connectionPosition2;
}
