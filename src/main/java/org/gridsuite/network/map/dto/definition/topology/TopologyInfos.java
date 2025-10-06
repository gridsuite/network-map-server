/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.definition.topology;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.powsybl.iidm.network.SwitchKind;
import com.powsybl.iidm.network.TopologyKind;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Map;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
@SuperBuilder
@Getter
public class TopologyInfos {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private TopologyKind topologyKind;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer busbarCount;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer sectionCount;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<SwitchKind> switchKinds;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean isRetrievedBusbarSections;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean isBusbarSectionPositionFound;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, List<String>> busBarSectionInfos;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, List<FeederBayInfos>> feederBaysInfos;
}
