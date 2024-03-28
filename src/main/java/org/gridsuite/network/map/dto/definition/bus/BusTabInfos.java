/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.definition.bus;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.powsybl.iidm.network.Country;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.gridsuite.network.map.dto.ElementInfoWithProperties;

/**
 * @author Hugo Marcellin <hugo.marcelin at rte-france.com>
 */
@SuperBuilder
@Getter
public class BusTabInfos extends ElementInfoWithProperties {
    private Double v;

    private Double angle;

    private Integer synchronousComponentNum;

    private Integer connectedComponentNum;

    private String voltageLevelId;

    private Double nominalVoltage;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Country country;
}
