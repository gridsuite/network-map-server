/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.network.map.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;

import org.gridsuite.network.map.dto.ElementInfos;

/**
 * @author Seddik Yengui <seddik.yengui at rte-france.com>
 */

@Builder
@Getter
@EqualsAndHashCode
public class VoltageLevelsEquipmentsMapData {

    private ElementInfos voltageLevel;

    private List<VoltageLevelConnectableMapData> equipments;
}
