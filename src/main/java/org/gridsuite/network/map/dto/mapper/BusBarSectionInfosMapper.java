/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.mapper;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.BusbarSectionPosition;
import com.powsybl.iidm.network.extensions.Measurement;
import org.gridsuite.network.map.dto.ElementInfos;
import org.gridsuite.network.map.dto.InfoTypeParameters;
import org.gridsuite.network.map.dto.definition.busbarsection.BusBarSectionFormInfos;
import org.gridsuite.network.map.dto.definition.busbarsection.BusBarSectionTabInfos;

import static org.gridsuite.network.map.dto.utils.ElementUtils.getProperties;
import static org.gridsuite.network.map.dto.utils.ElementUtils.toMeasurement;

/**
 * @author AJELLAL Ali <ali.ajellal@rte-france.com>
 */

public final class BusBarSectionInfosMapper {
    private BusBarSectionInfosMapper() {
    }

    public static ElementInfos toData(Identifiable<?> identifiable, InfoTypeParameters infoTypeParameters) {
        switch (infoTypeParameters.getInfoType()) {
            case TAB:
                return toTabInfos(identifiable);
            case FORM:
                return toFormInfos(identifiable);
            case LIST:
                return ElementInfosMapper.toListInfos(identifiable);
            case OPERATING_STATUS:
                return ElementInfosMapper.toInfosWithOperatingStatus(identifiable);
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
        return BusBarSectionTabInfos.builder()
            .id(busbarSection.getId())
            .name(busbarSection.getOptionalName().orElse(null))
            .properties(getProperties(busbarSection))
            .voltageLevelId(busbarSection.getTerminal().getVoltageLevel().getId())
            .measurementV(toMeasurement(busbarSection, Measurement.Type.VOLTAGE, 0))
            .measurementAngle(toMeasurement(busbarSection, Measurement.Type.ANGLE, 0))
            .build();
    }
}
