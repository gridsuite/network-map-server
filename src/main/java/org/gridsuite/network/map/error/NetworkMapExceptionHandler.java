/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.error;

import com.powsybl.ws.commons.error.AbstractBusinessExceptionHandler;
import com.powsybl.ws.commons.error.PowsyblWsProblemDetail;
import com.powsybl.ws.commons.error.ServerNameProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class NetworkMapExceptionHandler
    extends AbstractBusinessExceptionHandler<NetworkMapException, NetworkMapBusinessErrorCode> {

    public NetworkMapExceptionHandler(ServerNameProvider serverNameProvider) {
        super(serverNameProvider);
    }

    @NonNull
    @Override
    protected NetworkMapBusinessErrorCode getBusinessCode(NetworkMapException ex) {
        return ex.getBusinessErrorCode();
    }

    @Override
    protected HttpStatus mapStatus(NetworkMapBusinessErrorCode errorCode) {
        return switch (errorCode) {
            case NETWORK_NOT_FOUND, VOLTAGE_LEVEL_NOT_FOUND, ELEMENT_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case SCHEMA_NOT_FOUND -> HttpStatus.NOT_IMPLEMENTED;
        };
    }

    @ExceptionHandler(NetworkMapException.class)
    protected ResponseEntity<PowsyblWsProblemDetail> handleNetworkMapException(
        NetworkMapException exception, HttpServletRequest request) {
        return super.handleDomainException(exception, request);
    }
}
