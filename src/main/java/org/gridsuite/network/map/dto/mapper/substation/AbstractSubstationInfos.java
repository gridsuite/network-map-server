/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.mapper.substation;

import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Substation;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.gridsuite.network.map.dto.ElementInfos;
import org.gridsuite.network.map.dto.definition.substation.SubstationFormInfos;
import org.gridsuite.network.map.dto.definition.substation.SubstationListInfos;
import org.gridsuite.network.map.dto.definition.substation.SubstationMapInfos;
import org.gridsuite.network.map.dto.definition.substation.SubstationTabInfos;
import org.gridsuite.network.map.dto.mapper.voltagelevel.AbstractVoltageLevelInfos;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
@SuperBuilder
@Getter
public abstract class AbstractSubstationInfos extends ElementInfos {

    public static ElementInfos toData(Identifiable<?> identifiable, InfoType dataType) {
        switch (dataType) {
            case TAB:
                return toSubstationTabInfos(identifiable);
            case MAP:
                return toSubstationMapInfos(identifiable);
            case FORM:
                return toSubstationFormInfos(identifiable);
            case LIST:
                return toSubstationListInfos(identifiable);
            default:
                throw new UnsupportedOperationException("TODO");
        }
    }

    public static SubstationFormInfos toSubstationFormInfos(Identifiable<?> identifiable) {
        Substation substation = (Substation) identifiable;
        Map<String, String> properties = substation.getPropertyNames().stream()
                .map(name -> Map.entry(name, substation.getProperty(name)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return SubstationFormInfos.builder()
                .name(substation.getOptionalName().orElse(null))
                .id(substation.getId())
                .countryName(substation.getCountry().map(Country::getName).orElse(null))
                .countryCode(substation.getCountry().map(Country::name).orElse(null))
                .properties(properties.isEmpty() ? null : properties)
                .voltageLevels(List.of())
                .voltageLevels(substation.getVoltageLevelStream().map(AbstractVoltageLevelInfos::toVoltageLevelFormInfos).collect(Collectors.toList()))
                .build();
    }

    public static SubstationListInfos toSubstationListInfos(Identifiable<?> identifiable) {
        Substation substation = (Substation) identifiable;
        return SubstationListInfos.builder()
                .id(substation.getId())
                .name(substation.getOptionalName().orElse(null))
                .voltageLevels(substation.getVoltageLevelStream().map(AbstractVoltageLevelInfos::toVoltageLevelListInfos).collect(Collectors.toList()))
                .build();
    }

    public static SubstationMapInfos toSubstationMapInfos(Identifiable<?> identifiable) {
        Substation substation = (Substation) identifiable;
        return SubstationMapInfos.builder()
                .id(substation.getId())
                .name(substation.getOptionalName().orElse(null))
                .voltageLevels(substation.getVoltageLevelStream().map(AbstractVoltageLevelInfos::toVoltageLevelMapInfos).collect(Collectors.toList()))
                .build();
    }

    public static SubstationTabInfos toSubstationTabInfos(Identifiable<?> identifiable) {
        Substation substation = (Substation) identifiable;
        Map<String, String> properties = substation.getPropertyNames().stream()
                .map(name -> Map.entry(name, substation.getProperty(name)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return SubstationTabInfos.builder()
                .name(substation.getOptionalName().orElse(null))
                .id(substation.getId())
                .countryName(substation.getCountry().map(Country::getName).orElse(null))
                .countryCode(substation.getCountry().map(Country::name).orElse(null))
                .properties(properties.isEmpty() ? null : properties)
                .voltageLevels(List.of())
                .voltageLevels(substation.getVoltageLevelStream().map(AbstractVoltageLevelInfos::toVoltageLevelTabInfos).collect(Collectors.toList()))
                .build();
    }
}
