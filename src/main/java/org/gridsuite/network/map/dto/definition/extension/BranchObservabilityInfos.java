/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package org.gridsuite.network.map.dto.definition.extension;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

/**
 * @author Achour Berrahma <achour.berrahma at rte-france.com>
 */
@Getter
@Builder
public class BranchObservabilityInfos {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ObservabilityQualityInfos qualityP1;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ObservabilityQualityInfos qualityQ1;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ObservabilityQualityInfos qualityP2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ObservabilityQualityInfos qualityQ2;

    private boolean isObservable;
}


