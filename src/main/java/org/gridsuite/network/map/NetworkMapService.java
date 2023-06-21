/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.*;
import com.powsybl.network.store.client.NetworkStoreService;
import com.powsybl.network.store.client.PreloadingStrategy;
import org.gridsuite.network.map.dto.ElementInfos;
import org.gridsuite.network.map.dto.hvdc.HvdcShuntCompensatorInfos;
import org.gridsuite.network.map.dto.hvdc.SelectedShuntCompensatorData;
import org.gridsuite.network.map.dto.line.LineListInfos;
import org.gridsuite.network.map.dto.threewindingstransformer.ThreeWindingsTransformerListInfos;
import org.gridsuite.network.map.dto.twowindingstransformer.TwoWindingsTransformerListInfos;
import org.gridsuite.network.map.dto.AllElementsInfos;
import org.gridsuite.network.map.dto.busbarsection.BusBarSectionFormInfos;
import org.gridsuite.network.map.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.gridsuite.network.map.dto.utils.ElementUtils.getBusOrBusbarSection;

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
            mapVoltageLevelSwitchKindsAndSectionCount(builder, voltageLevel);
        }
        IdentifiableShortCircuit identifiableShortCircuit = voltageLevel.getExtension(IdentifiableShortCircuit.class);
        if (identifiableShortCircuit != null) {
            builder.ipMin(identifiableShortCircuit.getIpMin());
            builder.ipMax(identifiableShortCircuit.getIpMax());
        }

        return builder.build();
    }

    private static void mapVoltageLevelSwitchKindsAndSectionCount(
            VoltageLevelMapData.VoltageLevelMapDataBuilder builder, VoltageLevel voltageLevel) {
        AtomicInteger busbarCount = new AtomicInteger(1);
        AtomicInteger sectionCount = new AtomicInteger(1);
        AtomicBoolean warning = new AtomicBoolean(false);
        if (voltageLevel.getTopologyKind().equals(TopologyKind.NODE_BREAKER)) {
            determinateBusBarSectionPosition(voltageLevel, busbarCount, sectionCount, warning);
            if (!warning.get()) {
                builder.busbarCount(busbarCount.get());
                builder.sectionCount(sectionCount.get());
                builder.switchKinds(new ArrayList<>(Collections.nCopies(sectionCount.get() - 1, SwitchKind.DISCONNECTOR)));
            } else {
                builder.busbarCount(1);
                builder.sectionCount(1);
                builder.switchKinds(Collections.emptyList());
            }
        } else {
            warning.set(true);
        }
    }

    private static void determinateBusBarSectionPosition(VoltageLevel voltageLevel, AtomicInteger busbarCount, AtomicInteger sectionCount, AtomicBoolean warning) {
        voltageLevel.getNodeBreakerView().getBusbarSections().forEach(bbs -> {
            var pos = bbs.getExtension(BusbarSectionPosition.class);
            if (pos != null) {
                if (pos.getBusbarIndex() > busbarCount.get()) {
                    busbarCount.set(pos.getBusbarIndex());
                }
                if (pos.getSectionIndex() > sectionCount.get()) {
                    sectionCount.set(pos.getSectionIndex());
                }
            } else {
                warning.set(true);
            }
        });
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

    private List<String> getElementsIds(UUID networkUuid, String variantId, List<String> substationsIds, Class<? extends Connectable> equipmentClass) {
        Network network = getNetwork(networkUuid, getPreloadingStrategy(substationsIds), variantId);
        if (substationsIds == null) {
            return network.getConnectableStream(equipmentClass)
                    .map(Connectable::getId)
                    .collect(Collectors.toList());
        } else {
            return substationsIds.stream()
                    .flatMap(substationId -> network.getSubstation(substationId).getVoltageLevelStream())
                    .flatMap(voltageLevel -> voltageLevel.getConnectableStream(equipmentClass))
                    .map(Connectable::getId)
                    .distinct()
                    .collect(Collectors.toList());
        }
    }

    public AllElementsInfos getAllElementsInfos(UUID networkUuid, String variantId, List<String> substationsId) {
        Network network = getNetwork(networkUuid, getPreloadingStrategy(substationsId), variantId);
        return AllElementsInfos.builder()
                .substations(getSubstationsInfos(network, substationsId, ElementInfos.InfoType.TAB))
                .hvdcLines(getHvdcLinesInfos(network, substationsId, ElementInfos.InfoType.TAB))
                .lines(getElementsInfos(network, substationsId, ElementType.LINE, ElementInfos.InfoType.TAB))
                .loads(getElementsInfos(network, substationsId, ElementType.LOAD, ElementInfos.InfoType.TAB))
                .generators(getElementsInfos(network, substationsId, ElementType.GENERATOR, ElementInfos.InfoType.TAB))
                .twoWindingsTransformers(getElementsInfos(network, substationsId, ElementType.TWO_WINDINGS_TRANSFORMER, ElementInfos.InfoType.TAB))
                .threeWindingsTransformers(getElementsInfos(network, substationsId, ElementType.THREE_WINDINGS_TRANSFORMER, ElementInfos.InfoType.TAB))
                .batteries(getElementsInfos(network, substationsId, ElementType.BATTERY, ElementInfos.InfoType.TAB))
                .danglingLines(getElementsInfos(network, substationsId, ElementType.DANGLING_LINE, ElementInfos.InfoType.TAB))
                .lccConverterStations(getElementsInfos(network, substationsId, ElementType.LCC_CONVERTER_STATION, ElementInfos.InfoType.TAB))
                .shuntCompensators(getElementsInfos(network, substationsId, ElementType.SHUNT_COMPENSATOR, ElementInfos.InfoType.TAB))
                .staticVarCompensators(getElementsInfos(network, substationsId, ElementType.STATIC_VAR_COMPENSATOR, ElementInfos.InfoType.TAB))
                .vscConverterStations(getElementsInfos(network, substationsId, ElementType.VSC_CONVERTER_STATION, ElementInfos.InfoType.TAB))
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
            .map(BusBarSectionFormInfos::toData).collect(Collectors.toList());
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

    private List<ElementInfos> getSubstationsInfos(Network network, List<String> substationsId, ElementInfos.InfoType infoType) {
        Stream<Substation> substations = substationsId == null ? network.getSubstationStream() : substationsId.stream().map(network::getSubstation);
        return substations
                .map(c -> ElementType.SUBSTATION.getInfosGetter().apply(c, infoType))
                .collect(Collectors.toList());
    }

    public List<ElementInfos> getVoltageLevelsInfos(Network network, List<String> substationsId, ElementInfos.InfoType infoType) {
        Stream<VoltageLevel> voltageLevels = substationsId == null ? network.getVoltageLevelStream() : substationsId.stream().flatMap(id -> network.getSubstation(id).getVoltageLevelStream());
        return voltageLevels
                .map(c -> ElementType.VOLTAGE_LEVEL.getInfosGetter().apply(c, infoType))
                .collect(Collectors.toList());
    }

    public List<ElementInfos> getHvdcLinesInfos(Network network, List<String> substationsId, ElementInfos.InfoType infoType) {
        Stream<HvdcLine> hvdcLines = substationsId == null ? network.getHvdcLineStream() :
                substationsId.stream()
                        .map(network::getSubstation)
                        .flatMap(Substation::getVoltageLevelStream)
                        .flatMap(vl -> vl.getConnectableStream(HvdcConverterStation.class))
                        .map(HvdcConverterStation::getHvdcLine)
                        .filter(Objects::nonNull)
                        .distinct();
        return hvdcLines
                .map(c -> ElementType.HVDC_LINE.getInfosGetter().apply(c, infoType))
                .distinct()
                .collect(Collectors.toList());
    }

    private List<ElementInfos> getElementsInfos(Network network, List<String> substationsIds, ElementType elementType, ElementInfos.InfoType infoType) {
        Class<? extends Connectable> elementClass = (Class<? extends Connectable>) elementType.getElementClass();
        Stream<? extends Connectable> connectables = substationsIds == null ?
                network.getConnectableStream(elementClass) :
                substationsIds.stream()
                        .flatMap(substationId -> network.getSubstation(substationId).getVoltageLevelStream())
                        .flatMap(voltageLevel -> voltageLevel.getConnectableStream(elementClass));
        return connectables
                .map(c -> elementType.getInfosGetter().apply(c, infoType))
                .collect(Collectors.toList());
    }

    public List<ElementInfos> getElementsInfos(UUID networkUuid, String variantId, List<String> substationsIds, ElementType equipmentType, ElementInfos.InfoType infoType) {
        Network network = getNetwork(networkUuid, getPreloadingStrategy(substationsIds), variantId);
        switch (equipmentType) {
            case SUBSTATION:
                return getSubstationsInfos(network, substationsIds, infoType);
            case VOLTAGE_LEVEL:
                return getVoltageLevelsInfos(network, substationsIds, infoType);
            case HVDC_LINE:
                return getHvdcLinesInfos(network, substationsIds, infoType);
            default:
                return getElementsInfos(network, substationsIds, equipmentType, infoType);
        }
    }

    public ElementInfos getElementInfos(UUID networkUuid, String variantId, ElementType elementType, ElementInfos.InfoType infoType, String elementId) {
        Network network = getNetwork(networkUuid, PreloadingStrategy.NONE, variantId);
        Identifiable<?> identifiable = network.getIdentifiable(elementId);
        if (identifiable == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return elementType.getInfosGetter().apply(identifiable, infoType);
    }

    // Ideally we should directly call the appropriate method but in some cases we receive only an ID without knowing its type
    public ElementInfos getBranchOrThreeWindingsTransformer(UUID networkUuid, String variantId, String equipmentId) {
        Network network = getNetwork(networkUuid, PreloadingStrategy.NONE, variantId);
        Line line = network.getLine(equipmentId);
        if (line != null) {
            return LineListInfos.toData(line);
        }
        TwoWindingsTransformer twoWT = network.getTwoWindingsTransformer(equipmentId);
        if (twoWT != null) {
            return TwoWindingsTransformerListInfos.toData(twoWT);
        }
        ThreeWindingsTransformer threeWT = network.getThreeWindingsTransformer(equipmentId);
        if (threeWT != null) {
            return ThreeWindingsTransformerListInfos.toData(threeWT);
        }
        throw new ResponseStatusException(HttpStatus.NO_CONTENT);
    }

    private static List<SelectedShuntCompensatorData> toShuntCompensatorData(String lccBusOrBusbarSectionId, Stream<ShuntCompensator> shuntCompensators) {
        return shuntCompensators
                .map(s -> SelectedShuntCompensatorData.builder()
                        .id(s.getId())
                        .selected(Objects.equals(lccBusOrBusbarSectionId, getBusOrBusbarSection(s.getTerminal())))
                        .build())
                .collect(Collectors.toList());
    }

    public HvdcShuntCompensatorInfos getHvcLineWithShuntCompensators(UUID networkUuid, String variantId, String hvdcId) {
        HvdcShuntCompensatorInfos.HvdcShuntCompensatorInfosBuilder builder = HvdcShuntCompensatorInfos.builder().id(hvdcId);
        Network network = getNetwork(networkUuid, PreloadingStrategy.NONE, variantId);
        HvdcLine hvdcLine = network.getHvdcLine(hvdcId);
        if (hvdcLine != null) { // it could be OK to not find the hvdc: can be used in a modification with un-existing hvdc
            builder.hvdcType(hvdcLine.getConverterStation1().getHvdcType());
            if (hvdcLine.getConverterStation1().getHvdcType() == HvdcConverterStation.HvdcType.LCC) {
                Terminal terminalLcc1 = hvdcLine.getConverterStation1().getTerminal();
                builder.mcsOnSide1(toShuntCompensatorData(getBusOrBusbarSection(terminalLcc1), terminalLcc1.getVoltageLevel().getShuntCompensatorStream()));
                Terminal terminalLcc2 = hvdcLine.getConverterStation2().getTerminal();
                builder.mcsOnSide2(toShuntCompensatorData(getBusOrBusbarSection(terminalLcc2), terminalLcc2.getVoltageLevel().getShuntCompensatorStream()));
            }
        }
        return builder.build();
    }

    public List<String> getElementsIds(UUID networkUuid, String variantId, List<String> substationsIds, ElementType elementType) {
        switch (elementType) {
            case GENERATOR:
            case LINE:
            case TWO_WINDINGS_TRANSFORMER:
            case THREE_WINDINGS_TRANSFORMER:
            case BATTERY:
            case DANGLING_LINE:
            case LCC_CONVERTER_STATION:
            case VSC_CONVERTER_STATION:
            case LOAD:
            case SHUNT_COMPENSATOR:
            case STATIC_VAR_COMPENSATOR:
                return getElementsIds(networkUuid, variantId, substationsIds, (Class<? extends Connectable>) elementType.getElementClass());
            case SUBSTATION:
                return getSubstationsIds(networkUuid, variantId);
            case HVDC_LINE:
                return getHvdcLinesIds(networkUuid, variantId, substationsIds);
            case VOLTAGE_LEVEL:
                return getVoltageLevelsIds(networkUuid, variantId, substationsIds);
            default:
                return List.of();
        }
    }
}
