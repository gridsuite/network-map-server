/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.mapper;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.BusbarSectionPosition;
import org.gridsuite.network.map.dto.ElementInfos;
import org.gridsuite.network.map.dto.definition.busbarsection.BusBarSectionFormInfos;
import org.gridsuite.network.map.dto.definition.busbarsection.BusBarSectionTabInfos;

/**
 * @author AJELLAL Ali <ali.ajellal@rte-france.com>
 */

public final class BusBarSectionInfosMapper {
    private BusBarSectionInfosMapper() {
    }

    public static ElementInfos toData(Identifiable<?> identifiable, ElementInfos.ElementInfoType dataType) {
        switch (dataType.getInfoType()) {
            case FORM:
                return toFormInfos(identifiable);
            case TAB:
                return toTabInfos(identifiable);
            default:
                throw new UnsupportedOperationException("TODO");
        }
    }

    public static BusBarSectionFormInfos toFormInfos(Identifiable<?> identifiable) {
        BusbarSection busbarSection = (BusbarSection) identifiable;
        BusBarSectionFormInfos.BusBarSectionFormInfosBuilder<?, ?> builder = BusBarSectionFormInfos.builder().name(busbarSection.getOptionalName().orElse(null)).id(busbarSection.getId());
        var busbarSectionPosition = busbarSection.getExtension(BusbarSectionPosition.class);
        if (busbarSectionPosition != null) {
            builder.vertPos(busbarSectionPosition.getBusbarIndex()).horizPos(busbarSectionPosition.getSectionIndex());
        }
        return builder.build();
    }

    public static BusBarSectionTabInfos toTabInfos(Identifiable<?> identifiable) {
        BusbarSection busbarSection = (BusbarSection) identifiable;
        BusBarSectionTabInfos.BusBarSectionTabInfosBuilder<?, ?> builder = BusBarSectionTabInfos.builder().id(busbarSection.getId())
            .angle(busbarSection.getAngle())
            .v(busbarSection.getV())
            .voltageLevelId(busbarSection.getTerminal().getVoltageLevel().getId())
            .nominalVoltage(busbarSection.getTerminal().getVoltageLevel().getNominalV())
            .countryName(busbarSection.getTerminal().getVoltageLevel().getSubstation().flatMap(substation -> substation.getCountry().map(Country::getName)).orElse(null));

        if (busbarSection.getTerminal().getBusView().getBus() != null) {
            builder.synchronousComponentNum(busbarSection.getTerminal().getBusView().getBus().getSynchronousComponent().getNum())
                .connectedComponentNum(busbarSection.getTerminal().getBusView().getBus().getConnectedComponent().getNum());
        }

        return builder.build();
    }
}
