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
import org.gridsuite.network.map.dto.definition.voltagelevel.VoltageLevelFormInfos;
import org.gridsuite.network.map.dto.definition.voltagelevel.VoltageLevelMapInfos;
import org.gridsuite.network.map.dto.definition.voltagelevel.VoltageLevelTabInfos;
import org.gridsuite.network.map.dto.utils.ElementUtils;

import java.util.*;

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
        List<BusbarSectionInfos> busbarSectionInfos = new ArrayList<>();
        Map<Integer, Integer> nbSectionsPerBusbar = new HashMap<>();

        for (BusbarSection bbs : voltageLevel.getNodeBreakerView().getBusbarSections()) {
            var extension = bbs.getExtension(BusbarSectionPosition.class);
            BusbarSectionInfos busbarSectionInfo = new BusbarSectionInfos();
            if (extension != null) {
                busbarSectionInfo.setBusbarSectionId(bbs.getId());
                if (extension.getBusbarIndex() > busbarSectionInfo.busbarCount) {
                    busbarSectionInfo.setBusbarCount(extension.getBusbarIndex());
                }
                if (extension.getSectionIndex() > busbarSectionInfo.sectionCount) {
                    busbarSectionInfo.setSectionCount(extension.getSectionIndex());
                }
                busbarSectionInfos.add(busbarSectionInfo);
                nbSectionsPerBusbar.putIfAbsent(extension.getBusbarIndex(), 1);
                if (extension.getSectionIndex() > nbSectionsPerBusbar.get(extension.getBusbarIndex())) {
                    nbSectionsPerBusbar.put(extension.getBusbarIndex(), extension.getSectionIndex());
                }
            } else if (nbSectionsPerBusbar.values().stream().anyMatch(v -> v != busbarSectionInfo.sectionCount)) { // Non-symmetrical busbars (nb sections)
                return new VoltageLevelTopologyInfos();
            } else {
                return new VoltageLevelTopologyInfos();
            }
            topologyInfos.setSwitchKinds(Collections.nCopies(busbarSectionInfo.getSectionCount() - 1, SwitchKind.DISCONNECTOR));
        }
        topologyInfos.setRetrievedBusbarSections(true);
        topologyInfos.setBusbarSections(busbarSectionInfos);

        return topologyInfos;
    }

    protected static VoltageLevelFormInfos toFormInfos(Identifiable<?> identifiable) {
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
            builder.busBarSectionInfos((List<BusBarSectionFormInfos>) vlTopologyInfos.getBusbarSections().stream()
                    .map(bbsInfo -> {
                        return BusBarSectionFormInfos.builder()
                                .id(bbsInfo.getBusbarSectionId())
                                .horizPos(bbsInfo.getSectionCount())
                                .vertPos(bbsInfo.getBusbarCount())
                                .build();
                    })
                    .toList());
            builder.switchKinds(vlTopologyInfos.getSwitchKinds());
            builder.isRetrievedBusbarSections(vlTopologyInfos.isRetrievedBusbarSections());
        }

        builder.identifiableShortCircuit(toIdentifiableShortCircuit(voltageLevel));

        return builder.build();
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
        List<BusbarSectionInfos> busbarSections = List.of();
        boolean isRetrievedBusbarSections = false;
        List<SwitchKind> switchKinds = List.of();
    }

    @Getter
    @Setter
    public static class BusbarSectionInfos {
        private String busbarSectionId;
        int busbarCount = 1;
        int sectionCount = 1;
    }
}
