/**
 * Copyright © 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.definition.branch;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
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

@SuperBuilder
@Getter
public class BranchTabInfos extends ElementInfosWithProperties {
    private String type;

    private String voltageLevelId1;
    private String voltageLevelId2;

    @JsonInclude(Include.NON_NULL)
    private String voltageLevelName1;

    @JsonInclude(Include.NON_NULL)
    private String voltageLevelName2;

    private Double nominalVoltage1;
    private Double nominalVoltage2;

    @JsonInclude(Include.NON_NULL)
    private String substationId1;

    @JsonInclude(Include.NON_NULL)
    private String substationId2;

    @JsonInclude(Include.NON_NULL)
    private Country country1;

    @JsonInclude(Include.NON_NULL)
    private Country country2;

    private Boolean terminal1Connected;
    private Boolean terminal2Connected;

    @JsonInclude(Include.NON_NULL)
    private Double p1;

    @JsonInclude(Include.NON_NULL)
    private Double q1;

    @JsonInclude(Include.NON_NULL)
    private Double p2;

    @JsonInclude(Include.NON_NULL)
    private Double q2;

    @JsonInclude(Include.NON_NULL)
    private Double i1;

    @JsonInclude(Include.NON_NULL)
    private Double i2;

    @JsonInclude(Include.NON_NULL)
    private Double r;

    @JsonInclude(Include.NON_NULL)
    private Double x;

    @JsonInclude(Include.NON_EMPTY)
    private Map<String, CurrentLimitsData> operationalLimitsGroup1;

    @JsonInclude(Include.NON_EMPTY)
    private List<String> operationalLimitsGroup1Names;

    @JsonInclude(Include.NON_EMPTY)
    private CurrentLimitsData selectedOperationalLimitsGroup1;

    @JsonInclude(Include.NON_NULL)
    private String selectedOperationalLimitsGroup1Name;

    @JsonInclude(Include.NON_EMPTY)
    private Map<String, CurrentLimitsData> operationalLimitsGroup2;

    @JsonInclude(Include.NON_EMPTY)
    private List<String> operationalLimitsGroup2Names;

    @JsonInclude(Include.NON_EMPTY)
    private CurrentLimitsData selectedOperationalLimitsGroup2;

    @JsonInclude(Include.NON_NULL)
    private String selectedOperationalLimitsGroup2Name;

    @JsonInclude(Include.NON_ABSENT)
    private Optional<BranchObservabilityInfos> branchObservability;

    @JsonInclude(Include.NON_NULL)
    private Map<String, String> substationProperties1;

    @JsonInclude(Include.NON_NULL)
    private Map<String, String> voltageLevelProperties1;

    @JsonInclude(Include.NON_NULL)
    private Map<String, String> substationProperties2;

    @JsonInclude(Include.NON_NULL)
    private Map<String, String> voltageLevelProperties2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String operatingStatus;

    /* * *  Extensions * * */

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private Optional<MeasurementsInfos> measurementP1;

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private Optional<MeasurementsInfos> measurementQ1;

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private Optional<MeasurementsInfos> measurementP2;

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private Optional<MeasurementsInfos> measurementQ2;
}
