/*
  Copyright (c) 2023, RTE (http://www.rte-france.com)
  This Source Code Form is subject to the terms of the Mozilla Public
  License, v. 2.0. If a copy of the MPL was not distributed with this
  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.definition.hvdc;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * @author David Braquart <david.braquart at rte-france.com>
 */
@SuperBuilder
@Getter
@Setter
public class HvdcShuntCompensatorsInfos {

    private String id;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<ShuntCompensatorInfos> mcsOnSide1;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<ShuntCompensatorInfos> mcsOnSide2;

    @Builder
    @Getter
    @EqualsAndHashCode
    public static class ShuntCompensatorInfos {
        private String id;
        private String name;
        private double maxQAtNominalV;
        private boolean connectedToHvdc;
        private boolean terminalConnected;
    }

}
