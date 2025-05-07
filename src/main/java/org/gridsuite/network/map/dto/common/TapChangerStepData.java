/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.gridsuite.network.map.config.nan.NullAndNaNFilter;

/**
 * @author Hugo Marcellin <hugo.marcelin at rte-france.com>
 */
@Builder
@Getter
@EqualsAndHashCode
public class TapChangerStepData {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer index;

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NullAndNaNFilter.class)
    private Double rho;

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NullAndNaNFilter.class)
    private Double r;

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NullAndNaNFilter.class)
    private Double x;

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NullAndNaNFilter.class)
    private Double g;

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NullAndNaNFilter.class)
    private Double b;

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NullAndNaNFilter.class)
    private Double alpha;
}

