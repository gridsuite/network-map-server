/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.definition.branch.twowindingstransformer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.powsybl.iidm.network.Country;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.gridsuite.network.map.dto.common.TapChangerData;
import org.gridsuite.network.map.dto.definition.branch.BranchTabInfos;
import org.gridsuite.network.map.dto.definition.extension.ConnectablePositionInfos;
import org.gridsuite.network.map.dto.definition.extension.TapChangerDiscreteMeasurementsInfos;
import org.gridsuite.network.map.dto.definition.extension.TwoWindingsTransformerToBeEstimatedInfos;

import java.util.Optional;

/**
 * @author AJELLAL Ali <ali.ajellal@rte-france.com>
 */
@SuperBuilder
@Getter
public class TwoWindingsTransformerTabInfos extends BranchTabInfos {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Country country;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private TapChangerData phaseTapChanger;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private TapChangerData ratioTapChanger;

    private Double g;

    private Double b;

    private Double ratedU1;

    private Double ratedU2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double ratedS;

    private ConnectablePositionInfos connectablePosition1;
    private ConnectablePositionInfos connectablePosition2;

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private Optional<TapChangerDiscreteMeasurementsInfos> measurementRatioTap;

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private Optional<TapChangerDiscreteMeasurementsInfos> measurementPhaseTap;

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private Optional<TwoWindingsTransformerToBeEstimatedInfos> toBeEstimated;
}
