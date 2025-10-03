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
        List<BusbarSectionResult> withoutSwitch = results.stream().filter(r -> r.allSwitchesClosed).toList();
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
        record NodeState(int depth, SwitchInfo lastSwitch, boolean allClosed) { }
        Map<Integer, List<NodeState>> nodeStates = new HashMap<>();
        nodeStates.put(startNode, List.of(new NodeState(0, null, true)));
        voltageLevel.getNodeBreakerView().getTerminal(startNode).traverse(new Terminal.TopologyTraverser() {
            @Override
            public TraverseResult traverse(Terminal terminal, boolean connected) {
                if (terminal.getVoltageLevel() != voltageLevel) {
                    return TraverseResult.TERMINATE_PATH;
                }
                int currentNode = terminal.getNodeBreakerView().getNode();
                List<NodeState> states = nodeStates.get(currentNode);
                if (terminal.getConnectable() instanceof BusbarSection busbarSection) {
                    if (states != null) {
                        for (NodeState state : states) {
                            results.add(new BusbarSectionResult(busbarSection.getId(), state.depth, state.lastSwitch, state.allClosed));
                        }
                    }
                    return TraverseResult.TERMINATE_PATH;
                }
                return TraverseResult.CONTINUE;
            }

            @Override
            public TraverseResult traverse(Switch aSwitch) {
                int node1 = voltageLevel.getNodeBreakerView().getNode1(aSwitch.getId());
                int node2 = voltageLevel.getNodeBreakerView().getNode2(aSwitch.getId());
                int sourceNode = nodeStates.containsKey(node1) ? node1 : node2;
                int targetNode = nodeStates.containsKey(node1) ? node2 : node1;
                List<NodeState> sourceStates = nodeStates.get(sourceNode);
                if (sourceStates == null) {
                    return TraverseResult.CONTINUE;
                }
                for (NodeState sourceState : sourceStates) {
                    NodeState newState = new NodeState(sourceState.depth + 1, new SwitchInfo(aSwitch.getId(), aSwitch.isOpen()), sourceState.allClosed && !aSwitch.isOpen());
                    nodeStates.computeIfAbsent(targetNode, k -> new ArrayList<>()).add(newState);
                }
                return TraverseResult.CONTINUE;
            }
        }, TraversalType.BREADTH_FIRST);
        return results;
    }
}
