/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.definition.busbarsection;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.gridsuite.network.map.dto.ElementInfos;

/**
 * @author AJELLAL Ali <ali.ajellal at rte-france.com>
 */
@SuperBuilder
@Getter
public class BusBarSectionFormInfos extends ElementInfos {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer vertPos;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer horizPos;

}
