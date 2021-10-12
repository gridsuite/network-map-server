/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@Builder
@Getter
@EqualsAndHashCode
public class DanglingLineMapData {

    private String id;

    private String voltageLevelId;

    private String name;

    private Boolean terminalConnected;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double nominalVoltage;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String ucteXnodeCode;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double p;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double q;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double p0;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double q0;
}
