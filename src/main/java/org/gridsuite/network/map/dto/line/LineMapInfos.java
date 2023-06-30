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
import com.powsybl.iidm.network.extensions.BranchStatus;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.gridsuite.network.map.model.CurrentLimitsData;

import static org.gridsuite.network.map.dto.utils.ElementUtils.nullIfNan;
import static org.gridsuite.network.map.dto.utils.ElementUtils.toMapDataCurrentLimits;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
@SuperBuilder
@Getter
public class LineMapInfos extends AbstractLineInfos {

    private String voltageLevelId1;

    private String voltageLevelId2;

    private Boolean terminal1Connected;

    private Boolean terminal2Connected;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double p1;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double p2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double i1;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double i2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private CurrentLimitsData currentLimits1;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private CurrentLimitsData currentLimits2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String branchStatus;

    public static LineMapInfos toData(Identifiable<?> identifiable) {
        Line line = (Line) identifiable;
        Terminal terminal1 = line.getTerminal1();
        Terminal terminal2 = line.getTerminal2();
        LineInfos lineInfos = getLinesInfos(line);
        LineMapInfos.LineMapInfosBuilder builder = LineMapInfos.builder()
                .id(line.getId())
                .name(line.getOptionalName().orElse(null))
                .terminal1Connected(terminal1.isConnected())
                .terminal2Connected(terminal2.isConnected())
                .voltageLevelId1(terminal1.getVoltageLevel().getId())
                .voltageLevelId2(terminal2.getVoltageLevel().getId())
                .i1(nullIfNan(terminal1.getI()))
                .i2(nullIfNan(terminal2.getI()))
                .p1(nullIfNan(terminal1.getP()))
                .p2(nullIfNan(terminal2.getP()));

        if (lineInfos.getCurrentLimits1() != null) {
            builder.currentLimits1(lineInfos.getCurrentLimits1());
        }
        if (lineInfos.getCurrentLimits2() != null) {
            builder.currentLimits2(lineInfos.getCurrentLimits2());
        }
        if (lineInfos.getBranchStatus() != null) {
            builder.branchStatus(lineInfos.getBranchStatus().getStatus().name());
        }

        return builder.build();
    }
}
