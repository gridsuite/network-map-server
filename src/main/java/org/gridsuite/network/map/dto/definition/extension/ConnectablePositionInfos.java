/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.definition.extension;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import lombok.Builder;
import lombok.Getter;

/**
 * @author Souissi Maissa <maissa.souissi at rte-france.com>
 */

@Getter
@Builder
public class ConnectablePositionInfos {
    private ConnectablePosition.Direction connectionDirection;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer connectionPosition;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String connectionName;

}


