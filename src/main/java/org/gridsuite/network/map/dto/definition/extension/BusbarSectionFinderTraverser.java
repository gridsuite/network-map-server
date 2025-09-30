/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.definition.extension;

import com.powsybl.iidm.network.*;

import java.util.*;

/**
 * @author Ghazwa Rehili <ghazwa.rehili at rte-france.com>
 */
// TODO : to remove when this class is available in network-store
public final class BusbarSectionFinderTraverser {

    private BusbarSectionFinderTraverser() {
        throw new UnsupportedOperationException();
    }

    private record NodePath(int node, List<SwitchInfo> pathSwitches, SwitchInfo lastSwitch) { }

    public record SwitchInfo(String id, SwitchKind kind, boolean isOpen) { }

    public record BusbarSectionResult(String busbarSectionId, int depth, SwitchInfo lastSwitch) { }

    public static String findBusbarSectionId(Terminal terminal) {
        BusbarSectionResult result = getBusbarSectionResult(terminal);
        return result != null ? result.busbarSectionId() : null;
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
            return withoutSwitch.stream().min(Comparator.comparingInt(BusbarSectionResult::depth)).orElse(null);
        }
        List<BusbarSectionResult> withClosedSwitch = results.stream().filter(r -> r.lastSwitch() != null && !r.lastSwitch().isOpen()).toList();
        if (!withClosedSwitch.isEmpty()) {
            return withClosedSwitch.stream().min(Comparator.comparingInt(BusbarSectionResult::depth)).orElse(null);
        }
        List<BusbarSectionResult> withOpenSwitch = results.stream().filter(r -> r.lastSwitch() != null && r.lastSwitch().isOpen()).toList();
        if (!withOpenSwitch.isEmpty()) {
            return withOpenSwitch.stream().min(Comparator.comparingInt(BusbarSectionResult::depth)).orElse(null);
        }
        return results.getFirst();
    }

    private static List<BusbarSectionResult> searchAllBusbars(VoltageLevel.NodeBreakerView view, int startNode) {
        List<BusbarSectionResult> results = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();
        Queue<NodePath> queue = new LinkedList<>();
        queue.offer(new NodePath(startNode, new ArrayList<>(), null));
        while (!queue.isEmpty()) {
            NodePath currentNodePath = queue.poll();
            if (!hasNotBeenVisited(currentNodePath.node(), visited)) {
                continue;
            }
            visited.add(currentNodePath.node());
            Optional<BusbarSectionResult> busbarSectionResult = tryCreateBusbarResult(view, currentNodePath);
            if (busbarSectionResult.isPresent()) {
                results.add(busbarSectionResult.get());
            } else {
                exploreAdjacentNodes(view, currentNodePath, visited, queue);
            }
        }
        return results;
    }

    private static boolean hasNotBeenVisited(int node, Set<Integer> visited) {
        return !visited.contains(node);
    }

    private static Optional<BusbarSectionResult> tryCreateBusbarResult(VoltageLevel.NodeBreakerView view, NodePath currentNodePath) {
        Optional<Terminal> nodeTerminal = view.getOptionalTerminal(currentNodePath.node());
        if (nodeTerminal.isEmpty()) {
            return Optional.empty();
        }
        Terminal term = nodeTerminal.get();
        if (term.getConnectable().getType() == IdentifiableType.BUSBAR_SECTION) {
            String busbarSectionId = term.getConnectable().getId();
            int depth = currentNodePath.pathSwitches().size();
            SwitchInfo lastSwitch = currentNodePath.lastSwitch();
            return Optional.of(new BusbarSectionResult(busbarSectionId, depth, lastSwitch));
        }
        return Optional.empty();
    }

    private static void exploreAdjacentNodes(VoltageLevel.NodeBreakerView view, NodePath currentNodePath, Set<Integer> visited, Queue<NodePath> queue) {
        view.getSwitchStream().forEach(sw -> {
            int node1 = view.getNode1(sw.getId());
            int node2 = view.getNode2(sw.getId());
            Optional<Integer> nextNode = getNextNodeIfAdjacent(currentNodePath.node(), node1, node2);
            if (nextNode.isPresent() && !visited.contains(nextNode.get())) {
                NodePath newPath = createNodePath(currentNodePath, sw, nextNode.get());
                queue.offer(newPath);
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
        List<SwitchInfo> newPathSwitches = new ArrayList<>(currentNodePath.pathSwitches());
        SwitchInfo switchInfo = new SwitchInfo(sw.getId(), sw.getKind(), sw.isOpen());
        newPathSwitches.add(switchInfo);
        return new NodePath(nextNode, newPathSwitches, switchInfo);
    }
}
