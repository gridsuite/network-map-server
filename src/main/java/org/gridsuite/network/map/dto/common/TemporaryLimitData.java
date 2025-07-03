/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * @author David Braquart <david.braquart at rte-france.com>
 */
@Builder
@Getter
@Setter
@EqualsAndHashCode
public class TemporaryLimitData {
    private String name;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double value;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer acceptableDuration;
}

