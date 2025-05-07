/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.definition.lccconverterstation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.powsybl.iidm.network.Country;
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
public class LccConverterStationTabInfos extends ElementInfosWithProperties {
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NullAndNaNFilter.class)
    private Float powerFactor;

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NullAndNaNFilter.class)
    private Float lossFactor;

    private String voltageLevelId;

    private Double nominalV;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Country country;

    private Boolean terminalConnected;

    private String hvdcLineId;

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NullAndNaNFilter.class)
    private Double p;

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NullAndNaNFilter.class)
    private Double q;

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private Optional<MeasurementsInfos> measurementP;

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private Optional<MeasurementsInfos> measurementQ;

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private Optional<InjectionObservabilityInfos> injectionObservability;
}
