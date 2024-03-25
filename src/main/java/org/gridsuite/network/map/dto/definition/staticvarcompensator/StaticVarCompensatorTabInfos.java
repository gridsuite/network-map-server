/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.definition.staticvarcompensator;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.StaticVarCompensator;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.gridsuite.network.map.dto.ElementTabInfoWithProperties;

/**
 * @author AJELLAL Ali <ali.ajellal@rte-france.com>
 */
@SuperBuilder
@Getter
public class StaticVarCompensatorTabInfos extends ElementTabInfoWithProperties {

    private String voltageLevelId;

    private Double nominalV; // nominal voltage of the voltage level of the regulating terminal

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Country country;

    private Boolean terminalConnected;

    private StaticVarCompensator.RegulationMode regulationMode;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double p;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double q;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double voltageSetpoint;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double reactivePowerSetpoint;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String busOrBusbarSectionId;

}
