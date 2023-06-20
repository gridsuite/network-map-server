/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.powsybl.iidm.network.Identifiable;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
@SuperBuilder
@Getter
public class ElementInfos {

    public enum InfoType { //LevelOfDetail
        LIST,
        MAP,
        FORM,
        TAB,
        TOOLTIP
    }

    private String id;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String name;

    public static ElementInfos toData(Identifiable<?> identifiable) {
        ElementInfos.ElementInfosBuilder builder = builder()
                .name(identifiable.getOptionalName().orElse(null))
                .id(identifiable.getId());
        return builder.build();
    }
}
