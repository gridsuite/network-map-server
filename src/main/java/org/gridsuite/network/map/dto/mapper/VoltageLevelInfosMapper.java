/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.mapper;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.BusbarSectionPosition;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.gridsuite.network.map.dto.ElementInfos;
import org.gridsuite.network.map.dto.InfoTypeParameters;
import org.gridsuite.network.map.dto.definition.busbarsection.BusBarSectionFormInfos;
import org.gridsuite.network.map.dto.definition.voltagelevel.VoltageLevelFormInfos;
import org.gridsuite.network.map.dto.definition.voltagelevel.VoltageLevelMapInfos;
import org.gridsuite.network.map.dto.definition.voltagelevel.VoltageLevelTabInfos;
import org.gridsuite.network.map.dto.utils.ElementUtils;
import org.gridsuite.network.map.dto.utils.ExtensionUtils;

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
        return switch (infoTypeParameters.getInfoType()) {
            case TAB -> toTabInfos(identifiable);
            case FORM -> toFormInfos(identifiable);
            case LIST -> ElementInfosMapper.toListInfos(identifiable);
            case MAP -> toMapInfos(identifiable);
            default -> throw new UnsupportedOperationException("TODO");
        };
    }

    private static VoltageLevelTopologyInfos getTopologyInfos(VoltageLevel voltageLevel) {
        Map<Integer, Integer> nbSectionsPerBusbar = new HashMap<>();
        List<BusBarSectionFormInfos> busbarSectionInfos = new ArrayList<>();
        int maxBusbarIndex = 1;
        int maxSectionIndex = 1;
        boolean busbarSectionPositionFound = true;
        for (BusbarSection bbs : voltageLevel.getNodeBreakerView().getBusbarSections()) {
            var extension = bbs.getExtension(BusbarSectionPosition.class);
            if (extension == null) {
                busbarSectionPositionFound = false;
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
        VoltageLevelTopologyInfos voltageLevelTopologyInfos = createDefaultTopologyInfosBuilder().build();
        if (!busbarSectionPositionFound) {
            return voltageLevelTopologyInfos;
        }

        voltageLevelTopologyInfos.setBusbarSections(busbarSectionInfos);
        voltageLevelTopologyInfos.setBusbarSectionPositionFound(true);

        boolean isSymmetrical = nbSectionsPerBusbar.values().stream()
                .distinct()
                .count() == 1
                && nbSectionsPerBusbar.values().stream()
                .findFirst()
                .orElse(0)
                .equals(maxSectionIndex);

        if (isSymmetrical) {
            voltageLevelTopologyInfos.setBusbarCount(maxBusbarIndex);
            voltageLevelTopologyInfos.setSectionCount(maxSectionIndex);
            voltageLevelTopologyInfos.setRetrievedBusbarSections(true);
            voltageLevelTopologyInfos.setSwitchKinds(Collections.nCopies(maxSectionIndex - 1, SwitchKind.DISCONNECTOR));
        }
        return voltageLevelTopologyInfos;
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
            builder.isBusbarSectionPositionFound(vlTopologyInfos.isBusbarSectionPositionFound());
            builder.busBarSectionInfos(vlTopologyInfos.getBusBarSectionInfosGrouped());
        }

        builder.identifiableShortCircuit(ExtensionUtils.toIdentifiableShortCircuit(voltageLevel));

        return builder.build();
    }

    static VoltageLevelMapInfos toMapInfos(Identifiable<?> identifiable) {
        VoltageLevel voltageLevel = (VoltageLevel) identifiable;
        return VoltageLevelMapInfos.builder()
                .id(voltageLevel.getId())
                .name(voltageLevel.getOptionalName().orElse(null))
                .substationId(voltageLevel.getSubstation().map(Substation::getId).orElse(null))
                .nominalV(voltageLevel.getNominalV())
                .build();
    }

    static VoltageLevelTabInfos toTabInfos(Identifiable<?> identifiable) {
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
        builder.identifiableShortCircuit(ExtensionUtils.toIdentifiableShortCircuit(voltageLevel));

        return builder.build();
    }

    private static VoltageLevelTopologyInfos.VoltageLevelTopologyInfosBuilder createDefaultTopologyInfosBuilder() {
        return VoltageLevelTopologyInfos.builder()
                .busbarCount(1).sectionCount(1).isRetrievedBusbarSections(false)
                .switchKinds(List.of()).busbarSections(List.of()).isBusbarSectionPositionFound(false);
    }

    @Builder
    @Getter
    @Setter
    public static class VoltageLevelTopologyInfos {
        private List<BusBarSectionFormInfos> busbarSections;
        private boolean isRetrievedBusbarSections;   // true if busbar sections are symmetrical
        private boolean isBusbarSectionPositionFound;
        private int busbarCount;
        private int sectionCount;
        private List<SwitchKind> switchKinds;

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
