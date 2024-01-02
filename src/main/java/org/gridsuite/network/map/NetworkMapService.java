/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.network.store.client.NetworkStoreService;
import com.powsybl.network.store.client.PreloadingStrategy;
import org.gridsuite.network.map.dto.AllElementsInfos;
import org.gridsuite.network.map.dto.ElementInfos;
import org.gridsuite.network.map.dto.definition.hvdc.HvdcShuntCompensatorsInfos;
import org.gridsuite.network.map.dto.mapper.*;
import org.gridsuite.network.map.dto.ElementInfos.InfoType;
import org.gridsuite.network.map.dto.ElementInfos.ElementInfoType;
import org.gridsuite.network.map.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.gridsuite.network.map.dto.utils.ElementUtils.toIdentifiableShortCircuit;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@ComponentScan(basePackageClasses = {NetworkStoreService.class})
@Service
class NetworkMapService {

    @Autowired
    private NetworkStoreService networkStoreService;

    private Network getNetwork(UUID networkUuid, PreloadingStrategy strategy, String variantId) {
        try {
            Network network = networkStoreService.getNetwork(networkUuid, strategy);
            if (variantId != null) {
                network.getVariantManager().setWorkingVariant(variantId);
            }
            return network;
        } catch (PowsyblException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    private static VoltageLevelMapData toMapData(VoltageLevel voltageLevel) {
        VoltageLevelMapData.VoltageLevelMapDataBuilder builder = VoltageLevelMapData.builder()
                .id(voltageLevel.getId())
                .topologyKind(voltageLevel.getTopologyKind());

        builder.name(voltageLevel.getOptionalName().orElse(null))
                .substationId(voltageLevel.getSubstation().map(Substation::getId).orElse(null))
                .substationName(voltageLevel.getSubstation().map(s -> s.getOptionalName().orElse(null)).orElse(null))

                .nominalVoltage(voltageLevel.getNominalV())
                .lowVoltageLimit(Double.isNaN(voltageLevel.getLowVoltageLimit()) ? null : voltageLevel.getLowVoltageLimit())
                .highVoltageLimit(Double.isNaN(voltageLevel.getHighVoltageLimit()) ? null : voltageLevel.getHighVoltageLimit());
        if (voltageLevel.getTopologyKind().equals(TopologyKind.NODE_BREAKER)) {
            VoltageLevelInfosMapper.VoltageLevelTopologyInfos topologyInfos = VoltageLevelInfosMapper.getTopologyInfos(voltageLevel);
            builder.busbarCount(topologyInfos.getBusbarCount());
            builder.sectionCount(topologyInfos.getSectionCount());
            builder.switchKinds(topologyInfos.getSwitchKinds());
        }

        builder.identifiableShortCircuit(toIdentifiableShortCircuit(voltageLevel));

        return builder.build();
    }

    private static VoltageLevelConnectableMapData toMapData(Connectable<?> connectable) {
        return VoltageLevelConnectableMapData.builder()
                .id(connectable.getId())
                .name(connectable.getOptionalName().orElse(null))
                .type(connectable.getType())
                .build();
    }

    private static BusMapData toMapData(Bus bus) {
        return BusMapData.builder()
            .name(bus.getOptionalName().orElse(null))
            .id(bus.getId())
            .build();
    }

    private PreloadingStrategy getPreloadingStrategy(List<String> substationsIds) {
        return substationsIds == null ? PreloadingStrategy.COLLECTION : PreloadingStrategy.NONE;
    }

    public List<String> getSubstationsIds(UUID networkUuid, String variantId) {
        Network network = getNetwork(networkUuid, PreloadingStrategy.COLLECTION, variantId);
        return network.getSubstationStream()
                .map(Substation::getId).collect(Collectors.toList());
    }

    public AllElementsInfos getAllElementsInfos(UUID networkUuid, String variantId, List<String> substationsId) {
        Network network = getNetwork(networkUuid, getPreloadingStrategy(substationsId), variantId);
        return AllElementsInfos.builder()
                .substations(getSubstationsInfos(network, substationsId, new ElementInfoType(InfoType.TAB)))
                .hvdcLines(getHvdcLinesInfos(network, substationsId, new ElementInfoType(InfoType.TAB)))
                .lines(getElementsInfos(network, substationsId, ElementType.LINE, new ElementInfoType(InfoType.TAB)))
                .loads(getElementsInfos(network, substationsId, ElementType.LOAD, new ElementInfoType(InfoType.TAB)))
                .generators(getElementsInfos(network, substationsId, ElementType.GENERATOR, new ElementInfoType(InfoType.TAB)))
                .twoWindingsTransformers(getElementsInfos(network, substationsId, ElementType.TWO_WINDINGS_TRANSFORMER, new ElementInfoType(InfoType.TAB)))
                .threeWindingsTransformers(getElementsInfos(network, substationsId, ElementType.THREE_WINDINGS_TRANSFORMER, new ElementInfoType(InfoType.TAB)))
                .batteries(getElementsInfos(network, substationsId, ElementType.BATTERY, new ElementInfoType(InfoType.TAB)))
                .danglingLines(getElementsInfos(network, substationsId, ElementType.DANGLING_LINE, new ElementInfoType(InfoType.TAB)))
                .lccConverterStations(getElementsInfos(network, substationsId, ElementType.LCC_CONVERTER_STATION, new ElementInfoType(InfoType.TAB)))
                .shuntCompensators(getElementsInfos(network, substationsId, ElementType.SHUNT_COMPENSATOR, new ElementInfoType(InfoType.TAB)))
                .staticVarCompensators(getElementsInfos(network, substationsId, ElementType.STATIC_VAR_COMPENSATOR, new ElementInfoType(InfoType.TAB)))
                .vscConverterStations(getElementsInfos(network, substationsId, ElementType.VSC_CONVERTER_STATION, new ElementInfoType(InfoType.TAB)))
                .build();
    }

    public List<String> getVoltageLevelsIds(UUID networkUuid, String variantId, List<String> substationsIds) {
        Network network = getNetwork(networkUuid, getPreloadingStrategy(substationsIds), variantId);
        return substationsIds == null ?
                network.getVoltageLevelStream().map(VoltageLevel::getId).collect(Collectors.toList()) :
                substationsIds.stream().flatMap(id -> network.getSubstation(id).getVoltageLevelStream().map(VoltageLevel::getId)).collect(Collectors.toList());
    }

    public List<VoltageLevelsEquipmentsMapData> getVoltageLevelsAndConnectable(UUID networkUuid, String variantId, List<String> substationsId) {
        Network network = getNetwork(networkUuid, getPreloadingStrategy(substationsId), variantId);
        List<VoltageLevel> voltageLevels = substationsId == null ?
                network.getVoltageLevelStream().collect(Collectors.toList()) :
                substationsId.stream().flatMap(id -> network.getSubstation(id).getVoltageLevelStream()).collect(Collectors.toList());

        return voltageLevels.stream().map(vl -> {
            List<VoltageLevelConnectableMapData> equipments = new ArrayList<>();
            vl.getConnectables().forEach(connectable -> equipments.add(toMapData(connectable)));
            return VoltageLevelsEquipmentsMapData.builder().voltageLevel(toMapData(vl)).equipments(equipments).build();
        }).collect(Collectors.toList());
    }

    public List<VoltageLevelConnectableMapData> getVoltageLevelEquipements(UUID networkUuid, String voltageLevelId, String variantId, List<String> substationsId) {
        Network network = getNetwork(networkUuid, getPreloadingStrategy(substationsId), variantId);
        List<VoltageLevel> voltageLevels = substationsId == null ?
                List.of(network.getVoltageLevel(voltageLevelId)) :
                substationsId.stream().flatMap(id -> network.getSubstation(id).getVoltageLevelStream().filter(voltageLevel -> voltageLevelId.equals(voltageLevel.getId()))).collect(Collectors.toList());

        return voltageLevels.stream()
                .flatMap(VoltageLevel::getConnectableStream)
                .map(NetworkMapService::toMapData)
                .collect(Collectors.toList());
    }

    public List<BusMapData> getVoltageLevelBuses(UUID networkUuid, String voltageLevelId, String variantId) {
        Network network = getNetwork(networkUuid, PreloadingStrategy.NONE, variantId);
        return network.getVoltageLevel(voltageLevelId).getBusBreakerView().getBusStream()
            .map(NetworkMapService::toMapData).collect(Collectors.toList());
    }

    public List<ElementInfos> getVoltageLevelBusbarSections(UUID networkUuid, String voltageLevelId, String variantId) {
        Network network = getNetwork(networkUuid, PreloadingStrategy.NONE, variantId);
        return network.getVoltageLevel(voltageLevelId).getNodeBreakerView().getBusbarSectionStream()
            .map(BusBarSectionInfosMapper::toFormInfos).collect(Collectors.toList());
    }

    public List<String> getVoltageLevelBusbarSectionsIds(UUID networkUuid, String voltageLevelId, String variantId) {
        Network network = getNetwork(networkUuid, PreloadingStrategy.NONE, variantId);
        return network.getVoltageLevel(voltageLevelId).getNodeBreakerView().getBusbarSectionStream()
                .map(BusbarSection::getId).collect(Collectors.toList());
    }

    public List<String> getHvdcLinesIds(UUID networkUuid, String variantId, List<String> substationsIds) {
        Network network = getNetwork(networkUuid, getPreloadingStrategy(substationsIds), variantId);
        if (substationsIds == null) {
            return network.getHvdcLineStream()
                    .map(HvdcLine::getId).collect(Collectors.toList());
        } else {
            return substationsIds.stream()
                    .flatMap(substationsId -> network.getSubstation(substationsId).getVoltageLevelStream())
                    .flatMap(voltageLevel -> voltageLevel.getConnectableStream(HvdcConverterStation.class))
                    .map(HvdcConverterStation::getHvdcLine)
                    .filter(Objects::nonNull)
                    .map(HvdcLine::getId)
                    .distinct()
                    .collect(Collectors.toList());
        }
    }

    private List<ElementInfos> getSubstationsInfos(Network network, List<String> substationsId, ElementInfoType elementInfoType) {
        Stream<Substation> substations = substationsId == null ? network.getSubstationStream() : substationsId.stream().map(network::getSubstation);
        return substations
                .map(c -> ElementType.SUBSTATION.getInfosGetter().apply(c, elementInfoType))
                .collect(Collectors.toList());
    }

    public List<ElementInfos> getVoltageLevelsInfos(Network network, List<String> substationsId, ElementInfoType elementInfoType) {
        Stream<VoltageLevel> voltageLevels = substationsId == null ? network.getVoltageLevelStream() : substationsId.stream().flatMap(id -> network.getSubstation(id).getVoltageLevelStream());
        return voltageLevels
                .map(c -> ElementType.VOLTAGE_LEVEL.getInfosGetter().apply(c, elementInfoType))
                .collect(Collectors.toList());
    }

    public List<ElementInfos> getHvdcLinesInfos(Network network, List<String> substationsId, ElementInfoType elementInfoType) {
        Stream<HvdcLine> hvdcLines = substationsId == null ? network.getHvdcLineStream() :
                substationsId.stream()
                        .map(network::getSubstation)
                        .flatMap(Substation::getVoltageLevelStream)
                        .flatMap(vl -> vl.getConnectableStream(HvdcConverterStation.class))
                        .map(HvdcConverterStation::getHvdcLine)
                        .filter(Objects::nonNull)
                        .distinct();
        return hvdcLines
                .map(c -> ElementType.HVDC_LINE.getInfosGetter().apply(c, elementInfoType))
                .distinct()
                .collect(Collectors.toList());
    }

    private List<ElementInfos> getElementsInfos(Network network, List<String> substationsIds, ElementType elementType, ElementInfoType elementInfoType) {
        Class<? extends Connectable> elementClass = (Class<? extends Connectable>) elementType.getElementClass();
        Stream<? extends Connectable> connectables = substationsIds == null ?
            getConnectableStream(network, elementType) :
            substationsIds.stream()
                .flatMap(substationId -> network.getSubstation(substationId).getVoltageLevelStream())
                .flatMap(voltageLevel -> voltageLevel.getConnectableStream(elementClass))
                .distinct();
        return connectables
            .map(c -> elementType.getInfosGetter().apply(c, elementInfoType))
            .collect(Collectors.toList());
    }

    public List<ElementInfos> getElementsInfos(UUID networkUuid, String variantId, List<String> substationsIds, ElementType equipmentType, ElementInfoType elementInfoType) {
        Network network = getNetwork(networkUuid, getPreloadingStrategy(substationsIds), variantId);
        switch (equipmentType) {
            case SUBSTATION:
                return getSubstationsInfos(network, substationsIds, elementInfoType);
            case VOLTAGE_LEVEL:
                return getVoltageLevelsInfos(network, substationsIds, elementInfoType);
            case HVDC_LINE:
                return getHvdcLinesInfos(network, substationsIds, elementInfoType);
            default:
                return getElementsInfos(network, substationsIds, equipmentType, elementInfoType);
        }
    }

    public ElementInfos getElementInfos(UUID networkUuid, String variantId, ElementType elementType, ElementInfoType elementInfoType, String elementId) {
        Network network = getNetwork(networkUuid, PreloadingStrategy.NONE, variantId);
        Identifiable<?> identifiable = network.getIdentifiable(elementId);
        if (identifiable == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return elementType.getInfosGetter().apply(identifiable, elementInfoType);
    }

    // Ideally we should directly call the appropriate method but in some cases we receive only an ID without knowing its type
    public ElementInfos getBranchOrThreeWindingsTransformer(UUID networkUuid, String variantId, String equipmentId) {
        Network network = getNetwork(networkUuid, PreloadingStrategy.NONE, variantId);
        Line line = network.getLine(equipmentId);
        if (line != null) {
            return LineInfosMapper.toListInfos(line);
        }
        TwoWindingsTransformer twoWT = network.getTwoWindingsTransformer(equipmentId);
        if (twoWT != null) {
            return TwoWindingsTransformerInfosMapper.toListInfos(twoWT);
        }
        ThreeWindingsTransformer threeWT = network.getThreeWindingsTransformer(equipmentId);
        if (threeWT != null) {
            return ThreeWindingsTransformerInfosMapper.toListInfos(threeWT);
        }
        throw new ResponseStatusException(HttpStatus.NO_CONTENT);
    }

    public HvdcShuntCompensatorsInfos getHvdcLineShuntCompensators(UUID networkUuid, String variantId, String hvdcId) {
        Network network = getNetwork(networkUuid, PreloadingStrategy.NONE, variantId);
        HvdcLine hvdcLine = network.getHvdcLine(hvdcId);
        if (hvdcLine == null) {
            // called from a modification, then we must support un-existing equipment
            return HvdcShuntCompensatorsInfos.builder().id(hvdcId).build();
        }
        return HvdcInfosMapper.toHvdcShuntCompensatorsInfos(hvdcLine);
    }

    public List<String> getElementsIds(UUID networkUuid, String variantId, List<String> substationsIds, ElementType elementType) {
        switch (elementType) {
            case SUBSTATION:
                return getSubstationsIds(networkUuid, variantId);
            case HVDC_LINE:
                return getHvdcLinesIds(networkUuid, variantId, substationsIds);
            case VOLTAGE_LEVEL:
                return getVoltageLevelsIds(networkUuid, variantId, substationsIds);
            default:
                return getConnectablesIds(networkUuid, variantId, substationsIds, elementType);
        }
    }

    private List<String> getConnectablesIds(UUID networkUuid, String variantId, List<String> substationsIds, ElementType elementType) {
        Network network = getNetwork(networkUuid, getPreloadingStrategy(substationsIds), variantId);
        if (substationsIds == null) {
            return getConnectableStream(network, elementType)
                .map(Connectable::getId)
                .collect(Collectors.toList());
        } else {
            return substationsIds.stream()
                .flatMap(substationId -> network.getSubstation(substationId).getVoltageLevelStream())
                .flatMap(voltageLevel -> voltageLevel.getConnectableStream((Class<? extends Connectable>) elementType.getElementClass()))
                .map(Connectable::getId)
                .distinct()
                .collect(Collectors.toList());
        }
    }

    public Stream<? extends Connectable> getConnectableStream(Network network, ElementType elementType) {
        switch (elementType) {
            case BUSBAR_SECTION:
                return network.getBusbarSectionStream();
            case GENERATOR:
                return network.getGeneratorStream();
            case LINE:
                return network.getLineStream();
            case TWO_WINDINGS_TRANSFORMER:
                return network.getTwoWindingsTransformerStream();
            case THREE_WINDINGS_TRANSFORMER:
                return network.getThreeWindingsTransformerStream();
            case BATTERY:
                return network.getBatteryStream();
            case DANGLING_LINE:
                return network.getDanglingLineStream();
            case LCC_CONVERTER_STATION:
                return network.getLccConverterStationStream();
            case VSC_CONVERTER_STATION:
                return network.getVscConverterStationStream();
            case LOAD:
                return network.getLoadStream();
            case SHUNT_COMPENSATOR:
                return network.getShuntCompensatorStream();
            case STATIC_VAR_COMPENSATOR:
                return network.getStaticVarCompensatorStream();
            default:
                throw new IllegalStateException("Unexpected connectable type:" + elementType);
        }
    }

    public Set<Country> getCountries(UUID networkUuid, String variantId) {
        Network network = getNetwork(networkUuid, PreloadingStrategy.COLLECTION, variantId);
        return network.getSubstationStream()
                .map(Substation::getCountry)
                .filter(Optional::isPresent)
                .map(Optional::get).collect(Collectors.toSet());
    }
}
