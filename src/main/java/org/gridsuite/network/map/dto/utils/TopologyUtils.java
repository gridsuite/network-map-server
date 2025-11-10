/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.utils;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.BusbarSectionPosition;
import org.gridsuite.network.map.dto.definition.busbarsection.BusBarSectionFormInfos;
import org.gridsuite.network.map.dto.definition.topology.BusBarSectionsInfos;
import org.gridsuite.network.map.dto.definition.topology.FeederBayInfos;
import org.gridsuite.network.map.dto.definition.topology.SwitchInfos;

import java.util.*;
import java.util.stream.Collectors;

import static com.powsybl.iidm.network.Terminal.getConnectableSide;
import static org.gridsuite.network.map.dto.utils.ElementUtils.getBusOrBusbarSection;
import static org.gridsuite.network.map.dto.utils.ElementUtils.getConnectablePosition;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
public final class TopologyUtils {

    private TopologyUtils() { }

    public static BusBarSectionsInfos getBusBarSectionsInfos(VoltageLevel voltageLevel) {
        Map<Integer, Integer> nbSectionsPerBusbar = new HashMap<>();
        List<BusBarSectionFormInfos> busbarSectionInfos = new ArrayList<>();
        BusBarSectionsInfos busBarSectionsInfos = BusBarSectionsInfos.builder()
                .isBusbarSectionPositionFound(true)
                .isSymmetrical(false)
                .build();
        int maxBusbarIndex = 1;
        int maxSectionIndex = 1;
        for (BusbarSection bbs : voltageLevel.getNodeBreakerView().getBusbarSections()) {
            var extension = bbs.getExtension(BusbarSectionPosition.class);
            if (extension == null) {
                busBarSectionsInfos.setIsBusbarSectionPositionFound(false);
                break;
            }
            int busbarIndex = extension.getBusbarIndex();
            int sectionIndex = extension.getSectionIndex();
            maxBusbarIndex = Math.max(maxBusbarIndex, busbarIndex);
            maxSectionIndex = Math.max(maxSectionIndex, sectionIndex);
            nbSectionsPerBusbar.merge(busbarIndex, 1, Integer::sum);
            busbarSectionInfos.add(BusBarSectionFormInfos.builder()
                    .id(bbs.getId())
                    .vertPos(sectionIndex)
                    .horizPos(busbarIndex)
                    .build());
        }

        busBarSectionsInfos.setBusBarSections(busbarSectionInfos.stream()
                .collect(Collectors.groupingBy(
                        section -> String.valueOf(section.getHorizPos()),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> list.stream()
                                        .sorted(Comparator.comparing(BusBarSectionFormInfos::getVertPos))
                                        .map(BusBarSectionFormInfos::getId)
                                        .toList()
                        )
                )));

        boolean isSymmetrical = maxBusbarIndex == 1 ||
                nbSectionsPerBusbar.values().stream().distinct().count() == 1
                        && nbSectionsPerBusbar.values().stream().findFirst().orElse(0).equals(maxSectionIndex);

        if (isSymmetrical) {
            busBarSectionsInfos.setIsSymmetrical(true);
        }
        busBarSectionsInfos.setTopologyKind(voltageLevel.getTopologyKind());
        return busBarSectionsInfos;
    }

    public static Map<String, List<FeederBayInfos>> getFeederBaysInfos(VoltageLevel voltageLevel) {
        Map<String, List<FeederBayInfos>> feederBayInfos = new HashMap<>();
        String currentVoltageLevelId = voltageLevel.getId();
        voltageLevel.getConnectableStream()
                .filter(connectable -> !(connectable instanceof BusbarSection))
                .forEach(connectable -> {
                    List<FeederBayInfos> connections = new ArrayList<>();
                    for (Object obj : connectable.getTerminals()) {
                        Terminal terminal = (Terminal) obj;
                        if (terminal.getVoltageLevel().getId().equals(currentVoltageLevelId)) {
                            connections.add(new FeederBayInfos(
                                    getBusOrBusbarSection(terminal),
                                    getConnectablePosition(connectable, ElementUtils.FeederSide.from(getConnectableSide(terminal))),
                                    getConnectableSide(terminal).map(ThreeSides::toTwoSides).orElse(null)
                            ));
                        }
                    }
                    feederBayInfos.put(connectable.getId(), connections);
                });
        return feederBayInfos;
    }

    public static List<SwitchInfos> getSwitchesInfos(String voltageLevelId, Network network) {
        List<SwitchInfos> switchInfosList = new ArrayList<>();
        network.getVoltageLevel(voltageLevelId).getSwitches().forEach(sw ->
                switchInfosList.add(SwitchInfos.builder()
                        .id(sw.getId())
                        .open(sw.isOpen())
                        .build()));
        return switchInfosList;
    }
}
