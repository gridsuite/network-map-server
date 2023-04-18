/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.substation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Substation;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.gridsuite.network.map.dto.voltagelevel.VoltageLevelTabInfos;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
@SuperBuilder
@Getter
public class SubstationTabInfos extends SubstationInfos {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String countryName;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String countryCode;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, String> properties;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<VoltageLevelTabInfos> voltageLevels;

    public static SubstationTabInfos toData(Identifiable<?> identifiable) {
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
                .voltageLevels(substation.getVoltageLevelStream().map(VoltageLevelTabInfos::toData).collect(Collectors.toList()))
                .build();
    }
}
