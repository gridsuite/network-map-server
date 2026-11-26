/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.error;

import com.powsybl.ws.commons.error.BusinessErrorCode;

/**
 * Business error codes emitted by the network-map service.
 */
public enum NetworkMapBusinessErrorCode implements BusinessErrorCode {
    NETWORK_NOT_FOUND("networkMap.networkNotFound"),
    VOLTAGE_LEVEL_NOT_FOUND("networkMap.voltageLevelNotFound"),
    ELEMENT_NOT_FOUND("networkMap.elementNotFound"),
    SCHEMA_NOT_FOUND("networkMap.schemaNotFound");

    private final String code;

    NetworkMapBusinessErrorCode(String code) {
        this.code = code;
    }

    public String value() {
        return code;
    }
}
