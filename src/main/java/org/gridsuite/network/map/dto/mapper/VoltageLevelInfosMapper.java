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
import org.gridsuite.network.map.dto.InfoTypesParatters;
import org.gridsuite.network.map.dto.definition.voltagelevel.VoltageLevelFormInfos;
import org.gridsuite.network.map.dto.definition.voltagelevel.VoltageLevelListInfos;
import org.gridsuite.network.map.dto.definition.voltagelevel.VoltageLevelMapInfos;
import org.gridsuite.network.map.dto.definition.voltagelevel.VoltageLevelTabInfos;

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

    public static ElementInfos toData(Identifiable<?> identifiable, InfoTypesParatters dataType) {
        switch (dataType.getInfoType()) {
            case TAB:
                return toTabInfos(identifiable);
            case FORM:
                return toFormInfos(identifiable);
            case LIST:
                return toListInfos(identifiable);
            default:
                throw new UnsupportedOperationException("TODO");
        }
    }

    public static VoltageLevelTopologyInfos getTopologyInfos(VoltageLevel voltageLevel) {
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
        if (nbSectionsPerBusbar.values().stream().anyMatch(v -> v != topologyInfos.sectionCount)) { // Non-symmetrical busbars (nb sections)
            return new VoltageLevelTopologyInfos();
        }

        topologyInfos.setRetrievedBusbarSections(true);
        topologyInfos.setSwitchKinds(Collections.nCopies(topologyInfos.getSectionCount() - 1, SwitchKind.DISCONNECTOR));

        return topologyInfos;
    }

    protected static VoltageLevelFormInfos toFormInfos(Identifiable<?> identifiable) {
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

        builder.identifiableShortCircuit(toIdentifiableShortCircuit(voltageLevel));

        return builder.build();
    }

    protected static VoltageLevelListInfos toListInfos(Identifiable<?> identifiable) {
        VoltageLevel voltageLevel = (VoltageLevel) identifiable;
        return VoltageLevelListInfos.builder()
                .name(voltageLevel.getOptionalName().orElse(null))
                .id(voltageLevel.getId())
                .substationId(voltageLevel.getSubstation().map(Substation::getId).orElse(null))
                .topologyKind(voltageLevel.getTopologyKind())
                .build();
    }

    protected static VoltageLevelMapInfos toMapInfos(Identifiable<?> identifiable) {
        VoltageLevel voltageLevel = (VoltageLevel) identifiable;
        return VoltageLevelMapInfos.builder()
                .id(voltageLevel.getId())
                .name(voltageLevel.getOptionalName().orElse(null))
                .substationId(voltageLevel.getSubstation().map(Substation::getId).orElse(null))
                .nominalV(voltageLevel.getNominalV())
                .build();
    }

    protected static VoltageLevelTabInfos toTabInfos(Identifiable<?> identifiable) {
        VoltageLevel voltageLevel = (VoltageLevel) identifiable;

        VoltageLevelTabInfos.VoltageLevelTabInfosBuilder builder = VoltageLevelTabInfos.builder()
                .id(voltageLevel.getId())
                .name(voltageLevel.getOptionalName().orElse(null))
                .substationId(voltageLevel.getSubstation().orElseThrow().getId())
                .nominalV(voltageLevel.getNominalV())
                .country(mapCountry(voltageLevel.getSubstation().orElse(null)))
                .lowVoltageLimit(nullIfNan(voltageLevel.getLowVoltageLimit()))
                .properties(getProperties(voltageLevel))
                .highVoltageLimit(nullIfNan(voltageLevel.getHighVoltageLimit()));
        builder.identifiableShortCircuit(toIdentifiableShortCircuit(voltageLevel));

        return builder.build();
    }

    @Getter
    @Setter
    public static class VoltageLevelTopologyInfos {
        boolean isRetrievedBusbarSections = false;
        int busbarCount = 1;
        int sectionCount = 1;
        List<SwitchKind> switchKinds = List.of();
    }
}
