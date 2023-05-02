/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.twoWT;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.powsybl.iidm.network.*;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.gridsuite.network.map.dto.voltagelevel.AbstractVoltageLevelInfos;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
@SuperBuilder
@Getter
public class TwoWindingsTransformerListInfos extends AbstractVoltageLevelInfos {

    private String voltageLevelId1;

    private String voltageLevelId2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String substationId1;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String substationId2;

    public static TwoWindingsTransformerListInfos toData(Identifiable<?> identifiable) {
        TwoWindingsTransformer twoWT = (TwoWindingsTransformer) identifiable;
        Terminal terminal1 = twoWT.getTerminal1();
        Terminal terminal2 = twoWT.getTerminal2();

        return TwoWindingsTransformerListInfos.builder()
                .id(twoWT.getId())
                .name(twoWT.getOptionalName().orElse(null))
                .voltageLevelId1(terminal1.getVoltageLevel().getId())
                .voltageLevelId2(terminal2.getVoltageLevel().getId())
                .substationId1(terminal1.getVoltageLevel().getSubstation().map(Substation::getId).orElse(null))
                .substationId2(terminal2.getVoltageLevel().getSubstation().map(Substation::getId).orElse(null))
                .build();
    }
}
