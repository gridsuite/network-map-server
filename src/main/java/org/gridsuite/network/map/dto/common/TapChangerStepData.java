/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

/**
 * @author Hugo Marcellin <hugo.marcelin at rte-france.com>
 */
@Builder
@Getter
@EqualsAndHashCode
public class TapChangerStepData {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer index;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double rho;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double r;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double x;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double g;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double b;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double alpha;
}

