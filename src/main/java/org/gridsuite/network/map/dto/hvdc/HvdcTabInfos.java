/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.hvdc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.extensions.HvdcAngleDroopActivePowerControl;
import com.powsybl.iidm.network.extensions.HvdcOperatorActivePowerRange;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
@SuperBuilder
@Getter
public class HvdcTabInfos extends AbstractHvdcInfos {

    private HvdcLine.ConvertersMode convertersMode;

    private String converterStationId1;

    private String converterStationId2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double r;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double nominalV;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double activePowerSetpoint;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double maxP;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Float k;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean isEnabled;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Float p0;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Float oprFromCS1toCS2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Float oprFromCS2toCS1;

    public static HvdcTabInfos toData(Identifiable<?> identifiable) {
        HvdcLine hvdcLine = (HvdcLine) identifiable;
        HvdcTabInfos.HvdcTabInfosBuilder builder = HvdcTabInfos.builder();
        builder
                .name(hvdcLine.getOptionalName().orElse(null))
                .id(hvdcLine.getId());

        HvdcAngleDroopActivePowerControl hvdcAngleDroopActivePowerControl = hvdcLine.getExtension(HvdcAngleDroopActivePowerControl.class);
        HvdcOperatorActivePowerRange hvdcOperatorActivePowerRange = hvdcLine.getExtension(HvdcOperatorActivePowerRange.class);
        builder
                .convertersMode(hvdcLine.getConvertersMode())
                .converterStationId1(hvdcLine.getConverterStation1().getId())
                .converterStationId2(hvdcLine.getConverterStation2().getId())
                .maxP(hvdcLine.getMaxP())
                .r(hvdcLine.getR())
                .activePowerSetpoint(hvdcLine.getActivePowerSetpoint());

        if (hvdcAngleDroopActivePowerControl != null) {
            builder.k(hvdcAngleDroopActivePowerControl.getDroop())
                    .isEnabled(hvdcAngleDroopActivePowerControl.isEnabled())
                    .p0(hvdcAngleDroopActivePowerControl.getP0());
        }

        if (hvdcOperatorActivePowerRange != null) {
            builder.oprFromCS1toCS2(hvdcOperatorActivePowerRange.getOprFromCS1toCS2())
                    .oprFromCS2toCS1(hvdcOperatorActivePowerRange.getOprFromCS2toCS1());
        }

        return builder.build();
    }
}
