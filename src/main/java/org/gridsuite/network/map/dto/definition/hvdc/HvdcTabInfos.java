/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.definition.hvdc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.powsybl.iidm.network.HvdcLine;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.gridsuite.network.map.dto.mapper.hvdc.AbstractHvdcInfos;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
@SuperBuilder
@Getter
public class HvdcTabInfos extends AbstractHvdcInfos {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private HvdcLine.ConvertersMode convertersMode;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String converterStationId1;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String converterStationId2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double r;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double nominalV;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double activePowerSetpoint;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double maxP;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Float k;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean isEnabled;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Float p0;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Float oprFromCS1toCS2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Float oprFromCS2toCS1;


}
