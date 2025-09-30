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
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
// TODO : to remove when this class is available in network-store
public final class BusbarSectionFinderTraverser {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private BusbarSectionFinderTraverser() {
        throw new UnsupportedOperationException();
    }

    /**
     * Finds the best busbar section connected to the given terminal.
     * Uses a breadth-first search algorithm to explore all possible paths.
     *
     * @param terminal the starting terminal
     * @return the best busbar result according to selection criteria, or null if none found
     */
    public static BusbarSectionResult findBestBusbar(Terminal terminal) {
        VoltageLevel.NodeBreakerView view = terminal.getVoltageLevel().getNodeBreakerView();
        int startNode = terminal.getNodeBreakerView().getNode();
        List<BusbarSectionResult> allResults = searchAllBusbars(view, startNode);
        if (allResults.isEmpty()) {
            return null;
        }
        return selectBestBusbar(allResults);
    }

    /**
     * Selects the best busbar from a list of candidates using a priority-based approach:
     * Priority 1: Busbar with closed last switch (minimum depth, then minimum switches before last)
     * Priority 2: Busbar with open last switch (minimum depth, then minimum switches before last)
     * Priority 3: Busbar without switch (direct connection, minimum depth)
     *
     * @param results list of all found busbar results
     * @return the best busbar according to selection criteria
     */
    private static BusbarSectionResult selectBestBusbar(List<BusbarSectionResult> results) {
        // Priority 1: Search for busbar with closed last switch
        List<BusbarSectionResult> withClosedSwitch = results.stream().filter(r -> r.lastSwitch() != null && !r.lastSwitch().isOpen()).toList();
        if (!withClosedSwitch.isEmpty()) {
            return withClosedSwitch.stream().min(Comparator.comparingInt(BusbarSectionResult::depth).thenComparingInt(BusbarSectionResult::switchesBeforeLast)).orElse(null);
        }

        // Priority 2: Search for busbar with open last switch
        List<BusbarSectionResult> withOpenSwitch = results.stream().filter(r -> r.lastSwitch() != null && r.lastSwitch().isOpen()).toList();
        if (!withOpenSwitch.isEmpty()) {
            return withOpenSwitch.stream().min(Comparator.comparingInt(BusbarSectionResult::depth).thenComparingInt(BusbarSectionResult::switchesBeforeLast)).orElse(null);
        }

        // Priority 3: Busbars without switch direct connection
        List<BusbarSectionResult> withoutSwitch = results.stream().filter(r -> r.lastSwitch() == null).toList();
        if (!withoutSwitch.isEmpty()) {
            return withoutSwitch.stream().min(Comparator.comparingInt(BusbarSectionResult::depth)).orElse(null);
        }

        // Fallback: select first busbar
        return results.getFirst();
    }

    /**
     * Searches all accessible busbars from a starting node using breadth-first search.
     * Explores the node-breaker topology through switches.
     *
     * @param view the node-breaker view of the voltage level
     * @param startNode the starting node index
     * @return list of all busbar results found
     */
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
        // Check if current node is a busbar section
        if (term.getConnectable().getType() == IdentifiableType.BUSBAR_SECTION) {
            String busbarSectionId = term.getConnectable().getId();
            int depth = currentNodePath.pathSwitches().size();
            SwitchInfo lastSwitch = currentNodePath.lastSwitch();
            int switchesBeforeLast = lastSwitch != null ? (depth - 1) : 0;
            return Optional.of(new BusbarSectionResult(busbarSectionId, depth, switchesBeforeLast, lastSwitch));
        }
        return Optional.empty();
    }

    private static void exploreAdjacentNodes(VoltageLevel.NodeBreakerView view, NodePath currentNodePath, Set<Integer> visited, Queue<NodePath> queue) {
        view.getSwitchStream().forEach(sw -> {
            int node1 = view.getNode1(sw.getId());
            int node2 = view.getNode2(sw.getId());
            Optional<Integer> nextNode = getNextNodeIfAdjacent(currentNodePath.node(), node1, node2);
            if (nextNode.isPresent() && !visited.contains(nextNode.get())) {
                NodePath newPath = createNodePath(currentNodePath, sw, node1, node2, nextNode.get());
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

    private static NodePath createNodePath(NodePath currentNodePath, Switch sw, int node1, int node2, int nextNode) {
        List<SwitchInfo> newPathSwitches = new ArrayList<>(currentNodePath.pathSwitches());
        SwitchInfo switchInfo = new SwitchInfo(sw.getId(), sw.getKind(), sw.isOpen(), node1, node2);
        newPathSwitches.add(switchInfo);
        return new NodePath(nextNode, newPathSwitches, switchInfo);
    }

    /**
     * Internal record to track the path during graph traversal.
     */
    private record NodePath(int node, List<SwitchInfo> pathSwitches, SwitchInfo lastSwitch) { }

    /**
     * Record containing information about a switch in the topology.
     */
    public record SwitchInfo(String id, SwitchKind kind, boolean isOpen, int node1, int node2) { }

    /**
     * Record containing the result of a busbar search with selection metadata.
     */
    public record BusbarSectionResult(String busbarSectionId, int depth, int switchesBeforeLast, SwitchInfo lastSwitch) { }

    /**
     * Convenience method to get only the busbar ID.
     *
     * @param terminal the starting terminal
     * @return the busbar ID or null if none found
     */
    public static String findBusbarSectionId(Terminal terminal) {
        BusbarSectionResult result = findBestBusbar(terminal);
        return result != null ? result.busbarSectionId() : null;
    }
}
