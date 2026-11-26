/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.error;

import com.powsybl.ws.commons.error.AbstractBusinessException;
import lombok.NonNull;
import org.gridsuite.network.map.dto.ElementInfos.InfoType;
import org.gridsuite.network.map.dto.ElementType;

import java.util.Objects;
import java.util.UUID;

public class NetworkMapException extends AbstractBusinessException {

    private static final String NOT_FOUND_SUFFIX = "' not found";

    private final NetworkMapBusinessErrorCode errorCode;

    public NetworkMapException(NetworkMapBusinessErrorCode errorCode, String message) {
        super(Objects.requireNonNull(message, "message must not be null"));
        this.errorCode = Objects.requireNonNull(errorCode, "errorCode must not be null");
    }

    public static NetworkMapException createNetworkNotFound(@NonNull UUID networkUuid) {
        return new NetworkMapException(NetworkMapBusinessErrorCode.NETWORK_NOT_FOUND,
                "Network '" + networkUuid + NOT_FOUND_SUFFIX);
    }

    public static NetworkMapException createVoltageLevelNotFound(@NonNull String voltageLevelId) {
        return new NetworkMapException(NetworkMapBusinessErrorCode.VOLTAGE_LEVEL_NOT_FOUND,
                "Voltage level '" + voltageLevelId + NOT_FOUND_SUFFIX);
    }

    public static NetworkMapException createElementNotFound(@NonNull String elementId) {
        return new NetworkMapException(NetworkMapBusinessErrorCode.ELEMENT_NOT_FOUND,
                "Element '" + elementId + NOT_FOUND_SUFFIX);
    }

    public static NetworkMapException createSchemaNotFound(@NonNull ElementType elementType, @NonNull InfoType infoType) {
        return new NetworkMapException(NetworkMapBusinessErrorCode.SCHEMA_NOT_FOUND,
                "The view " + infoType + " for " + elementType + " type is not available yet.");
    }

    @NonNull
    @Override
    public NetworkMapBusinessErrorCode getBusinessErrorCode() {
        return errorCode;
    }
}
