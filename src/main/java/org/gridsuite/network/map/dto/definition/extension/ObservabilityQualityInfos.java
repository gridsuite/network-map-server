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
import org.gridsuite.network.map.config.nan.NullAndNaNFilter;

/**
 * @author Achour Berrahma <achour.berrahma at rte-france.com>
 */
@Getter
@Builder
public class ObservabilityQualityInfos {

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NullAndNaNFilter.class)
    private Double standardDeviation;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean isRedundant;
}


