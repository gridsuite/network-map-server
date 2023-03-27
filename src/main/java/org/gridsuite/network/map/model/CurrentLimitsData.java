/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * @author David Braquart <david.braquart at rte-france.com>
 */
@Builder
@Getter
public class CurrentLimitsData {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double permanentLimit;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<TemporaryLimitData> temporaryLimits;
}

