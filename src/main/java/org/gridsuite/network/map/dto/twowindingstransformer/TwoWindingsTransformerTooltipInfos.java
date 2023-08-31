/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.network.map.dto.twowindingstransformer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.powsybl.iidm.network.*;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.gridsuite.network.map.dto.utils.ElementUtils;
import org.gridsuite.network.map.model.CurrentLimitsData;

import static org.gridsuite.network.map.dto.utils.ElementUtils.*;

@SuperBuilder
@Getter
public class TwoWindingsTransformerTooltipInfos extends AbstractTwoWindingsTransformerInfos {

    private String voltageLevelId1;

    private String voltageLevelId2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double i1;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double i2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private CurrentLimitsData currentLimits1;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private CurrentLimitsData currentLimits2;

    public static TwoWindingsTransformerTooltipInfos toData(Identifiable<?> identifiable, Double dcPowerFactor) {
        TwoWindingsTransformer twoWindingsTransformer = (TwoWindingsTransformer) identifiable;
        Terminal terminal1 = twoWindingsTransformer.getTerminal1();
        Terminal terminal2 = twoWindingsTransformer.getTerminal2();

        TwoWindingsTransformerTooltipInfos.TwoWindingsTransformerTooltipInfosBuilder<?, ?> builder = TwoWindingsTransformerTooltipInfos.builder()
            .id(twoWindingsTransformer.getId())
            .name(twoWindingsTransformer.getOptionalName().orElse(null))
            .voltageLevelId1(terminal1.getVoltageLevel().getId())
            .voltageLevelId2(terminal2.getVoltageLevel().getId())
            .i1(nullIfNan(ElementUtils.computeIntensity(terminal1, dcPowerFactor)))
            .i2(nullIfNan(ElementUtils.computeIntensity(terminal2, dcPowerFactor)));

        twoWindingsTransformer.getCurrentLimits1().ifPresent(limits1 -> builder.currentLimits1(toMapDataCurrentLimits(limits1)));
        twoWindingsTransformer.getCurrentLimits2().ifPresent(limits2 -> builder.currentLimits2(toMapDataCurrentLimits(limits2)));

        return builder.build();
    }
}
