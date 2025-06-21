/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.definition.busbarsection;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.gridsuite.network.map.dto.ElementInfosWithProperties;
import org.gridsuite.network.map.dto.definition.extension.MeasurementsInfos;

import java.util.Map;
import java.util.Optional;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@SuperBuilder
@Getter
public class BusBarSectionTabInfos extends ElementInfosWithProperties {
    private String voltageLevelId;

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private Optional<MeasurementsInfos> measurementV;

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private Optional<MeasurementsInfos> measurementAngle;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, String> substationProperties;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, String> voltageLevelProperties;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String substationId;
}
