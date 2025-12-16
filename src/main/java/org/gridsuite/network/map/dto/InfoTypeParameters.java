/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class InfoTypeParameters {
    public static final String QUERY_PARAM_DC_POWERFACTOR = "dcPowerFactor";
    public static final String QUERY_PARAM_LOAD_OPERATIONAL_LIMIT_GROUPS = "loadOperationalLimitGroups";
    public static final String QUERY_PARAM_LOAD_REGULATING_TERMINALS = "loadRegulatingTerminals";
    public static final String QUERY_PARAM_LOAD_NETWORK_COMPONENTS = "loadNetworkComponents";
    public static final String QUERY_PARAM_BUS_ID_TO_ICC_VALUES = "busIdToIccValues";

    public static final InfoTypeParameters TAB = new InfoTypeParameters(ElementInfos.InfoType.TAB, null);

    public InfoTypeParameters(ElementInfos.InfoType infoType, Map<String, String> optionalParameters) {
        this.infoType = infoType;
        this.optionalParameters = optionalParameters == null ? new HashMap<>() : optionalParameters;
    }

    private ElementInfos.InfoType infoType;
    private Map<String, String> optionalParameters;
}
