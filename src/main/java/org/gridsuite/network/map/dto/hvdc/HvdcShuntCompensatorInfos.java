/*
  Copyright (c) 2023, RTE (http://www.rte-france.com)
  This Source Code Form is subject to the terms of the Mozilla Public
  License, v. 2.0. If a copy of the MPL was not distributed with this
  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.hvdc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.powsybl.iidm.network.HvdcConverterStation;
import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.Terminal;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.gridsuite.network.map.dto.utils.ElementUtils.getBusOrBusbarSection;

/**
 * @author David Braquart <david.braquart at rte-france.com>
 */
@SuperBuilder
@NoArgsConstructor
@Getter
@Setter
@ToString(callSuper = true)
@Schema(description = "HVDC deletion")
public class HvdcShuntCompensatorInfos {

    private String id;

    private HvdcConverterStation.HvdcType hvdcType;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<SelectedShuntCompensatorData> mcsOnSide1;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<SelectedShuntCompensatorData> mcsOnSide2;

    public static HvdcShuntCompensatorInfos toData(HvdcLine hvdcLine) {
        HvdcConverterStation.HvdcType hvdcType = hvdcLine.getConverterStation1().getHvdcType();
        HvdcShuntCompensatorInfos.HvdcShuntCompensatorInfosBuilder builder = HvdcShuntCompensatorInfos.builder();
        builder.hvdcType(hvdcType);
        if (hvdcType == HvdcConverterStation.HvdcType.LCC) {
            Terminal terminalLcc1 = hvdcLine.getConverterStation1().getTerminal();
            builder.mcsOnSide1(toShuntCompensatorData(getBusOrBusbarSection(terminalLcc1), terminalLcc1.getVoltageLevel().getShuntCompensatorStream()));
            Terminal terminalLcc2 = hvdcLine.getConverterStation2().getTerminal();
            builder.mcsOnSide2(toShuntCompensatorData(getBusOrBusbarSection(terminalLcc2), terminalLcc2.getVoltageLevel().getShuntCompensatorStream()));
        }
        return builder.build();
    }

    private static List<SelectedShuntCompensatorData> toShuntCompensatorData(String lccBusOrBusbarSectionId, Stream<ShuntCompensator> shuntCompensators) {
        return shuntCompensators
                .map(s -> SelectedShuntCompensatorData.builder()
                        .id(s.getId())
                        .selected(Objects.equals(lccBusOrBusbarSectionId, getBusOrBusbarSection(s.getTerminal())))
                        .build())
                .collect(Collectors.toList());
    }
}
