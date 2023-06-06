/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.threewindingstransformer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
@SuperBuilder
@Getter
public class ThreeWindingsTransformerListInfos extends AbstractThreeWindingsTransformerInfos {

    private String voltageLevelId1;

    private String voltageLevelId2;

    private String voltageLevelId3;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String substationId1;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String substationId2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String substationId3;

    public static ThreeWindingsTransformerListInfos toData(Identifiable<?> identifiable) {
        ThreeWindingsTransformer threeWT = (ThreeWindingsTransformer) identifiable;
        Terminal terminal1 = threeWT.getLeg1().getTerminal();
        Terminal terminal2 = threeWT.getLeg2().getTerminal();
        Terminal terminal3 = threeWT.getLeg3().getTerminal();

        return ThreeWindingsTransformerListInfos.builder()
                .id(threeWT.getId())
                .name(threeWT.getOptionalName().orElse(null))
                .voltageLevelId1(terminal1.getVoltageLevel().getId())
                .voltageLevelId2(terminal2.getVoltageLevel().getId())
                .voltageLevelId3(terminal3.getVoltageLevel().getId())
                .substationId1(terminal1.getVoltageLevel().getSubstation().map(Substation::getId).orElse(null))
                .substationId2(terminal2.getVoltageLevel().getSubstation().map(Substation::getId).orElse(null))
                .substationId3(terminal3.getVoltageLevel().getSubstation().map(Substation::getId).orElse(null))
                .build();
    }
}
