/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.powsybl.iidm.network.EnergySource;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@Builder
@Getter
@EqualsAndHashCode
public class GeneratorMapData {

    private String id;

    private String name;

    private String voltageLevelId;

    private Boolean terminalConnected;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double p;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double q;

    //todo delete : active power setpoint
    private Double targetP;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    //todo delete : reactive power setpoint
    private Double targetQ;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    //todo delete : Voltage setpoint
    private Double targetV;

    private Double minP;

    private Double maxP;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private EnergySource energySource;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    //todo delete : rated nominal power
    private Double ratedS;

    private boolean voltageRegulatorOn;

}
