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
import org.gridsuite.network.map.dto.ElementInfosWithProperties;
import org.gridsuite.network.map.dto.definition.voltagelevel.VoltageLevelFormInfos;

import java.util.List;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
@SuperBuilder
@Getter
public class SubstationFormInfos extends ElementInfosWithProperties {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Country country;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<VoltageLevelFormInfos> voltageLevels;

}
