/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.voltagelevel;

import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.SwitchKind;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.extensions.BusbarSectionPosition;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.gridsuite.network.map.dto.ElementInfos;

import java.util.*;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
@SuperBuilder
@Getter
public abstract class AbstractVoltageLevelInfos extends ElementInfos {

    public static ElementInfos toData(Identifiable<?> identifiable, ElementInfoType dataType) {
        switch (dataType.getInfoType()) {
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
}
