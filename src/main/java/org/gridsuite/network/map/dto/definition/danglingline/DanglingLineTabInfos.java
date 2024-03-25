/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.definition.danglingline;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.powsybl.iidm.network.Country;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.gridsuite.network.map.dto.ElementTabInfoWithProperties;

/**
 * @author AJELLAL Ali <ali.ajellal@rte-france.com>
 */

@SuperBuilder
@Getter
public class DanglingLineTabInfos extends ElementTabInfoWithProperties {

    private String voltageLevelId;

    private Double nominalV;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Country country;

    private Boolean terminalConnected;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String pairingKey;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double p;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double q;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double p0;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double q0;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String busOrBusbarSectionId;

}
