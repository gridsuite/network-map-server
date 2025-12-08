/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.BusbarSectionPosition;
import lombok.Builder;
import lombok.Getter;
import org.gridsuite.network.map.dto.ElementInfos;
import org.gridsuite.network.map.dto.InfoTypeParameters;
import org.gridsuite.network.map.dto.definition.voltagelevel.VoltageLevelFormInfos;
import org.gridsuite.network.map.dto.definition.voltagelevel.VoltageLevelMapInfos;
import org.gridsuite.network.map.dto.definition.voltagelevel.VoltageLevelTabInfos;
import org.gridsuite.network.map.dto.definition.voltagelevel.VoltageLevelTooltipInfos;
import org.gridsuite.network.map.dto.utils.ElementUtils;
import org.gridsuite.network.map.dto.utils.ExtensionUtils;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.gridsuite.network.map.dto.mapper.VoltageLevelInfosMapper.TopologyInfos.setDefaultBuilder;
import static org.gridsuite.network.map.dto.utils.ElementUtils.*;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
public final class VoltageLevelInfosMapper {
    private VoltageLevelInfosMapper() {
    }

    public static ElementInfos toData(Identifiable<?> identifiable, InfoTypeParameters infoTypeParameters) {
        ObjectMapper objectMapper = new ObjectMapper();

        String busIdToIccValuesStr = infoTypeParameters.getOptionalParameters().getOrDefault(InfoTypeParameters.QUERY_PARAM_BUS_ID_TO_ICC_VALUES, null);
        Map<String, Double> busIdToIccValues;
        try {
            busIdToIccValues = busIdToIccValuesStr != null
                ? objectMapper.readValue(UriUtils.decode(busIdToIccValuesStr, StandardCharsets.UTF_8), new TypeReference<>() { })
                : Map.of();
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid JSON: " + busIdToIccValuesStr, e);
        }
        return switch (infoTypeParameters.getInfoType()) {
            case TAB -> toTabInfos(identifiable);
            case FORM -> toFormInfos(identifiable);
            case LIST -> ElementInfosMapper.toListInfos(identifiable);
            case MAP -> toMapInfos(identifiable);
            case TOOLTIP -> toTooltipInfos(identifiable, busIdToIccValues);
            default -> throw handleUnsupportedInfoType(infoTypeParameters.getInfoType(), "VoltageLevel");
        };
    }

