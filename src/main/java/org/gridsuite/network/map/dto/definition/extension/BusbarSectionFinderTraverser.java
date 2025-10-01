/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.definition.extension;

import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.math.graph.TraversalType;
import com.powsybl.math.graph.TraverseResult;

import java.util.*;

/**
 * @author Ghazwa Rehili <ghazwa.rehili at rte-france.com>
 */
// TODO : to remove when this class is available in network-store
public final class BusbarSectionFinderTraverser {

    private BusbarSectionFinderTraverser() {
        throw new UnsupportedOperationException();
    }

    public record SwitchInfo(String id, boolean isOpen) { }

    public record BusbarSectionResult(String busbarSectionId, int depth, SwitchInfo lastSwitch, boolean allSwitchesClosed) { }

    public static String findBusbarSectionId(Terminal terminal) {
        BusbarSectionResult result = getBusbarSectionResult(terminal);
        return result != null ? result.busbarSectionId() : terminal.getVoltageLevel().getNodeBreakerView().getBusbarSections().iterator().next().getId();
    }

    public static BusbarSectionResult getBusbarSectionResult(Terminal terminal) {
        int startNode = terminal.getNodeBreakerView().getNode();
        List<BusbarSectionResult> allResults = searchAllBusbars(terminal.getVoltageLevel(), startNode);
        if (allResults.isEmpty()) {
            return null;
        }
        return selectBestBusbar(allResults);
    }

    private static BusbarSectionResult selectBestBusbar(List<BusbarSectionResult> results) {
        List<BusbarSectionResult> withoutSwitch = results.stream().filter(r -> r.lastSwitch() == null).toList();
        if (!withoutSwitch.isEmpty()) {
            return withoutSwitch.stream().min(Comparator.comparingInt(BusbarSectionResult::depth)
                    .thenComparing(BusbarSectionResult::busbarSectionId)).orElse(null);
        }
        List<BusbarSectionResult> withClosedSwitch = results.stream().filter(r -> r.lastSwitch() != null && !r.lastSwitch().isOpen()).toList();
        if (!withClosedSwitch.isEmpty()) {
            return withClosedSwitch.stream().min(Comparator.comparingInt(BusbarSectionResult::depth)
                    .thenComparing(BusbarSectionResult::busbarSectionId)).orElse(null);
        }
        List<BusbarSectionResult> withOpenSwitch = results.stream().filter(r -> r.lastSwitch() != null && r.lastSwitch().isOpen()).toList();
        if (!withOpenSwitch.isEmpty()) {
            return withOpenSwitch.stream().min(Comparator.comparingInt(BusbarSectionResult::depth)
                    .thenComparing(BusbarSectionResult::busbarSectionId)).orElse(null);
        }
        return results.getFirst();
    }

    private static List<BusbarSectionResult> searchAllBusbars(VoltageLevel voltageLevel, int startNode) {
        List<BusbarSectionResult> results = new ArrayList<>();
        Map<Integer, Integer> nodeDepths = new HashMap<>();
        Map<Integer, SwitchInfo> nodeLastSwitch = new HashMap<>();
        Map<Integer, Boolean> nodeAllClosed = new HashMap<>();
        nodeDepths.put(startNode, 0);
        nodeLastSwitch.put(startNode, null);
        nodeAllClosed.put(startNode, true);
        voltageLevel.getNodeBreakerView().getTerminal(startNode).traverse(new Terminal.TopologyTraverser() {
            @Override
            public TraverseResult traverse(Terminal terminal, boolean connected) {
                if (terminal.getVoltageLevel() != voltageLevel) {
                    return TraverseResult.TERMINATE_PATH;
                }
                int currentNode = terminal.getNodeBreakerView().getNode();
                if (terminal.getConnectable() instanceof BusbarSection busbarSection) {
                    results.add(new BusbarSectionResult(
                            busbarSection.getId(),
                            nodeDepths.getOrDefault(currentNode, 0),
                            nodeLastSwitch.get(currentNode),
                            nodeAllClosed.getOrDefault(currentNode, true)
                    ));
                    return TraverseResult.TERMINATE_PATH;
                }
                return TraverseResult.CONTINUE;
            }

            @Override
            public TraverseResult traverse(Switch aSwitch) {
                int node1 = voltageLevel.getNodeBreakerView().getNode1(aSwitch.getId());
                int node2 = voltageLevel.getNodeBreakerView().getNode2(aSwitch.getId());
                int sourceNode = nodeDepths.containsKey(node1) ? node1 : node2;
                int targetNode = nodeDepths.containsKey(node1) ? node2 : node1;
                int newDepth = nodeDepths.get(sourceNode) + 1;
                SwitchInfo newLastSwitch = new SwitchInfo(aSwitch.getId(), aSwitch.isOpen());
                boolean newAllClosed = nodeAllClosed.get(sourceNode) && !aSwitch.isOpen();

                nodeDepths.put(targetNode, newDepth);
                nodeLastSwitch.put(targetNode, newLastSwitch);
                nodeAllClosed.put(targetNode, newAllClosed);
                return TraverseResult.CONTINUE;
            }
        }, TraversalType.BREADTH_FIRST);
        return results;
    }
}
