/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.mapper;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.BusbarSectionPosition;
import lombok.Getter;
import lombok.Setter;
import org.gridsuite.network.map.dto.ElementInfos;
import org.gridsuite.network.map.dto.InfoTypeParameters;
import org.gridsuite.network.map.dto.definition.voltagelevel.VoltageLevelFormInfos;
import org.gridsuite.network.map.dto.definition.voltagelevel.VoltageLevelMapInfos;
import org.gridsuite.network.map.dto.definition.voltagelevel.VoltageLevelTabInfos;
import org.gridsuite.network.map.dto.utils.ElementUtils;
import org.gridsuite.network.map.dto.utils.ExtensionUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            default -> throw new UnsupportedOperationException("TODO");
        };
    }

    private static VoltageLevelTopologyInfos getTopologyInfos(VoltageLevel voltageLevel) {
        VoltageLevelTopologyInfos topologyInfos = new VoltageLevelTopologyInfos();
        Map<Integer, Integer> nbSectionsPerBusbar = new HashMap<>();
        for (BusbarSection bbs : voltageLevel.getNodeBreakerView().getBusbarSections()) {
            var extension = bbs.getExtension(BusbarSectionPosition.class);
            if (extension != null) {
                if (extension.getBusbarIndex() > topologyInfos.getBusbarCount()) {
                    topologyInfos.setBusbarCount(extension.getBusbarIndex());
                }
                if (extension.getSectionIndex() > topologyInfos.getSectionCount()) {
                    topologyInfos.setSectionCount(extension.getSectionIndex());
                }
                nbSectionsPerBusbar.putIfAbsent(extension.getBusbarIndex(), 1);
                if (extension.getSectionIndex() > nbSectionsPerBusbar.get(extension.getBusbarIndex())) {
                    nbSectionsPerBusbar.put(extension.getBusbarIndex(), extension.getSectionIndex());
                }
            } else {
                return new VoltageLevelTopologyInfos();
            }
        }
        if (nbSectionsPerBusbar.values().stream().anyMatch(v -> v != topologyInfos.getSectionCount())) { // Non-symmetrical busbars (nb sections)
            return new VoltageLevelTopologyInfos();
        }

        topologyInfos.setRetrievedBusbarSections(true);
        topologyInfos.setSwitchKinds(Collections.nCopies(topologyInfos.getSectionCount() - 1, SwitchKind.DISCONNECTOR));

        return topologyInfos;
    }

    static VoltageLevelFormInfos toFormInfos(Identifiable<?> identifiable) {
        VoltageLevel voltageLevel = (VoltageLevel) identifiable;
        VoltageLevelFormInfos.VoltageLevelFormInfosBuilder<?, ?> builder = VoltageLevelFormInfos.builder()
                .name(voltageLevel.getOptionalName().orElse(null))
                .id(voltageLevel.getId())
                .topologyKind(voltageLevel.getTopologyKind())
                .substationId(voltageLevel.getSubstation().map(Substation::getId).orElse(null))
                .nominalV(voltageLevel.getNominalV())
                .lowVoltageLimit(Double.isNaN(voltageLevel.getLowVoltageLimit()) ? null : voltageLevel.getLowVoltageLimit())
                .highVoltageLimit(Double.isNaN(voltageLevel.getHighVoltageLimit()) ? null : voltageLevel.getHighVoltageLimit())
                .properties(getProperties(voltageLevel));

        if (voltageLevel.getTopologyKind().equals(TopologyKind.NODE_BREAKER)) {
            VoltageLevelTopologyInfos vlTopologyInfos = getTopologyInfos(voltageLevel);
            builder.busbarCount(vlTopologyInfos.getBusbarCount());
            builder.sectionCount(vlTopologyInfos.getSectionCount());
            builder.switchKinds(vlTopologyInfos.getSwitchKinds());
            builder.isRetrievedBusbarSections(vlTopologyInfos.isRetrievedBusbarSections());
        }

        builder.identifiableShortCircuit(ExtensionUtils.toIdentifiableShortCircuit(voltageLevel));

        return builder.build();
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

    @Getter
    @Setter
    public static class VoltageLevelTopologyInfos {
        private boolean isRetrievedBusbarSections = false;
        private int busbarCount = 1;
        private int sectionCount = 1;
        private List<SwitchKind> switchKinds = List.of();
    }
}
