/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.mapper;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.BusbarSectionPosition;
import lombok.Getter;
import lombok.Setter;
import org.gridsuite.network.map.dto.ElementInfos;
import org.gridsuite.network.map.dto.InfoTypeParameters;
import org.gridsuite.network.map.dto.definition.busbarsection.BusBarSectionFormInfos;
import org.gridsuite.network.map.dto.definition.voltagelevel.FeederBayInfos;
import org.gridsuite.network.map.dto.definition.voltagelevel.VoltageLevelFormInfos;
import org.gridsuite.network.map.dto.definition.voltagelevel.VoltageLevelMapInfos;
import org.gridsuite.network.map.dto.definition.voltagelevel.VoltageLevelTabInfos;
import org.gridsuite.network.map.dto.utils.ElementUtils;

import java.util.*;
import java.util.stream.Collectors;

import static org.gridsuite.network.map.dto.utils.ElementUtils.*;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
public final class VoltageLevelInfosMapper {
    private VoltageLevelInfosMapper() {
    }

    public static ElementInfos toData(Identifiable<?> identifiable, InfoTypeParameters infoTypeParameters) {
        switch (infoTypeParameters.getInfoType()) {
            case TAB:
                return toTabInfos(identifiable);
            case FORM:
                return toFormInfos(identifiable);
            case LIST:
                return ElementInfosMapper.toListInfos(identifiable);
            case MAP:
                return toMapInfos(identifiable);
            default:
                throw new UnsupportedOperationException("TODO");
        }
    }

