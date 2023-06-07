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
import org.gridsuite.network.map.model.CurrentLimitsData;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
@SuperBuilder
@Getter
public class LineTabInfos extends AbstractLineInfos {

    private String voltageLevelId1;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String voltageLevelName1;

    private Double nominalVoltage1;

    private String voltageLevelId2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String voltageLevelName2;

    private Double nominalVoltage2;

    private Boolean terminal1Connected;

    private Boolean terminal2Connected;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double p1;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double q1;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double p2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double q2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double i1;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double i2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private CurrentLimitsData currentLimits1;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private CurrentLimitsData currentLimits2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double r;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double x;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double g1;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double b1;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double g2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double b2;

    public static LineTabInfos toData(Identifiable<?> identifiable) {
        Line line = (Line) identifiable;
        Terminal terminal1 = line.getTerminal1();
        Terminal terminal2 = line.getTerminal2();
        LineTabInfos.LineTabInfosBuilder builder = LineTabInfos.builder()
                .name(line.getOptionalName().orElse(null))
                .id(line.getId())
                .terminal1Connected(terminal1.isConnected())
                .terminal2Connected(terminal2.isConnected())
                .voltageLevelId1(terminal1.getVoltageLevel().getId())
                .voltageLevelName1(terminal1.getVoltageLevel().getOptionalName().orElse(null))
                .nominalVoltage1(terminal1.getVoltageLevel().getNominalV())
                .voltageLevelId2(terminal2.getVoltageLevel().getId())
                .voltageLevelName2(terminal2.getVoltageLevel().getOptionalName().orElse(null))
                .nominalVoltage2(terminal2.getVoltageLevel().getNominalV())
                .p1(nullIfNan(terminal1.getP()))
                .q1(nullIfNan(terminal1.getQ()))
                .p2(nullIfNan(terminal2.getP()))
                .q2(nullIfNan(terminal2.getQ()))
                .i1(nullIfNan(terminal1.getI()))
                .i2(nullIfNan(terminal2.getI()))
                .r(line.getR())
                .x(line.getX())
                .g1(line.getG1())
                .b1(line.getB1())
                .g2(line.getG2())
                .b2(line.getB2());

        line.getCurrentLimits1().ifPresent(limits1 -> builder.currentLimits1(toMapDataCurrentLimits(limits1)));
        line.getCurrentLimits2().ifPresent(limits2 -> builder.currentLimits2(toMapDataCurrentLimits(limits2)));

        return builder.build();
    }
}
