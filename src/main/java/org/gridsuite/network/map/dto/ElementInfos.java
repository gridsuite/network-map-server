/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
@SuperBuilder(builderMethodName = "")
@Getter
public class ElementInfos {

    public enum InfoType { //LevelOfDetail
        LIST,
        MAP,
        FORM,
        TAB,
        TOOLTIP
    }

    public enum Operation {
        CREATION,
        MODIFICATION
    }

    public static class ElementInfoType {
        private InfoType infoType;
        private Double dcPowerFactor;

        private Operation operation;

        public ElementInfoType(InfoType infoType, Double dcPowerFactor, Operation operation) {
            this.infoType = infoType;
            this.dcPowerFactor = dcPowerFactor;
            this.operation = operation;
        }

        public ElementInfoType(InfoType infoType) {
            this.infoType = infoType;
        }

        public InfoType getInfoType() {
            return infoType;
        }

        public Double getDcPowerFactor() {
            return dcPowerFactor;
        }

        public Operation getOperation() {
            return operation;
        }
    }

    private String id;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String name;
}
