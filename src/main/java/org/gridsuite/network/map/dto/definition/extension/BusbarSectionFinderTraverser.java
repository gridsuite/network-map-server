/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.definition.extension;

import com.powsybl.iidm.network.IdentifiableType;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.SwitchKind;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.math.graph.TraverseResult;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
public class BusbarSectionFinderTraverser implements Terminal.TopologyTraverser {

    private final List<BusbarCandidate> busbarCandidates = new ArrayList<>();
    private final Set<String> visitedTerminals = new HashSet<>();
    private static final int MAX_VISITED = 50;

    @Override
    public TraverseResult traverse(Terminal terminal, boolean connected) {
        String terminalId = terminal.getConnectable().getId();
        if (visitedTerminals.contains(terminalId)) {
            return TraverseResult.TERMINATE_PATH;
        }
        visitedTerminals.add(terminalId);
        if (visitedTerminals.size() > MAX_VISITED) {
            return TraverseResult.TERMINATE_TRAVERSER;
        }

        // If a busbar section is found, add it as a candidate
        if (terminal.getConnectable().getType() == IdentifiableType.BUSBAR_SECTION) {
            busbarCandidates.add(new BusbarCandidate(terminalId, connected));
            // CONTINUE to explore other paths to other busbars
            return TraverseResult.CONTINUE;
        }
        return TraverseResult.CONTINUE;
    }

    @Override
    public TraverseResult traverse(Switch aSwitch) {
        if (visitedTerminals.size() > MAX_VISITED) {
            return TraverseResult.TERMINATE_TRAVERSER;
        }

        // KEY: Open disconnectors end this path but not the overall traversal
        // They block access to this busbar but not to the others
        if (aSwitch.isOpen() && aSwitch.getKind() == SwitchKind.DISCONNECTOR) {
            return TraverseResult.TERMINATE_PATH; // Ends this path, not the whole traversal
        }
        return TraverseResult.CONTINUE;
    }

    public String getBusbarWithClosedDisconnector() {
        // Search for a connected busbar (disconnector closed)
        for (BusbarCandidate candidate : busbarCandidates) {
            if (candidate.isConnected()) {
                return candidate.getId();
            }
        }

        // If none is connected, return the first one found (fallback)
        if (!busbarCandidates.isEmpty()) {
            return busbarCandidates.getFirst().getId();
        }
        return null;
    }

    @Getter
    private static class BusbarCandidate {
        private final String id;
        private final boolean connected;

        public BusbarCandidate(String id, boolean connected) {
            this.id = id;
            this.connected = connected;
        }
    }
}
