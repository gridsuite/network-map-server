/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.powsybl.iidm.network.LoadingLimits;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author David Braquart <david.braquart at rte-france.com>
 *     == a powsybl OperationalLimitsGroup == a LimitSet
 */
@Builder
@Getter
@Setter
@EqualsAndHashCode
public class CurrentLimitsData {
    // may be null in case we just need the selected limit set and don't really need its name/id
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String id;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double permanentLimit;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<TemporaryLimitData> temporaryLimits;

    @JsonInclude
    private Applicability applicability;

    public enum Applicability {
        EQUIPMENT,
        SIDE1,
        SIDE2,
    }

    public boolean hasLimits() {
        return !Double.isNaN(permanentLimit) || !CollectionUtils.isEmpty(temporaryLimits);
    }

    public boolean limitsEquals(CurrentLimitsData other) {
        return permanentLimit != null && permanentLimit.equals(other.permanentLimit)
            && (temporaryLimits != null && temporaryLimits.equals(other.temporaryLimits)
            || temporaryLimits == null && other.temporaryLimits == null);
    }
}

