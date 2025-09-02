/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

@Data
@NoArgsConstructor //needed with setter for spring mvc endpoint
@AllArgsConstructor
public class InfoTypeParameters {
    public static final String QUERY_PARAM_DC_POWERFACTOR = "dcPowerFactor";

    public static final InfoTypeParameters TAB = new InfoTypeParameters(InfoType.TAB, null, true, true, true, true);

    /*@NonNull*/ private InfoType infoType;
    @Nullable private Double dcPowerFactor;
    private boolean viewBranchShowOperationalLimitsGroup = true;
    private boolean viewLineShowOperationalLimitsGroup = true;
    private boolean view2wtShowOperationalLimitsGroup = true;
    private boolean viewGeneratorShowRegulatingTerminal = true;
}
