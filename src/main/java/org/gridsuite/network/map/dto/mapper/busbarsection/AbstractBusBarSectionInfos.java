/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.mapper.busbarsection;

import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.extensions.BusbarSectionPosition;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.gridsuite.network.map.dto.ElementInfos;
import org.gridsuite.network.map.dto.definition.busbarsection.BusBarSectionFormInfos;

/**
 * @author AJELLAL Ali <ali.ajellal@rte-france.com>
 */

@SuperBuilder
@Getter
public abstract class AbstractBusBarSectionInfos extends ElementInfos {

    public static ElementInfos toData(Identifiable<?> identifiable, InfoType dataType) {
        switch (dataType) {
            case FORM:
                return toBusBarSectionFormInfos(identifiable);
            default:
                throw new UnsupportedOperationException("TODO");
        }
    }

    public static BusBarSectionFormInfos toBusBarSectionFormInfos(Identifiable<?> identifiable) {
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
