/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package org.gridsuite.network.map.dto.definition.extension;

import com.powsybl.iidm.network.extensions.ObservabilityArea;
import lombok.Builder;
import lombok.Getter;

/**
 * @author Kamil MARUT {@literal <kamil.marut at rte-france.com>}
 */
@Getter
@Builder
public class ObservabilityAreaInfos {

    private int areaNumber;

    private ObservabilityArea.ObservabilityStatus status;
}
