/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.definition.shuntcompensator;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.gridsuite.network.map.dto.ElementInfos;

/**
 * @author AJELLAL Ali <ali.ajellal@rte-france.com>
 */
@SuperBuilder
@Getter
public class ShuntCompensatorFormInfos extends ElementInfos {
    private String voltageLevelId;

    private Boolean terminalConnected;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double q;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double targetV;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double targetDeadband;

    private Integer sectionCount;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double bPerSection;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double qAtNominalV;

    private Integer maximumSectionCount;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String connectionName;

    private ConnectablePosition.Direction connectionDirection;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer connectionPosition;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String busOrBusbarSectionId;

}