    public static VoltageLevelTopologyInfos getTopologyInfos(VoltageLevel voltageLevel) {
        VoltageLevelTopologyInfos topologyInfos = new VoltageLevelTopologyInfos();
        Map<Integer, Integer> nbSectionsPerBusbar = new HashMap<>();
        List<BusBarSectionFormInfos> busbarSectionInfos = new ArrayList<>();
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
                BusBarSectionFormInfos busbarSectionInfo = BusBarSectionFormInfos.builder()
                        .id(bbs.getId())
                        .vertPos(extension.getSectionIndex())
                        .horizPos(extension.getBusbarIndex())
                        .build();
                busbarSectionInfos.add(busbarSectionInfo);
            } else {
                return new VoltageLevelTopologyInfos();
            }
        }
        if (nbSectionsPerBusbar.values().stream().anyMatch(v -> v != topologyInfos.sectionCount)) { // Non-symmetrical busbars (nb sections)
            return new VoltageLevelTopologyInfos();
        }

        topologyInfos.setRetrievedBusbarSections(true);
        topologyInfos.setSwitchKinds(Collections.nCopies(topologyInfos.getSectionCount() - 1, SwitchKind.DISCONNECTOR));
        topologyInfos.setBusbarSections(busbarSectionInfos);

        return topologyInfos;
    }

    static VoltageLevelFormInfos toFormInfos(Identifiable<?> identifiable) {
        VoltageLevel voltageLevel = (VoltageLevel) identifiable;
        VoltageLevelFormInfos.VoltageLevelFormInfosBuilder<?, ?> builder = VoltageLevelFormInfos.builder()
                .name(voltageLevel.getOptionalName().orElse(null))
                .id(voltageLevel.getId())
                .topologyKind(voltageLevel.getTopologyKind())
                .substationId(voltageLevel.getSubstation().map(Substation::getId).orElse(null))
                .nominalV(voltageLevel.getNominalV())
                .lowVoltageLimit(Double.isNaN(voltageLevel.getLowVoltageLimit()) ? null : voltageLevel.getLowVoltageLimit())
                .highVoltageLimit(Double.isNaN(voltageLevel.getHighVoltageLimit()) ? null : voltageLevel.getHighVoltageLimit())
                .properties(getProperties(voltageLevel));

        if (voltageLevel.getTopologyKind().equals(TopologyKind.NODE_BREAKER)) {
            VoltageLevelTopologyInfos vlTopologyInfos = getTopologyInfos(voltageLevel);
            builder.busbarCount(vlTopologyInfos.getBusbarCount());
            builder.sectionCount(vlTopologyInfos.getSectionCount());
            builder.switchKinds(vlTopologyInfos.getSwitchKinds());
            builder.isRetrievedBusbarSections(vlTopologyInfos.isRetrievedBusbarSections());
            builder.busBarSectionInfos(vlTopologyInfos.getBusBarSectionInfosGrouped());
            builder.connectablePositionInfos(getConnectionInfos(voltageLevel));
        }

        builder.identifiableShortCircuit(toIdentifiableShortCircuit(voltageLevel));

        return builder.build();
    }

    private static Map<String, List<FeederBayInfos>> getConnectionInfos(VoltageLevel voltageLevel) {
        Map<String, List<FeederBayInfos>> connections = new HashMap<>();
        voltageLevel.getConnectableStream()
            .filter(connectable -> !(connectable instanceof BusbarSection))
            .forEach(connectable -> {
                switch (connectable) {
                    case Injection<?> injection -> connections.put(injection.getId(), List.of(new FeederBayInfos(getBusOrBusbarSection(injection.getTerminal()),
                            toMapConnectablePosition(injection, 0))));
                    case Branch<?> branch -> {
                        List<FeederBayInfos> branchConnections = new ArrayList<>();
                        if (branch.getTerminal1().getVoltageLevel().getId().equals(voltageLevel.getId())) {
                            branchConnections.add(new FeederBayInfos(getBusOrBusbarSection(branch.getTerminal1()), toMapConnectablePosition(branch, 1)));
                        }
                        if (branch.getTerminal2().getVoltageLevel().getId().equals(voltageLevel.getId())) {
                            branchConnections.add(new FeederBayInfos(getBusOrBusbarSection(branch.getTerminal2()), toMapConnectablePosition(branch, 2)));
                        }
                        connections.put(branch.getId(), branchConnections);
                    }
                    default -> throw new IllegalArgumentException("connectable type: " + connectable.getClass() + " not supported");
                }
            });
        return connections;
    }

    protected static VoltageLevelMapInfos toMapInfos(Identifiable<?> identifiable) {
        VoltageLevel voltageLevel = (VoltageLevel) identifiable;
        return VoltageLevelMapInfos.builder()
                .id(voltageLevel.getId())
                .name(voltageLevel.getOptionalName().orElse(null))
                .substationId(voltageLevel.getSubstation().map(Substation::getId).orElse(null))
                .nominalV(voltageLevel.getNominalV())
                .build();
    }

    protected static VoltageLevelTabInfos toTabInfos(Identifiable<?> identifiable) {
        VoltageLevel voltageLevel = (VoltageLevel) identifiable;

        VoltageLevelTabInfos.VoltageLevelTabInfosBuilder builder = VoltageLevelTabInfos.builder()
                .id(voltageLevel.getId())
                .name(voltageLevel.getOptionalName().orElse(null))
                .substationId(voltageLevel.getSubstation().map(Substation::getId).orElse(null))
                .nominalV(voltageLevel.getNominalV())
                .country(mapCountry(voltageLevel.getSubstation().orElse(null)))
                .lowVoltageLimit(nullIfNan(voltageLevel.getLowVoltageLimit()))
                .properties(getProperties(voltageLevel))
                .highVoltageLimit(nullIfNan(voltageLevel.getHighVoltageLimit()))
                .substationProperties(voltageLevel.getSubstation().map(ElementUtils::getProperties).orElse(null));
        builder.identifiableShortCircuit(toIdentifiableShortCircuit(voltageLevel));

        return builder.build();
    }

    @Getter
    @Setter
    public static class VoltageLevelTopologyInfos {
        List<BusBarSectionFormInfos> busbarSections = List.of();
        boolean isRetrievedBusbarSections = false;
        int busbarCount = 1;
        int sectionCount = 1;
        List<SwitchKind> switchKinds = List.of();

        public Map<String, List<String>> getBusBarSectionInfosGrouped() {
            return busbarSections.stream()
                    .collect(Collectors.groupingBy(
                            section -> String.valueOf(section.getHorizPos()),
                            Collectors.collectingAndThen(
                                    Collectors.toList(),
                                    list -> list.stream()
                                            .sorted(Comparator.comparing(BusBarSectionFormInfos::getVertPos))
                                            .map(BusBarSectionFormInfos::getId)
                                            .toList()
                            )
                    ));
        }
    }
}
