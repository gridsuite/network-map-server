/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.definition.generator;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.powsybl.iidm.network.EnergySource;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.gridsuite.network.map.config.nan.NullAndNaNFilter;
import org.gridsuite.network.map.dto.ElementInfosWithProperties;
import org.gridsuite.network.map.dto.common.MinMaxReactiveLimitsMapData;
import org.gridsuite.network.map.dto.common.ReactiveCapabilityCurveMapData;
import org.gridsuite.network.map.dto.definition.extension.*;

import java.util.List;
import java.util.Optional;

/**
 * @author AJELLAL Ali <ali.ajellal@rte-france.com>
 */
@SuperBuilder
@Getter
public class GeneratorFormInfos extends ElementInfosWithProperties {
    private String voltageLevelId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String regulatingTerminalVlName;

    private Boolean terminalConnected;

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NullAndNaNFilter.class)
    private Double p;

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NullAndNaNFilter.class)
    private Double q;

    private Double targetP;

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NullAndNaNFilter.class)
    private Double targetQ;

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NullAndNaNFilter.class)
    private Double targetV;

    private Double minP;

    private Double maxP;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private EnergySource energySource;

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NullAndNaNFilter.class)
    private Double ratedS;

    private boolean voltageRegulatorOn;

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private Optional<GeneratorStartupInfos> generatorStartup;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private MinMaxReactiveLimitsMapData minMaxReactiveLimits;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<ReactiveCapabilityCurveMapData> reactiveCapabilityCurvePoints;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean participate;

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private Optional<GeneratorShortCircuitInfos> generatorShortCircuit;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String regulatingTerminalConnectableId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String regulatingTerminalConnectableType;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String regulatingTerminalVlId;

    private CoordinatedReactiveControlInfos coordinatedReactiveControl;

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private Optional<ActivePowerControlInfos> activePowerControl;

    private ConnectablePositionInfos connectablePosition;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String busOrBusbarSectionId;


}
