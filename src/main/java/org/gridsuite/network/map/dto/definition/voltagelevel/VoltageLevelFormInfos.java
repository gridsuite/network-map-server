/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.definition.voltagelevel;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.powsybl.iidm.network.SwitchKind;
import com.powsybl.iidm.network.TopologyKind;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.gridsuite.network.map.dto.ElementFormInfosWithProperties;
import org.gridsuite.network.map.dto.definition.extension.IdentifiableShortCircuitInfos;

import java.util.List;
import java.util.Optional;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
@SuperBuilder
@Getter
public class VoltageLevelFormInfos extends ElementFormInfosWithProperties {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private TopologyKind topologyKind;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String substationId;

    private double nominalVoltage;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double lowVoltageLimit;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double highVoltageLimit;

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private Optional<IdentifiableShortCircuitInfos> identifiableShortCircuit;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer busbarCount;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer sectionCount;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<SwitchKind> switchKinds;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean isRetrievedBusbarSections;

}
