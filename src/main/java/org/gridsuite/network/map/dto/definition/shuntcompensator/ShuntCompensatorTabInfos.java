/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.definition.shuntcompensator;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.gridsuite.network.map.dto.ElementInfos;
import org.gridsuite.network.map.dto.definition.extension.ConnectablePositionInfos;
import org.gridsuite.network.map.model.CountryData;

/**
 * @author AJELLAL Ali <ali.ajellal@rte-france.com>
 */
@SuperBuilder
@Getter
public class ShuntCompensatorTabInfos extends ElementInfos {
    private String voltageLevelId;

    private Double nominalVoltage;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private CountryData country;

    private Boolean terminalConnected;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double q;

    private Integer sectionCount;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double maxSusceptance;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double maxQAtNominalV;

    private Integer maximumSectionCount;

    private ConnectablePositionInfos connectablePosition;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String busOrBusbarSectionId;

}
