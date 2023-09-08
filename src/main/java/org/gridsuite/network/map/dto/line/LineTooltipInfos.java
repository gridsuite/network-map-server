/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.line;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Terminal;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.gridsuite.network.map.dto.utils.ElementUtils;
import org.gridsuite.network.map.model.CurrentLimitsData;

import static org.gridsuite.network.map.dto.utils.ElementUtils.nullIfNan;
import static org.gridsuite.network.map.dto.utils.ElementUtils.toMapDataCurrentLimits;

/**
 * @author Le Saulnier Kevin <kevin.lesaulnier at rte-france.com>
 */
@SuperBuilder
@Getter
public class LineTooltipInfos extends AbstractLineInfos {

    private String voltageLevelId1;

    private String voltageLevelId2;

    private Boolean terminal1Connected;

    private Boolean terminal2Connected;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double i1;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double i2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private CurrentLimitsData currentLimits1;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private CurrentLimitsData currentLimits2;

    public static LineTooltipInfos toData(Identifiable<?> identifiable, Double dcPowerFactor) {
        Line line = (Line) identifiable;
        Terminal terminal1 = line.getTerminal1();
        Terminal terminal2 = line.getTerminal2();

        LineTooltipInfos.LineTooltipInfosBuilder<?, ?> builder = LineTooltipInfos.builder()
                .id(line.getId())
                .name(line.getOptionalName().orElse(null))
                .terminal1Connected(terminal1.isConnected())
                .terminal2Connected(terminal2.isConnected())
                .voltageLevelId1(terminal1.getVoltageLevel().getId())
                .voltageLevelId2(terminal2.getVoltageLevel().getId())
                .i1(nullIfNan(ElementUtils.computeIntensity(terminal1, dcPowerFactor)))
                .i2(nullIfNan(ElementUtils.computeIntensity(terminal2, dcPowerFactor)));

        line.getCurrentLimits1().ifPresent(limits1 -> builder.currentLimits1(toMapDataCurrentLimits(limits1)));
        line.getCurrentLimits2().ifPresent(limits2 -> builder.currentLimits2(toMapDataCurrentLimits(limits2)));

        return builder.build();
    }
}
