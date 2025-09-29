/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.definition.extension;

import com.powsybl.iidm.network.IdentifiableType;
import com.powsybl.iidm.network.SwitchKind;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.VoltageLevel;

import java.util.*;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
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
    public static BusbarResult findBestBusbar(Terminal terminal) {
        VoltageLevel.NodeBreakerView view = terminal.getVoltageLevel().getNodeBreakerView();
        int startNode = terminal.getNodeBreakerView().getNode();
        List<BusbarResult> allResults = searchAllBusbars(view, startNode);
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
    private static BusbarResult selectBestBusbar(List<BusbarResult> results) {
        // Priority 1: Search for busbar with closed last switch
        List<BusbarResult> withClosedSwitch = results.stream().filter(r -> r.lastSwitch() != null && !r.lastSwitch().isOpen()).toList();
        if (!withClosedSwitch.isEmpty()) {
            BusbarResult best = withClosedSwitch.stream().min(Comparator.comparingInt(BusbarResult::depth)
                    .thenComparingInt(BusbarResult::switchesBeforeLast))
                    .get();
            return best;
        }

        // Priority 2: Search for busbar with open last switch
        List<BusbarResult> withOpenSwitch = results.stream().filter(r -> r.lastSwitch() != null && r.lastSwitch().isOpen()).toList();
        if (!withOpenSwitch.isEmpty()) {
            BusbarResult best = withOpenSwitch.stream().min(Comparator.comparingInt(BusbarResult::depth)
                            .thenComparingInt(BusbarResult::switchesBeforeLast))
                            .get();

            return best;
        }

        // Priority 3: Busbars without switch (direct connection)
        List<BusbarResult> withoutSwitch = results.stream().filter(r -> r.lastSwitch() == null).toList();
        if (!withoutSwitch.isEmpty()) {
            BusbarResult best = withoutSwitch.stream().min(Comparator.comparingInt(BusbarResult::depth)).get();
            return best;
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
    private static List<BusbarResult> searchAllBusbars(VoltageLevel.NodeBreakerView view, int startNode) {
        List<BusbarResult> results = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();
        Queue<NodePath> queue = new LinkedList<>();
        queue.offer(new NodePath(startNode, new ArrayList<>(), null));
        while (!queue.isEmpty()) {
            NodePath current = queue.poll();
            if (visited.contains(current.node())) {
                continue;
            }
            visited.add(current.node());
            // Check if current node is a busbar section
            Optional<Terminal> nodeTerminal = view.getOptionalTerminal(current.node());
            if (nodeTerminal.isPresent()) {
                Terminal term = nodeTerminal.get();
                if (term.getConnectable().getType() == IdentifiableType.BUSBAR_SECTION) {
                    String busbarId = term.getConnectable().getId();
                    int depth = current.pathSwitches().size();
                    SwitchInfo lastSwitch = current.lastSwitch();
                    // Calculate number of switches BEFORE the last one
                    int switchesBeforeLast = lastSwitch != null ? (depth - 1) : 0;
                    results.add(new BusbarResult(busbarId, depth, switchesBeforeLast, lastSwitch, null));
                    continue; // Don't explore beyond busbar
                }
            }

            // Explore adjacent nodes through switches
            view.getSwitchStream().forEach(sw -> {
                int node1 = view.getNode1(sw.getId());
                int node2 = view.getNode2(sw.getId());
                if (node1 == current.node() || node2 == current.node()) {
                    int nextNode = (node1 == current.node()) ? node2 : node1;
                    if (!visited.contains(nextNode)) {
                        List<SwitchInfo> newPathSwitches = new ArrayList<>(current.pathSwitches());
                        SwitchInfo switchInfo = new SwitchInfo(sw.getId(), sw.getKind(), sw.isOpen(), node1, node2);
                        newPathSwitches.add(switchInfo);
                        queue.offer(new NodePath(nextNode, newPathSwitches, switchInfo));
                    }
                }
            });
        }
        return results;
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
    public record BusbarResult(String busbarId, int depth, int switchesBeforeLast, SwitchInfo lastSwitch, String selectionReason) { }

    /**
     * Convenience method to get only the busbar ID.
     *
     * @param terminal the starting terminal
     * @return the busbar ID or null if none found
     */
    public static String findBusbarId(Terminal terminal) {
        BusbarResult result = findBestBusbar(terminal);
        return result != null ? result.busbarId() : null;
    }
}