    static VoltageLevelFormInfos toFormInfos(Identifiable<?> identifiable) {
        VoltageLevel voltageLevel = (VoltageLevel) identifiable;
        VoltageLevelFormInfos voltageLevelFormInfos = VoltageLevelFormInfos.builder()
                .name(voltageLevel.getOptionalName().orElse(null))
                .id(voltageLevel.getId())
                .topologyKind(voltageLevel.getTopologyKind())
                .substationId(voltageLevel.getSubstation().map(Substation::getId).orElse(null))
                .nominalV(voltageLevel.getNominalV())
                .lowVoltageLimit(Double.isNaN(voltageLevel.getLowVoltageLimit()) ? null : voltageLevel.getLowVoltageLimit())
                .highVoltageLimit(Double.isNaN(voltageLevel.getHighVoltageLimit()) ? null : voltageLevel.getHighVoltageLimit())
                .properties(getProperties(voltageLevel))
                .identifiableShortCircuit(ExtensionUtils.toIdentifiableShortCircuit(voltageLevel))
                .build();
        if (voltageLevel.getTopologyKind().equals(TopologyKind.NODE_BREAKER)) {
            TopologyInfos topologyInfos = getVoltageLevelBusBarSectionsInfos(voltageLevel);
            voltageLevelFormInfos.setBusbarCount(topologyInfos.getBusbarCount());
            voltageLevelFormInfos.setSectionCount(topologyInfos.getSectionCount());
            voltageLevelFormInfos.setSwitchKinds(topologyInfos.getSwitchKinds());
            voltageLevelFormInfos.setIsSymmetrical(topologyInfos.getIsSymmetrical());
        }
        return voltageLevelFormInfos;
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

    static VoltageLevelTooltipInfos toTooltipInfos(Identifiable<?> identifiable, Map<String, Double> busIdToIccValues) {
        VoltageLevel voltageLevel = (VoltageLevel) identifiable;

        List<VoltageLevelTooltipInfos.VoltageLevelBusTooltipInfos> busTooltipInfos = voltageLevel.getBusView().getBusStream().map(b -> {
            Double loadValue = makeNaNNull(b.getLoadStream().mapToDouble(l -> l.getTerminal().getP()).sum());
            Double generationValue = makeNaNNull(b.getGeneratorStream().mapToDouble(g -> g.getTerminal().getP()).sum());
            Double balanceValue =
                (loadValue == null && generationValue == null)
                    ? null
                    : Optional.ofNullable(loadValue).orElse(0.0)
                    + Optional.ofNullable(generationValue).orElse(0.0);
            return VoltageLevelTooltipInfos.VoltageLevelBusTooltipInfos.builder()
                .id(b.getId())
                .u(makeNaNNull(b.getV()))
                .angle(makeNaNNull(b.getAngle()))
                .load(loadValue != null ? Math.abs(loadValue) : null)
                .generation(generationValue)
                .balance(balanceValue)
                .icc(busIdToIccValues.getOrDefault(b.getId(), 0.0))
                .build();
        }).toList();
        return VoltageLevelTooltipInfos.builder()
            .id(voltageLevel.getId())
            .name(voltageLevel.getOptionalName().orElse(null))
            .busInfos(busTooltipInfos)
            .uMin(voltageLevel.getLowVoltageLimit())
            .uMax(voltageLevel.getHighVoltageLimit())
            .build();
    }

    private static Double makeNaNNull(Double value) {
        return Double.isNaN(value) ? null : value;
    }

    static VoltageLevelTabInfos toTabInfos(Identifiable<?> identifiable) {
        VoltageLevel voltageLevel = (VoltageLevel) identifiable;

        VoltageLevelTabInfos.VoltageLevelTabInfosBuilder<?, ?> builder = VoltageLevelTabInfos.builder()
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

    @Builder
    @Getter
    public static class TopologyInfos {
        private Integer busbarCount;

        private Integer sectionCount;

        private List<SwitchKind> switchKinds;

        private Boolean isSymmetrical;

        public static TopologyInfos.TopologyInfosBuilder setDefaultBuilder() {
            return TopologyInfos.builder().busbarCount(1)
                    .sectionCount(1)
                    .switchKinds(Collections.emptyList())
                    .isSymmetrical(false);
        }
    }

    public static TopologyInfos getVoltageLevelBusBarSectionsInfos(VoltageLevel voltageLevel) {
        Map<Integer, Integer> nbSectionsPerBusbar = new HashMap<>();
        TopologyInfos.TopologyInfosBuilder builder = setDefaultBuilder();
        int maxBusbarIndex = 1;
        int maxSectionIndex = 1;
        for (BusbarSection bbs : voltageLevel.getNodeBreakerView().getBusbarSections()) {
            var extension = bbs.getExtension(BusbarSectionPosition.class);
            if (extension == null) {
                return builder.build();
            }
            int busbarIndex = extension.getBusbarIndex();
            int sectionIndex = extension.getSectionIndex();
            maxBusbarIndex = Math.max(maxBusbarIndex, busbarIndex);
            maxSectionIndex = Math.max(maxSectionIndex, sectionIndex);
            nbSectionsPerBusbar.merge(busbarIndex, 1, Integer::sum);
        }

        boolean isSymmetrical = maxBusbarIndex == 1 ||
                nbSectionsPerBusbar.values().stream().distinct().count() == 1
                        && nbSectionsPerBusbar.values().stream().findFirst().orElse(0).equals(maxSectionIndex);

        if (isSymmetrical) {
            return builder.busbarCount(maxBusbarIndex)
                    .sectionCount(maxSectionIndex)
                    .switchKinds(Collections.nCopies(maxSectionIndex - 1, SwitchKind.DISCONNECTOR))
                    .isSymmetrical(true)
                    .build();
        }
        return builder.build();
    }
}
