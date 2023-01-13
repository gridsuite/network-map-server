/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.powsybl.iidm.network.TopologyKind;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@Builder
@Getter
@EqualsAndHashCode
public class VoltageLevelMapData {

    private String id;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String name;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String substationId;

    private double nominalVoltage;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private TopologyKind topologyKind;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<BusbarSectionMapData> busbarSections;

}
