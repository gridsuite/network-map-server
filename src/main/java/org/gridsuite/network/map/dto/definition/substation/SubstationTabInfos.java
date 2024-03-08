/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.definition.substation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.powsybl.iidm.network.Country;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.gridsuite.network.map.dto.ElementInfos;
import org.gridsuite.network.map.dto.definition.voltagelevel.VoltageLevelTabInfos;

import java.util.List;
import java.util.Map;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
@SuperBuilder
@Getter
public class SubstationTabInfos extends ElementInfos {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, String> properties;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<VoltageLevelTabInfos> voltageLevels;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    Country country;

}
