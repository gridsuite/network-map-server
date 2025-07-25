/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.definition.branch.line;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.powsybl.iidm.network.Country;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.gridsuite.network.map.dto.ElementInfosWithProperties;
import org.gridsuite.network.map.dto.common.CurrentLimitsData;
import org.gridsuite.network.map.dto.definition.extension.BranchObservabilityInfos;
import org.gridsuite.network.map.dto.definition.extension.MeasurementsInfos;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
@SuperBuilder
@Getter
public class LineTabInfos extends ElementInfosWithProperties {

    private String voltageLevelId1;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String voltageLevelName1;

    private Double nominalVoltage1;

    private String voltageLevelId2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String voltageLevelName2;

    private Double nominalVoltage2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Country country1;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Country country2;

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

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, CurrentLimitsData> operationalLimitsGroup1;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<String> operationalLimitsGroup1Names;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String selectedOperationalLimitsGroup1;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, CurrentLimitsData> operationalLimitsGroup2;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<String> operationalLimitsGroup2Names;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String selectedOperationalLimitsGroup2;

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

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private Optional<MeasurementsInfos> measurementP1;

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private Optional<MeasurementsInfos> measurementQ1;

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private Optional<MeasurementsInfos> measurementP2;

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private Optional<MeasurementsInfos> measurementQ2;

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private Optional<BranchObservabilityInfos> branchObservability;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, String> substationProperties1;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, String> voltageLevelProperties1;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, String> substationProperties2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, String> voltageLevelProperties2;
}
