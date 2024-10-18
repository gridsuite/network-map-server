/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.definition.threewindingstransformer;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.gridsuite.network.map.dto.ElementInfosWithOperatingStatus;

/**
 * @author Maissa Souissi <maissa.souissi at rte-france.com>
 */
@SuperBuilder
@Getter
public class ThreeWindingsTransformerOperatingStatusInfos extends ElementInfosWithOperatingStatus {

    private String voltageLevelId1;

    private String voltageLevelId2;

    private String voltageLevelId3;

}
