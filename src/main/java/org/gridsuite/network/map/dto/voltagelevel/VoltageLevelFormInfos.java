/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.voltagelevel;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.IdentifiableShortCircuit;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
@SuperBuilder
@Getter
public class VoltageLevelFormInfos extends AbstractVoltageLevelInfos {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private TopologyKind topologyKind;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String substationId;

    private double nominalVoltage;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double lowVoltageLimit;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double highVoltageLimit;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double ipMin;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double ipMax;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer busbarCount;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer sectionCount;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<SwitchKind> switchKinds;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean isRetrievedBusbarSections;

    public static VoltageLevelFormInfos toData(Identifiable<?> identifiable) {
        VoltageLevel voltageLevel = (VoltageLevel) identifiable;
        VoltageLevelFormInfos.VoltageLevelFormInfosBuilder builder = VoltageLevelFormInfos.builder()
                .name(voltageLevel.getOptionalName().orElse(null))
                .id(voltageLevel.getId())
                .topologyKind(voltageLevel.getTopologyKind())
                .substationId(voltageLevel.getSubstation().map(Substation::getId).orElse(null))
                .nominalVoltage(voltageLevel.getNominalV())
                .lowVoltageLimit(Double.isNaN(voltageLevel.getLowVoltageLimit()) ? null : voltageLevel.getLowVoltageLimit())
                .highVoltageLimit(Double.isNaN(voltageLevel.getHighVoltageLimit()) ? null : voltageLevel.getHighVoltageLimit());

        if (voltageLevel.getTopologyKind().equals(TopologyKind.NODE_BREAKER)) {
            VoltageLevelTopologyInfos vlTopologyInfos = getTopologyInfos(voltageLevel);
            builder.busbarCount(vlTopologyInfos.getBusbarCount());
            builder.sectionCount(vlTopologyInfos.getSectionCount());
            builder.switchKinds(vlTopologyInfos.getSwitchKinds());
            builder.isRetrievedBusbarSections(vlTopologyInfos.isRetrievedBusbarSections());
        }

        IdentifiableShortCircuit identifiableShortCircuit = voltageLevel.getExtension(IdentifiableShortCircuit.class);
        if (identifiableShortCircuit != null) {
            builder.ipMin(identifiableShortCircuit.getIpMin());
            builder.ipMax(identifiableShortCircuit.getIpMax());
        }

        return builder.build();
    }
}
