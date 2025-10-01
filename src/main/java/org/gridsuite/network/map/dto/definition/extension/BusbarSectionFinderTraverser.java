/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.definition.extension;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.BusbarSectionPosition;
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

    private record NodePath(int startNode, List<SwitchInfo> traversedSwitches, SwitchInfo lastSwitch) { }

    public record SwitchInfo(String id, boolean isOpen) { }

    public record BusbarSectionResult(String busbarSectionId, int depth, SwitchInfo lastSwitch) { }

    public static String findBusbarSectionId(Terminal terminal) {
        BusbarSectionResult result = getBusbarSectionResult(terminal);
        return result != null ? result.busbarSectionId() : terminal.getVoltageLevel().getNodeBreakerView().getBusbarSections().iterator().next().getId();
    }

    public static BusbarSectionResult getBusbarSectionResult(Terminal terminal) {
        VoltageLevel.NodeBreakerView view = terminal.getVoltageLevel().getNodeBreakerView();
        int startNode = terminal.getNodeBreakerView().getNode();
        List<BusbarSectionResult> allResults = searchAllBusbars(view, startNode);
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

    private static List<BusbarSectionResult> searchAllBusbars(VoltageLevel.NodeBreakerView view, int startNode) {
        List<BusbarSectionResult> results = new ArrayList<>();
//        Set<Integer> visited = new HashSet<>();
//        Queue<NodePath> nodePathsToVisit = new LinkedList<>();
//        nodePathsToVisit.offer(new NodePath(startNode, new ArrayList<>(), null));


        view.getTerminal(startNode).traverse(new Terminal.TopologyTraverser() {
            int currentDepth = 0;
            SwitchInfo lastSwitch = null;
            @Override
            public TraverseResult traverse(Terminal terminal, boolean connected) {
                //if (terminal.getVoltageLevel() != view.

                if (terminal.getConnectable() instanceof BusbarSection busbarSection) {
                    // add busbar section to the path
                    results.add(new BusbarSectionResult(busbarSection.getId(), currentDepth, lastSwitch));
                    return TraverseResult.TERMINATE_PATH;
                }
//                currentDepth++;
                return TraverseResult.CONTINUE;
            }

            @Override
            public TraverseResult traverse(Switch aSwitch) {
                currentDepth++;
                lastSwitch = new SwitchInfo(aSwitch.getId(), aSwitch.isOpen());
                return TraverseResult.CONTINUE;
            }
        }, TraversalType.BREADTH_FIRST);

//        while (!nodePathsToVisit.isEmpty()) {
//            NodePath currentNodePath = nodePathsToVisit.poll();
//            if (hasBeenVisited(currentNodePath.startNode(), visited)) {
//                continue;
//            }
//            visited.add(currentNodePath.startNode());
//            Optional<BusbarSectionResult> busbarSectionResult = findBusbarSectionAtNode(view, currentNodePath);
//            if (busbarSectionResult.isPresent()) {
//                results.add(busbarSectionResult.get());
//            } else {
////                exploreAdjacentNodes(view, currentNodePath, visited, nodePathsToVisit);
//            }
//        }
        return results;
    }

    private static boolean hasBeenVisited(int node, Set<Integer> visited) {
        return visited.contains(node);
    }

    private static Optional<BusbarSectionResult> findBusbarSectionAtNode(VoltageLevel.NodeBreakerView view, NodePath currentNodePath) {
        Optional<Terminal> nodeTerminal = view.getOptionalTerminal(currentNodePath.startNode());
        if (nodeTerminal.isEmpty()) {
            return Optional.empty();
        }
        Terminal terminal = nodeTerminal.get();
        if (terminal.getConnectable().getType() == IdentifiableType.BUSBAR_SECTION) {
            String busbarSectionId = terminal.getConnectable().getId();
            int depth = currentNodePath.traversedSwitches().size();
            SwitchInfo lastSwitch = currentNodePath.lastSwitch();
            BusbarSection busbarSection = (BusbarSection) terminal.getConnectable();
            int busbarIndex = 1;
            int sectionIndex = 1;
            var busbarSectionPosition = busbarSection.getExtension(BusbarSectionPosition.class);
            if (busbarSectionPosition != null) {
                busbarIndex = busbarSectionPosition.getBusbarIndex();
                sectionIndex = busbarSectionPosition.getSectionIndex();
            }
            return Optional.of(new BusbarSectionResult(busbarSectionId, depth, lastSwitch));
        }
        return Optional.empty();
    }

    private static void exploreAdjacentNodes(VoltageLevel.NodeBreakerView view, NodePath currentNodePath, Set<Integer> visited, Queue<NodePath> nodePathsToVisit) {
        view.getSwitchStream().forEach(sw -> {
            int node1 = view.getNode1(sw.getId());
            int node2 = view.getNode2(sw.getId());
            Optional<Integer> nextNode = getNextNodeIfAdjacent(currentNodePath.startNode(), node1, node2);
            if (nextNode.isPresent() && !visited.contains(nextNode.get())) {
                NodePath newNodePath = createNodePath(currentNodePath, sw, nextNode.get());
                nodePathsToVisit.offer(newNodePath);
            }
        });
    }

    private static Optional<Integer> getNextNodeIfAdjacent(int currentNode, int node1, int node2) {
        if (node1 == currentNode) {
            return Optional.of(node2);
        }
        if (node2 == currentNode) {
            return Optional.of(node1);
        }
        return Optional.empty();
    }

    private static NodePath createNodePath(NodePath currentNodePath, Switch sw, int nextNode) {
        List<SwitchInfo> newPathSwitches = new ArrayList<>(currentNodePath.traversedSwitches());
        SwitchInfo switchInfo = new SwitchInfo(sw.getId(), sw.isOpen());
        newPathSwitches.add(switchInfo);
        return new NodePath(nextNode, newPathSwitches, switchInfo);
    }
}
