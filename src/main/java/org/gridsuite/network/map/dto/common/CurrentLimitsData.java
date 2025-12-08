/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author David Braquart <david.braquart at rte-france.com>
 *     == a powsybl OperationalLimitsGroup == a LimitSet
 */
@Builder
@Getter
@EqualsAndHashCode
@ToString
public class CurrentLimitsData {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String id;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double permanentLimit;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<TemporaryLimitData> temporaryLimits;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, TemporaryLimitData> temporaryLimitsByName;  // only set in BranchInfosMapper when InfoType == TAB

    @JsonInclude
    private Applicability applicability;

    public enum Applicability {
        EQUIPMENT, // applied to both sides
        SIDE1,
        SIDE2,
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    List<LimitsProperty> limitsProperties;

    public boolean limitsEquals(CurrentLimitsData other) {
        return Objects.equals(permanentLimit, other.permanentLimit)
            && Objects.equals(temporaryLimits, other.temporaryLimits)
            && Objects.equals(temporaryLimitsByName, other.temporaryLimitsByName)
            && Objects.equals(limitsProperties, other.limitsProperties);
    }
}

