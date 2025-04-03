/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.powsybl.iidm.network.PhaseTapChanger;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.gridsuite.network.map.config.nan.NullAndNaNFilter;

import java.util.List;

/**
 * @author Jacques Borsenberger <jacques.borsenberger at rte-france.com>
 */
@Builder
@Getter
@EqualsAndHashCode
public class TapChangerData {
    private Integer lowTapPosition;

    private Integer tapPosition;

    private Integer highTapPosition;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean isRegulating;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean hasLoadTapChangingCapabilities;

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NullAndNaNFilter.class)
    private Double targetV;

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NullAndNaNFilter.class)
    private Double targetDeadband;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private PhaseTapChanger.RegulationMode regulationMode;

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NullAndNaNFilter.class)
    private Double regulationValue;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String regulatingTerminalConnectableId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String regulatingTerminalConnectableType;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String regulatingTerminalVlId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<TapChangerStepData> steps;

}

