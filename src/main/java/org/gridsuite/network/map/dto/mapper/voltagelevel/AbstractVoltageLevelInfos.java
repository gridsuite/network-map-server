/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.mapper.voltagelevel;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.BusbarSectionPosition;
import com.powsybl.iidm.network.extensions.IdentifiableShortCircuit;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.gridsuite.network.map.dto.ElementInfos;
import org.gridsuite.network.map.dto.definition.voltagelevel.VoltageLevelFormInfos;
import org.gridsuite.network.map.dto.definition.voltagelevel.VoltageLevelListInfos;
import org.gridsuite.network.map.dto.definition.voltagelevel.VoltageLevelMapInfos;
import org.gridsuite.network.map.dto.definition.voltagelevel.VoltageLevelTabInfos;

import java.util.*;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
@SuperBuilder
@Getter
public abstract class AbstractVoltageLevelInfos extends ElementInfos {

    public static ElementInfos toData(Identifiable<?> identifiable, InfoType dataType) {
        switch (dataType) {
            case TAB:
                return VoltageLevelTabInfos.toData(identifiable);
            case FORM:
                return VoltageLevelFormInfos.toData(identifiable);
            case LIST:
                return VoltageLevelListInfos.toData(identifiable);
            default:
                throw new UnsupportedOperationException("TODO");
        }
    }

    @Getter
    @Setter
    public static class VoltageLevelTopologyInfos {
        boolean isRetrievedBusbarSections = false;
        int busbarCount = 1;
        int sectionCount = 1;
        List<SwitchKind> switchKinds = List.of();
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

    public static VoltageLevelFormInfos toVoltageLevelFormInfos(Identifiable<?> identifiable) {
        VoltageLevel voltageLevel = (VoltageLevel) identifiable;
        VoltageLevelFormInfos.VoltageLevelFormInfosBuilder builder = VoltageLevelFormInfos.builder()
                .name(voltageLevel.getOptionalName().orElse(null))
                .id(voltageLevel.getId())
                .topologyKind(voltageLevel.getTopologyKind())
                .substationId(voltageLevel.getSubstation().map(Substation::getId).orElse(null))
                .nominalVoltage(voltageLevel.getNominalV())
                .lowVoltageLimit(Double.isNaN(voltageLevel.getLowVoltageLimit()) ? null : voltageLevel.getLowVoltageLimit())
                .highVoltageLimit(Double.isNaN(voltageLevel.getHighVoltageLimit()) ? null : voltageLevel.getHighVoltageLimit());

        if (voltageLevel.getTopologyKind().equals(TopologyKind.NODE_BREAKER)) {
            VoltageLevelTopologyInfos vlTopologyInfos = getTopologyInfos(voltageLevel);
            builder.busbarCount(vlTopologyInfos.getBusbarCount());
            builder.sectionCount(vlTopologyInfos.getSectionCount());
            builder.switchKinds(vlTopologyInfos.getSwitchKinds());
            builder.isRetrievedBusbarSections(vlTopologyInfos.isRetrievedBusbarSections());
        }

        IdentifiableShortCircuit identifiableShortCircuit = voltageLevel.getExtension(IdentifiableShortCircuit.class);
        if (identifiableShortCircuit != null) {
            builder.ipMin(identifiableShortCircuit.getIpMin());
            builder.ipMax(identifiableShortCircuit.getIpMax());
        }

        return builder.build();
    }

    public static VoltageLevelListInfos toVoltageLevelListInfos(Identifiable<?> identifiable) {
        VoltageLevel voltageLevel = (VoltageLevel) identifiable;
        return VoltageLevelListInfos.builder()
                .name(voltageLevel.getOptionalName().orElse(null))
                .id(voltageLevel.getId())
                .substationId(voltageLevel.getSubstation().map(Substation::getId).orElse(null))
                .topologyKind(voltageLevel.getTopologyKind())
                .build();
    }

    public static VoltageLevelMapInfos toVoltageLevelMapInfos(Identifiable<?> identifiable) {
        VoltageLevel voltageLevel = (VoltageLevel) identifiable;
        return VoltageLevelMapInfos.builder()
                .id(voltageLevel.getId())
                .name(voltageLevel.getOptionalName().orElse(null))
                .substationId(voltageLevel.getSubstation().map(Substation::getId).orElse(null))
                .nominalVoltage(voltageLevel.getNominalV())
                .build();
    }

    public static VoltageLevelTabInfos toVoltageLevelTabInfos(Identifiable<?> identifiable) {
        VoltageLevel voltageLevel = (VoltageLevel) identifiable;
        return VoltageLevelTabInfos.builder()
                .id(voltageLevel.getId())
                .name(voltageLevel.getOptionalName().orElse(null))
                .substationId(voltageLevel.getSubstation().orElseThrow().getId())
                .nominalVoltage(voltageLevel.getNominalV())
                .build();
    }
}
