/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.network.map.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * @author Seddik Yengui <seddik.yengui at rte-france.com>
 */

@Builder
@Getter
@EqualsAndHashCode
public class MinMaxReactiveLimitsMapData {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double minQ;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double maxQ;
}
