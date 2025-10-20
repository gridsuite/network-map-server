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
import org.gridsuite.network.map.dto.InfoTypeParameters;
import org.gridsuite.network.map.dto.definition.voltagelevel.VoltageLevelFormInfos;
import org.gridsuite.network.map.dto.definition.voltagelevel.VoltageLevelMapInfos;
import org.gridsuite.network.map.dto.definition.voltagelevel.VoltageLevelTabInfos;
import org.gridsuite.network.map.dto.utils.ElementUtils;
import org.gridsuite.network.map.dto.utils.ExtensionUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.powsybl.iidm.network.TopologyKind.NODE_BREAKER;
import static org.gridsuite.network.map.dto.utils.ElementUtils.*;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
public final class VoltageLevelInfosMapper {
    private VoltageLevelInfosMapper() {
    }

    public static ElementInfos toData(Identifiable<?> identifiable, InfoTypeParameters infoTypeParameters) {
        return switch (infoTypeParameters.getInfoType()) {
            case TAB -> toTabInfos(identifiable);
            case FORM -> toFormInfos(identifiable);
            case LIST -> ElementInfosMapper.toListInfos(identifiable);
            case MAP -> toMapInfos(identifiable);
            default -> throw handleUnsupportedInfoType(infoTypeParameters.getInfoType(), "VoltageLevel");
        };
    }

    static VoltageLevelFormInfos toFormInfos(Identifiable<?> identifiable) {
        VoltageLevel voltageLevel = (VoltageLevel) identifiable;
        VoltageLevelFormInfos voltageLevelFormInfos = VoltageLevelFormInfos.builder()
                .name(voltageLevel.getOptionalName().orElse(null))
                .id(voltageLevel.getId())
                .topologyKind(voltageLevel.getTopologyKind())
                .substationId(voltageLevel.getSubstation().map(Substation::getId).orElse(null))
                .nominalV(voltageLevel.getNominalV())
                .lowVoltageLimit(Double.isNaN(voltageLevel.getLowVoltageLimit()) ? null : voltageLevel.getLowVoltageLimit())
                .highVoltageLimit(Double.isNaN(voltageLevel.getHighVoltageLimit()) ? null : voltageLevel.getHighVoltageLimit())
                .properties(getProperties(voltageLevel))
                .identifiableShortCircuit(ExtensionUtils.toIdentifiableShortCircuit(voltageLevel))
                .build();
        if (voltageLevel.getTopologyKind().equals(TopologyKind.NODE_BREAKER)) {
            VoltageLevelFormInfos barSectionsInfos = getVoltageLevelBusBarSectionsInfos(voltageLevel);
            voltageLevelFormInfos.setBusbarCount(barSectionsInfos.getBusbarCount());
            voltageLevelFormInfos.setSectionCount(barSectionsInfos.getSectionCount());
            voltageLevelFormInfos.setSwitchKinds(barSectionsInfos.getSwitchKinds());
            voltageLevelFormInfos.setIsSymmetrical(barSectionsInfos.getIsSymmetrical());
        }
        return voltageLevelFormInfos;
    }

    static VoltageLevelMapInfos toMapInfos(Identifiable<?> identifiable) {
        VoltageLevel voltageLevel = (VoltageLevel) identifiable;
        return VoltageLevelMapInfos.builder()
                .id(voltageLevel.getId())
                .name(voltageLevel.getOptionalName().orElse(null))
                .substationId(voltageLevel.getSubstation().map(Substation::getId).orElse(null))
                .nominalV(voltageLevel.getNominalV())
                .build();
    }

    static VoltageLevelTabInfos toTabInfos(Identifiable<?> identifiable) {
        VoltageLevel voltageLevel = (VoltageLevel) identifiable;

        VoltageLevelTabInfos.VoltageLevelTabInfosBuilder builder = VoltageLevelTabInfos.builder()
                .id(voltageLevel.getId())
                .name(voltageLevel.getOptionalName().orElse(null))
                .substationId(voltageLevel.getSubstation().map(Substation::getId).orElse(null))
                .nominalV(voltageLevel.getNominalV())
                .country(mapCountry(voltageLevel.getSubstation().orElse(null)))
                .lowVoltageLimit(nullIfNan(voltageLevel.getLowVoltageLimit()))
                .properties(getProperties(voltageLevel))
                .highVoltageLimit(nullIfNan(voltageLevel.getHighVoltageLimit()))
                .substationProperties(voltageLevel.getSubstation().map(ElementUtils::getProperties).orElse(null));
        builder.identifiableShortCircuit(ExtensionUtils.toIdentifiableShortCircuit(voltageLevel));

        return builder.build();
    }

    public static VoltageLevelFormInfos getVoltageLevelBusBarSectionsInfos(VoltageLevel voltageLevel) {
        Map<Integer, Integer> nbSectionsPerBusbar = new HashMap<>();
        int maxBusbarIndex = 1;
        int maxSectionIndex = 1;
        for (BusbarSection bbs : voltageLevel.getNodeBreakerView().getBusbarSections()) {
            var extension = bbs.getExtension(BusbarSectionPosition.class);
            if (extension == null) {
                return VoltageLevelFormInfos.builder()
                        .busbarCount(1)
                        .sectionCount(1)
                        .topologyKind(NODE_BREAKER)
                        .switchKinds(Collections.emptyList())
                        .isSymmetrical(false)
                        .build();
            }
            int busbarIndex = extension.getBusbarIndex();
            int sectionIndex = extension.getSectionIndex();
            maxBusbarIndex = Math.max(maxBusbarIndex, busbarIndex);
            maxSectionIndex = Math.max(maxSectionIndex, sectionIndex);
            nbSectionsPerBusbar.merge(busbarIndex, 1, Integer::sum);
        }

        boolean isSymmetrical = maxBusbarIndex == 1 ||
                nbSectionsPerBusbar.values().stream().distinct().count() == 1
                        && nbSectionsPerBusbar.values().stream().findFirst().orElse(0).equals(maxSectionIndex);

        if (isSymmetrical) {
            return VoltageLevelFormInfos.builder()
                    .busbarCount(maxBusbarIndex)
                    .sectionCount(maxSectionIndex)
                    .switchKinds(Collections.nCopies(maxSectionIndex - 1, SwitchKind.DISCONNECTOR))
                    .isSymmetrical(true)
                    .build();
        }
        return VoltageLevelFormInfos.builder()
                .busbarCount(1)
                .sectionCount(1)
                .topologyKind(NODE_BREAKER)
                .switchKinds(Collections.emptyList())
                .isSymmetrical(false)
                .build();
    }
}
