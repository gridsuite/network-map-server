/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.voltagelevel;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.extensions.IdentifiableShortCircuit;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import static org.gridsuite.network.map.dto.utils.ElementUtils.nullIfNan;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
@SuperBuilder
@Getter
public class VoltageLevelTabInfos extends AbstractVoltageLevelInfos {

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

    public static VoltageLevelTabInfos toData(Identifiable<?> identifiable) {
        VoltageLevel voltageLevel = (VoltageLevel) identifiable;
        VoltageLevelTabInfos.VoltageLevelTabInfosBuilder builder = VoltageLevelTabInfos.builder()
            .id(voltageLevel.getId())
            .name(voltageLevel.getOptionalName().orElse(null))
            .substationId(voltageLevel.getSubstation().orElseThrow().getId())
            .nominalVoltage(voltageLevel.getNominalV())
            .lowVoltageLimit(nullIfNan(voltageLevel.getLowVoltageLimit()))
            .highVoltageLimit(nullIfNan(voltageLevel.getHighVoltageLimit()));

        IdentifiableShortCircuit identifiableShortCircuit = voltageLevel.getExtension(IdentifiableShortCircuit.class);
        if (identifiableShortCircuit != null) {
            Double ipMin = nullIfNan(identifiableShortCircuit.getIpMin());
            if (ipMin != null) {
                builder.ipMin(ipMin / 1000.);  // to get value in kA : value is in A in iidm
            }
            Double ipMax = nullIfNan(identifiableShortCircuit.getIpMax());
            if (ipMax != null) {
                builder.ipMax(ipMax / 1000.);  // to get value in kA : value is in A in iidm
            }
        }

        return builder.build();
    }
}
