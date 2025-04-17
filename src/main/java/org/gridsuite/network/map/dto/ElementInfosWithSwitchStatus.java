/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * @author Rehili Ghazwa <ghazwa.rehili at rte-france.com>
 */
@SuperBuilder
@Getter
public class ElementInfosWithSwitchStatus extends ElementInfos {
    private boolean isOpen;
}
