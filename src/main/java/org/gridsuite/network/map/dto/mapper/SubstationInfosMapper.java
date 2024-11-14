/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.mapper;

import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Substation;
import org.gridsuite.network.map.dto.ElementInfos;
import org.gridsuite.network.map.dto.InfoTypeParameters;
import org.gridsuite.network.map.dto.definition.substation.SubstationFormInfos;
import org.gridsuite.network.map.dto.definition.substation.SubstationMapInfos;
import org.gridsuite.network.map.dto.definition.substation.SubstationTabInfos;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.gridsuite.network.map.dto.utils.ElementUtils.getProperties;
import static org.gridsuite.network.map.dto.utils.ElementUtils.mapCountry;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
public final class SubstationInfosMapper {
    private SubstationInfosMapper() {
    }

    public static ElementInfos toData(Identifiable<?> identifiable, InfoTypeParameters infoTypeParameters) {
        switch (infoTypeParameters.getInfoType()) {
            case TAB:
                return toTabInfos(identifiable);
            case MAP:
                return toMapInfos(identifiable);
            case FORM:
                return toFormInfos(identifiable);
            case LIST:
                return ElementInfosMapper.toListInfos(identifiable);
            default:
                throw new UnsupportedOperationException("TODO");
        }
    }

    private static SubstationFormInfos toFormInfos(Identifiable<?> identifiable) {
        Substation substation = (Substation) identifiable;
        return SubstationFormInfos.builder()
                .name(substation.getOptionalName().orElse(null))
                .id(substation.getId())
                .country(mapCountry(substation))
                .properties(getProperties(substation))
                .voltageLevels(List.of())
                .voltageLevels(substation.getVoltageLevelStream().map(VoltageLevelInfosMapper::toFormInfos).collect(Collectors.toList()))
                .build();
    }

    private static SubstationMapInfos toMapInfos(Identifiable<?> identifiable) {
        Substation substation = (Substation) identifiable;
        return SubstationMapInfos.builder()
                .id(substation.getId())
                .name(substation.getOptionalName().orElse(null))
                .voltageLevels(substation.getVoltageLevelStream().map(VoltageLevelInfosMapper::toMapInfos).collect(Collectors.toList()))
                .build();
    }

    private static SubstationTabInfos toTabInfos(Identifiable<?> identifiable) {
        Substation substation = (Substation) identifiable;
        Map<String, String> properties = substation.getPropertyNames().stream()
                .map(name -> Map.entry(name, substation.getProperty(name)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return SubstationTabInfos.builder()
                .name(substation.getOptionalName().orElse(null))
                .id(substation.getId())
                .country(mapCountry(substation))
                .properties(properties.isEmpty() ? null : properties)
                .voltageLevels(List.of())
                .voltageLevels(substation.getVoltageLevelStream().map(VoltageLevelInfosMapper::toTabInfos).collect(Collectors.toList()))
                .build();
    }
}
