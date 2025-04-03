/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.definition.staticvarcompensator;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.StaticVarCompensator;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.gridsuite.network.map.config.nan.NullAndNaNFilter;
import org.gridsuite.network.map.dto.ElementInfosWithProperties;
import org.gridsuite.network.map.dto.definition.extension.InjectionObservabilityInfos;
import org.gridsuite.network.map.dto.definition.extension.MeasurementsInfos;

import java.util.Optional;

/**
 * @author AJELLAL Ali <ali.ajellal@rte-france.com>
 */
@SuperBuilder
@Getter
public class StaticVarCompensatorTabInfos extends ElementInfosWithProperties {

    private String voltageLevelId;

    private Double nominalV; // nominal voltage of the voltage level of the regulating terminal

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Country country;

    private Boolean terminalConnected;

    private StaticVarCompensator.RegulationMode regulationMode;

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NullAndNaNFilter.class)
    private Double p;

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NullAndNaNFilter.class)
    private Double q;

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NullAndNaNFilter.class)
    private Double voltageSetpoint;

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NullAndNaNFilter.class)
    private Double reactivePowerSetpoint;

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private Optional<MeasurementsInfos> measurementQ;

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private Optional<InjectionObservabilityInfos> injectionObservability;
}
