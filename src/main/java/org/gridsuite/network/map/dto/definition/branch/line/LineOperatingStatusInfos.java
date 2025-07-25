/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.definition.branch.line;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.gridsuite.network.map.dto.ElementInfosWithOperatingStatus;

/**
 * @author Maissa Souissi <maissa.souissi at rte-france.com>
 */
@SuperBuilder
@Getter
public class LineOperatingStatusInfos extends ElementInfosWithOperatingStatus {

    private String voltageLevelId1;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String voltageLevelName1;

    private String voltageLevelId2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String voltageLevelName2;

    private Boolean terminal1Connected;

    private Boolean terminal2Connected;
}
