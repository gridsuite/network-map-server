/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.definition.vscconverterstation;

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
public class VscConverterStationTabInfos extends ElementTabInfoWithProperties {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Float lossFactor;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double voltageSetpoint;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double reactivePowerSetpoint;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean voltageRegulatorOn;
    private String voltageLevelId;

    private Double nominalVoltage;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Country country;

    private Boolean terminalConnected;

    private String hvdcLineId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double p;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double q;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String busOrBusbarSectionId;

}
