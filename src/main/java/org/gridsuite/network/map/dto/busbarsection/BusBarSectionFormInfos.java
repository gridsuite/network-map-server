/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.busbarsection;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.BusbarSectionPosition;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * @author AJELLAL Ali <ali.ajellal@rte-france.com>
 */
@SuperBuilder
@Getter
public class BusBarSectionFormInfos extends AbstractBusBarSectionInfos {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer vertPos;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer horizPos;

    public static BusBarSectionFormInfos toData(Identifiable<?> identifiable) {
        BusbarSection busbarSection = (BusbarSection) identifiable;
        BusBarSectionFormInfos.BusBarSectionFormInfosBuilder builder = BusBarSectionFormInfos.builder()
                .name(busbarSection.getOptionalName().orElse(null))
                .id(busbarSection.getId());
        var busbarSectionPosition = busbarSection.getExtension(BusbarSectionPosition.class);
        if (busbarSectionPosition != null) {
            builder
                    .vertPos(busbarSectionPosition.getBusbarIndex())
                    .horizPos(busbarSectionPosition.getSectionIndex());
        }
        return builder.build();
    }
}
