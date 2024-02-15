/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.definition.busbarsection;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.gridsuite.network.map.dto.ElementInfos;

/**
 * @author Hugo Marcellin <hugo.marcelin at rte-france.com>
 */
@SuperBuilder
@Getter
public class BusBarSectionTabInfos extends ElementInfos {
    private Double v;

    private Double angle;

    private Integer synchronousComponentNum;

    private Integer synchronousComponentSize;

    private Integer connectedComponentNum;

    private Integer connectedComponentSize;

    private String voltageLevelId;

    private Double nominalVoltage;

    private String countryName;

}