/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.definition.voltagelevel;

import com.powsybl.iidm.network.ThreeSides;
import org.gridsuite.network.map.dto.definition.extension.ConnectablePositionInfos;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
public record FeederBayInfos(String busbarId, ConnectablePositionInfos connectablePositionInfos, ThreeSides side) { }
