/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.definition.staticvarcompensator;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.powsybl.iidm.network.StaticVarCompensator;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.gridsuite.network.map.dto.ElementInfosWithProperties;
import org.gridsuite.network.map.dto.definition.extension.ConnectablePositionInfos;
import org.gridsuite.network.map.dto.definition.extension.StandByAutomatonInfos;

import java.util.Optional;

/**
 * @author REHILI Ghazwa <ghazwarhili@gmail.com>
 */
@SuperBuilder
@Getter
public class StaticVarCompensatorFormInfos extends ElementInfosWithProperties {
    private String voltageLevelId;

    private Double nominalV; // nominal voltage of the voltage level of the regulating terminal

    private Boolean terminalConnected;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double minSusceptance;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double maxSusceptance;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double maxQAtNominalV;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double minQAtNominalV;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double voltageSetpoint;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double reactivePowerSetpoint;

    private StaticVarCompensator.RegulationMode regulationMode;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String busOrBusbarSectionId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String regulatingTerminalConnectableId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String regulatingTerminalConnectableType;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String regulatingTerminalVlId;

    private ConnectablePositionInfos connectablePosition;

    private Optional<StandByAutomatonInfos> standByAutomatonInfos;

}
