/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.mapper;

import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.extensions.BusbarSectionPosition;
import com.powsybl.iidm.network.extensions.Measurement.Type;
import org.gridsuite.network.map.dto.ElementInfos;
import org.gridsuite.network.map.dto.InfoTypeParameters;
import org.gridsuite.network.map.dto.definition.busbarsection.BusBarSectionFormInfos;
import org.gridsuite.network.map.dto.definition.busbarsection.BusBarSectionTabInfos;
import org.gridsuite.network.map.dto.utils.ElementUtils;

import static org.gridsuite.network.map.dto.utils.ElementUtils.getProperties;
import static org.gridsuite.network.map.dto.utils.ExtensionUtils.toMeasurement;

/**
 * @author AJELLAL Ali <ali.ajellal@rte-france.com>
 */

public final class BusBarSectionInfosMapper {
    private BusBarSectionInfosMapper() {
    }

    public static ElementInfos toData(Identifiable<?> identifiable, InfoTypeParameters infoTypeParameters) {
        return switch (infoTypeParameters.getInfoType()) {
            case TAB -> toTabInfos(identifiable);
            case FORM -> toFormInfos(identifiable);
            case LIST -> ElementInfosMapper.toListInfos(identifiable);
            case OPERATING_STATUS -> ElementInfosMapper.toInfosWithOperatingStatus(identifiable);
            default -> throw new UnsupportedOperationException("TODO");
        };
    }

    private static BusBarSectionFormInfos toFormInfos(Identifiable<?> identifiable) {
        BusbarSection busbarSection = (BusbarSection) identifiable;
        BusBarSectionFormInfos.BusBarSectionFormInfosBuilder<?, ?> builder = BusBarSectionFormInfos.builder().name(busbarSection.getOptionalName().orElse(null)).id(busbarSection.getId());
        var busbarSectionPosition = busbarSection.getExtension(BusbarSectionPosition.class);
        if (busbarSectionPosition != null) {
            builder.vertPos(busbarSectionPosition.getBusbarIndex()).horizPos(busbarSectionPosition.getSectionIndex());
        }
        return builder.build();
    }

    private static BusBarSectionTabInfos toTabInfos(Identifiable<?> identifiable) {
        BusbarSection busbarSection = (BusbarSection) identifiable;
        Terminal terminal = busbarSection.getTerminal();
        return BusBarSectionTabInfos.builder()
            .id(busbarSection.getId())
            .name(busbarSection.getOptionalName().orElse(null))
            .properties(getProperties(busbarSection))
            .voltageLevelId(busbarSection.getTerminal().getVoltageLevel().getId())
            .measurementV(toMeasurement(busbarSection, Type.VOLTAGE, 0))
            .measurementAngle(toMeasurement(busbarSection, Type.ANGLE, 0))
            .voltageLevelProperties(getProperties(terminal.getVoltageLevel()))
            .substationProperties(terminal.getVoltageLevel().getSubstation().map(ElementUtils::getProperties).orElse(null))
            .build();
    }
}
