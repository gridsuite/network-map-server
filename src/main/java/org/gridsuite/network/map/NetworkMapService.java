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
import org.gridsuite.network.map.dto.*;
import org.gridsuite.network.map.dto.definition.hvdc.HvdcShuntCompensatorsInfos;
import org.gridsuite.network.map.dto.mapper.ElementInfosMapper;
import org.gridsuite.network.map.dto.mapper.HvdcInfosMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@ComponentScan(basePackageClasses = {NetworkStoreService.class})
@Service
public class NetworkMapService {

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

    private static PreloadingStrategy getPreloadingStrategy(@NonNull List<String> substationsIds) {
        return substationsIds.isEmpty() ? PreloadingStrategy.COLLECTION : PreloadingStrategy.NONE;
    }

    public List<String> getSubstationsIds(UUID networkUuid, String variantId, List<Double> nominalVoltages) {
        Network network = getNetwork(networkUuid, PreloadingStrategy.COLLECTION, variantId);
        return network.getSubstationStream()
                .filter(substation -> nominalVoltages == null ||
                        substation.getVoltageLevelStream().anyMatch(voltageLevel -> nominalVoltages.contains(voltageLevel.getNominalV())))
                .map(Substation::getId).toList();
    }

    public AllElementsInfos getAllElementsInfos(UUID networkUuid, String variantId, @NonNull List<String> substationsId) {
        Network network = getNetwork(networkUuid, PreloadingStrategy.ALL_COLLECTIONS_NEEDED_FOR_BUS_VIEW, variantId);
        return AllElementsInfos.builder()
                .substations(getSubstationsInfos(network, substationsId, InfoTypeParameters.TAB, null))
                .voltageLevels(getVoltageLevelsInfos(network, substationsId, InfoTypeParameters.TAB, null))
                .hvdcLines(getHvdcLinesInfos(network, substationsId, InfoTypeParameters.TAB, null))
                .lines(getElementsInfos(network, substationsId, ElementType.LINE, InfoTypeParameters.TAB, null))
                .loads(getElementsInfos(network, substationsId, ElementType.LOAD, InfoTypeParameters.TAB, null))
                .generators(getElementsInfos(network, substationsId, ElementType.GENERATOR, InfoTypeParameters.TAB, null))
                .twoWindingsTransformers(getElementsInfos(network, substationsId, ElementType.TWO_WINDINGS_TRANSFORMER, InfoTypeParameters.TAB, null))
                .threeWindingsTransformers(getElementsInfos(network, substationsId, ElementType.THREE_WINDINGS_TRANSFORMER, InfoTypeParameters.TAB, null))
                .batteries(getElementsInfos(network, substationsId, ElementType.BATTERY, InfoTypeParameters.TAB, null))
                .danglingLines(getElementsInfos(network, substationsId, ElementType.DANGLING_LINE, InfoTypeParameters.TAB, null))
                .tieLines(getTieLinesInfos(network, substationsId, InfoTypeParameters.TAB, null))
                .lccConverterStations(getElementsInfos(network, substationsId, ElementType.LCC_CONVERTER_STATION, InfoTypeParameters.TAB, null))
                .shuntCompensators(getElementsInfos(network, substationsId, ElementType.SHUNT_COMPENSATOR, InfoTypeParameters.TAB, null))
                .staticVarCompensators(getElementsInfos(network, substationsId, ElementType.STATIC_VAR_COMPENSATOR, InfoTypeParameters.TAB, null))
                .vscConverterStations(getElementsInfos(network, substationsId, ElementType.VSC_CONVERTER_STATION, InfoTypeParameters.TAB, null))
                .buses(getBusesInfos(network, substationsId, InfoTypeParameters.TAB))
                .busbarSections(getElementsInfos(network, substationsId, ElementType.BUSBAR_SECTION, InfoTypeParameters.TAB, null))
                .build();
    }

    private List<String> getVoltageLevelsIds(UUID networkUuid, String variantId, @NonNull List<String> substationsIds, List<Double> nominalVoltages) {
        Network network = getNetwork(networkUuid, getPreloadingStrategy(substationsIds), variantId);
        return getVoltageLevelStream(network, substationsIds, nominalVoltages).map(VoltageLevel::getId).toList();
    }

    public List<ElementInfos> getVoltageLevelEquipments(UUID networkUuid, String voltageLevelId, String variantId) {
        Network network = getNetwork(networkUuid, PreloadingStrategy.NONE, variantId);
        return network.getVoltageLevel(voltageLevelId).getConnectableStream()
                .map(ElementInfosMapper::toInfosWithType)
                .collect(Collectors.toList());
    }

    public List<ElementInfos> getVoltageLevelBusesOrBusbarSections(UUID networkUuid, String voltageLevelId, String variantId) {
        Network network = getNetwork(networkUuid, PreloadingStrategy.NONE, variantId);
        TopologyKind topologyKind = network.getVoltageLevel(voltageLevelId).getTopologyKind();
        return switch (topologyKind) {
            case NODE_BREAKER -> network.getVoltageLevel(voltageLevelId).getNodeBreakerView().getBusbarSectionStream()
                .map(ElementInfosMapper::toListInfos).toList();
            case BUS_BREAKER -> network.getVoltageLevel(voltageLevelId).getBusBreakerView().getBusStream()
                .map(ElementInfosMapper::toListInfos).collect(Collectors.toList());
        };
    }

    public List<ElementInfosWithSwitchStatus> getVoltageLevelSwitches(UUID networkUuid, String voltageLevelId, String variantId) {
        Network network = getNetwork(networkUuid, PreloadingStrategy.NONE, variantId);

        List<ElementInfosWithSwitchStatus> switchInfosList = new ArrayList<>();
        network.getVoltageLevel(voltageLevelId).getSwitches().forEach(sw ->
                switchInfosList.add(ElementInfosWithSwitchStatus.builder()
                        .id(sw.getId())
                        .open(sw.isOpen())
                        .build()));
        return switchInfosList;
    }

    public String getVoltageLevelSubstationID(UUID networkUuid, String voltageLevelId, String variantId) {
        Network network = getNetwork(networkUuid, PreloadingStrategy.NONE, variantId);
        return network.getVoltageLevel(voltageLevelId).getSubstation().map(Substation::getId).orElse(null);
    }

    public List<String> getVoltageLevelBusbarSectionsIds(UUID networkUuid, String voltageLevelId, String variantId) {
        Network network = getNetwork(networkUuid, PreloadingStrategy.NONE, variantId);
        return network.getVoltageLevel(voltageLevelId).getNodeBreakerView().getBusbarSectionStream()
                .map(BusbarSection::getId).collect(Collectors.toList());
    }

    private List<String> getHvdcLinesIdsByHvdcType(UUID networkUuid, String variantId, @NonNull List<String> substationsIds, List<Double> nominalVoltages, HvdcConverterStation.HvdcType type) {
        Network network = getNetwork(networkUuid, getPreloadingStrategy(substationsIds), variantId);
        Stream<HvdcLine> hvdcLineStream = (substationsIds.isEmpty() && nominalVoltages == null)
                ? network.getHvdcLineStream()
                : getVoltageLevelStream(network, substationsIds, nominalVoltages)
                .flatMap(voltageLevel -> voltageLevel.getConnectableStream(HvdcConverterStation.class))
                .map(HvdcConverterStation::getHvdcLine)
                .filter(Objects::nonNull)
                .distinct();
        if (type != null) {
            hvdcLineStream = hvdcLineStream.filter(hvdcLine -> hvdcLine.getConverterStation1().getHvdcType() == type);
        }
        return hvdcLineStream.map(HvdcLine::getId).toList();
    }

    private List<String> getHvdcLinesIds(UUID networkUuid, String variantId, @NonNull List<String> substationsIds, List<Double> nominalVoltages) {
        return getHvdcLinesIdsByHvdcType(networkUuid, variantId, substationsIds, nominalVoltages, null);
    }

    private List<String> getHvdcLinesLccIds(UUID networkUuid, String variantId, @NonNull List<String> substationsIds, List<Double> nominalVoltages) {
        return getHvdcLinesIdsByHvdcType(networkUuid, variantId, substationsIds, nominalVoltages, HvdcConverterStation.HvdcType.LCC);
    }

    private List<String> getHvdcLinesVscIds(UUID networkUuid, String variantId, @NonNull List<String> substationsIds, List<Double> nominalVoltages) {
        return getHvdcLinesIdsByHvdcType(networkUuid, variantId, substationsIds, nominalVoltages, HvdcConverterStation.HvdcType.VSC);
    }

    private List<String> getTieLinesIds(UUID networkUuid, String variantId, @NonNull List<String> substationsIds, List<Double> nominalVoltages) {
        Network network = getNetwork(networkUuid, getPreloadingStrategy(substationsIds), variantId);
        if (substationsIds.isEmpty() && nominalVoltages == null) {
            return network.getTieLineStream()
                    .map(TieLine::getId).toList();
        } else {
            return getVoltageLevelStream(network, substationsIds, nominalVoltages)
                    .flatMap(voltageLevel -> voltageLevel.getConnectableStream(DanglingLine.class))
                    .map(DanglingLine::getTieLine)
                    .flatMap(Optional::stream)
                    .map(TieLine::getId)
                    .distinct()
                    .toList();
        }
    }

    private List<ElementInfos> getSubstationsInfos(Network network, @NonNull List<String> substationsId, InfoTypeParameters infoTypeParameters, List<Double> nominalVoltages) {
        Stream<Substation> substations = substationsId.isEmpty() ? network.getSubstationStream() : substationsId.stream().map(network::getSubstation);
        return substations
                .filter(substation -> nominalVoltages == null ||
                        substation.getVoltageLevelStream().anyMatch(voltageLevel -> nominalVoltages.contains(voltageLevel.getNominalV())))
                .map(c -> ElementType.SUBSTATION.getInfosGetter().apply(c, infoTypeParameters))
                .toList();
    }

    private List<ElementInfos> getVoltageLevelsInfos(Network network, @NonNull List<String> substationsId, InfoTypeParameters infoTypeParameters, List<Double> nominalVoltages) {
        return getVoltageLevelStream(network, substationsId, nominalVoltages)
                .map(c -> ElementType.VOLTAGE_LEVEL.getInfosGetter().apply(c, infoTypeParameters))
                .toList();
    }

    private List<ElementInfos> getHvdcLinesInfosByHvdcType(Network network, @NonNull List<String> substationsId, InfoTypeParameters infoTypeParameters, List<Double> nominalVoltages, HvdcConverterStation.HvdcType type) {
        Stream<HvdcLine> hvdcLineStream = substationsId.isEmpty()
                ? network.getHvdcLineStream()
                : substationsId.stream()
                .map(network::getSubstation)
                .flatMap(Substation::getVoltageLevelStream)
                .filter(voltageLevel -> nominalVoltages == null || nominalVoltages.contains(voltageLevel.getNominalV()))
                .flatMap(vl -> vl.getConnectableStream(HvdcConverterStation.class))
                .map(HvdcConverterStation::getHvdcLine)
                .filter(Objects::nonNull)
                .distinct();
        if (type != null) {
            hvdcLineStream = hvdcLineStream.filter(hvdcLine -> hvdcLine.getConverterStation1().getHvdcType() == type);
        }
        return hvdcLineStream.map(c -> getElementTypeByHvdcType(type).getInfosGetter().apply(c, infoTypeParameters)).toList();
    }

    private ElementType getElementTypeByHvdcType(HvdcConverterStation.HvdcType type) {
        if (type == HvdcConverterStation.HvdcType.LCC) {
            return ElementType.HVDC_LINE_LCC;
        } else if (type == HvdcConverterStation.HvdcType.VSC) {
            return ElementType.HVDC_LINE_VSC;
        } else {
            return ElementType.HVDC_LINE;
        }
    }

    private List<ElementInfos> getHvdcLinesInfos(Network network, @NonNull List<String> substationsId, InfoTypeParameters infoTypeParameters, List<Double> nominalVoltages) {
        return getHvdcLinesInfosByHvdcType(network, substationsId, infoTypeParameters, nominalVoltages, null);
    }

    private List<ElementInfos> getHvdcLinesLccInfos(Network network, @NonNull List<String> substationsId, InfoTypeParameters infoTypeParameters, List<Double> nominalVoltages) {
        return getHvdcLinesInfosByHvdcType(network, substationsId, infoTypeParameters, nominalVoltages, HvdcConverterStation.HvdcType.LCC);
    }

    private List<ElementInfos> getHvdcLinesVscInfos(Network network, @NonNull List<String> substationsId, InfoTypeParameters infoTypeParameters, List<Double> nominalVoltages) {
        return getHvdcLinesInfosByHvdcType(network, substationsId, infoTypeParameters, nominalVoltages, HvdcConverterStation.HvdcType.VSC);
    }

    private List<ElementInfos> getTieLinesInfos(Network network, @NonNull List<String> substationsId, InfoTypeParameters infoTypeParameters, List<Double> nominalVoltages) {
        Stream<TieLine> tieLines = substationsId.isEmpty() ? network.getTieLineStream() :
                substationsId.stream()
                        .map(network::getSubstation)
                        .flatMap(Substation::getVoltageLevelStream)
                        .filter(voltageLevel -> nominalVoltages == null || nominalVoltages.contains(voltageLevel.getNominalV()))
                        .flatMap(vl -> vl.getConnectableStream(DanglingLine.class))
                        .map(DanglingLine::getTieLine)
                        .flatMap(Optional::stream)
                        .distinct();
        return tieLines
                .map(c -> ElementType.TIE_LINE.getInfosGetter().apply(c, infoTypeParameters))
                .distinct()
                .toList();
    }

    private List<ElementInfos> getBusesInfos(Network network, @NonNull List<String> substationsId, InfoTypeParameters infoTypeParameters) {
        Stream<Bus> buses = substationsId.isEmpty() ? network.getBusView().getBusStream() :
                network.getBusView().getBusStream()
                        .filter(bus -> bus.getVoltageLevel().getSubstation().stream().anyMatch(substation -> substationsId.contains(substation.getId())))
                        .filter(Objects::nonNull)
                        .distinct();
        return buses
                .map(c -> ElementType.BUS.getInfosGetter().apply(c, infoTypeParameters))
                .distinct()
                .toList();
    }

    private List<ElementInfos> getElementsInfos(Network network, @NonNull List<String> substationsIds, ElementType elementType, InfoTypeParameters infoTypeParameters, List<Double> nominalVoltages) {
        Class<? extends Connectable> elementClass = (Class<? extends Connectable>) elementType.getElementClass();
        Stream<? extends Connectable> connectables = substationsIds.isEmpty() ?
                getConnectableStream(network, elementType) :
                substationsIds.stream()
                        .flatMap(substationId -> network.getSubstation(substationId).getVoltageLevelStream())
                        .filter(voltageLevel -> (elementType != ElementType.BUSBAR_SECTION || voltageLevel.getTopologyKind() != TopologyKind.BUS_BREAKER)
                            && (nominalVoltages == null || nominalVoltages.contains(voltageLevel.getNominalV())))
                        .flatMap(voltageLevel -> voltageLevel.getConnectableStream(elementClass))
                        .distinct();
        return connectables
                .map(c -> elementType.getInfosGetter().apply(c, infoTypeParameters))
                .collect(Collectors.toList());
    }

    public List<ElementInfos> getElementsInfos(UUID networkUuid, String variantId, @NonNull List<String> substationsIds, ElementType equipmentType, InfoTypeParameters infoTypeParameters, List<Double> nominalVoltages) {
        Network network = getNetwork(networkUuid, getPreloadingStrategy(substationsIds), variantId);
        return switch (equipmentType) {
            case SUBSTATION -> getSubstationsInfos(network, substationsIds, infoTypeParameters, nominalVoltages);
            case VOLTAGE_LEVEL -> getVoltageLevelsInfos(network, substationsIds, infoTypeParameters, nominalVoltages);
            case HVDC_LINE -> getHvdcLinesInfos(network, substationsIds, infoTypeParameters, nominalVoltages);
            case HVDC_LINE_LCC -> getHvdcLinesLccInfos(network, substationsIds, infoTypeParameters, nominalVoltages);
            case HVDC_LINE_VSC -> getHvdcLinesVscInfos(network, substationsIds, infoTypeParameters, nominalVoltages);
            case TIE_LINE -> getTieLinesInfos(network, substationsIds, infoTypeParameters, nominalVoltages);
            case BUS -> getBusesInfos(network, substationsIds, infoTypeParameters);
            default -> getElementsInfos(network, substationsIds, equipmentType, infoTypeParameters, nominalVoltages);
        };
    }

    public ElementInfos getElementInfos(UUID networkUuid, String variantId, ElementType elementType, InfoTypeParameters infoTypeParameters, String elementId) {
        Network network = getNetwork(networkUuid, PreloadingStrategy.NONE, variantId);
        Identifiable<?> identifiable = network.getIdentifiable(elementId);
        if (identifiable == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return elementType.getInfosGetter().apply(identifiable, infoTypeParameters);
    }

    public String getBranchOr3WTVoltageLevelId(UUID networkUuid, String variantId, String equipmentId, ThreeSides side) {
        Network network = getNetwork(networkUuid, PreloadingStrategy.NONE, variantId);
        Branch<?> branch = network.getBranch(equipmentId);
        if (branch != null) {
            return branch.getTerminal(side.toTwoSides()).getVoltageLevel().getId();
        }
        ThreeWindingsTransformer threeWT = network.getThreeWindingsTransformer(equipmentId);
        if (threeWT != null) {
            return threeWT.getLeg(side).getTerminal().getVoltageLevel().getId();
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

    public List<String> getElementsIds(UUID networkUuid, String variantId, @NonNull List<String> substationsIds, ElementType elementType, List<Double> nominalVoltages) {
        return switch (elementType) {
            case SUBSTATION -> getSubstationsIds(networkUuid, variantId, nominalVoltages);
            case TIE_LINE -> getTieLinesIds(networkUuid, variantId, substationsIds, nominalVoltages);
            case HVDC_LINE -> getHvdcLinesIds(networkUuid, variantId, substationsIds, nominalVoltages);
            case HVDC_LINE_LCC -> getHvdcLinesLccIds(networkUuid, variantId, substationsIds, nominalVoltages);
            case HVDC_LINE_VSC -> getHvdcLinesVscIds(networkUuid, variantId, substationsIds, nominalVoltages);
            case VOLTAGE_LEVEL -> getVoltageLevelsIds(networkUuid, variantId, substationsIds, nominalVoltages);
            default -> getConnectablesIds(networkUuid, variantId, substationsIds, elementType, nominalVoltages);
        };
    }

    private List<String> getConnectablesIds(UUID networkUuid, String variantId, @NonNull List<String> substationsIds, ElementType elementType, List<Double> nominalVoltages) {
        Network network = getNetwork(networkUuid, getPreloadingStrategy(substationsIds), variantId);
        if (substationsIds.isEmpty() && nominalVoltages == null) {
            return getConnectableStream(network, elementType)
                    .map(Connectable::getId)
                    .toList();
        } else {
            return getVoltageLevelStream(network, substationsIds, nominalVoltages)
                    .flatMap(voltageLevel -> voltageLevel.getConnectableStream((Class<? extends Connectable>) elementType.getElementClass()))
                    .map(Connectable::getId)
                    .distinct()
                    .collect(Collectors.toList());
        }
    }

    private Stream<VoltageLevel> getVoltageLevelStream(Network network, @NonNull List<String> substationsIds, List<Double> nominalVoltages) {
        Stream<VoltageLevel> voltageLevelStream =
                substationsIds.isEmpty() ?
                        network.getVoltageLevelStream() :
                        substationsIds.stream().flatMap(substationId -> network.getSubstation(substationId).getVoltageLevelStream());
        return voltageLevelStream.filter(voltageLevel -> nominalVoltages == null || nominalVoltages.contains(voltageLevel.getNominalV()));
    }

    public Stream<? extends Connectable> getConnectableStream(Network network, ElementType elementType) {
        return switch (elementType) {
            case BUSBAR_SECTION, BUS -> network.getBusbarSectionStream();
            case GENERATOR -> network.getGeneratorStream();
            case LINE -> network.getLineStream();
            case TWO_WINDINGS_TRANSFORMER -> network.getTwoWindingsTransformerStream();
            case THREE_WINDINGS_TRANSFORMER -> network.getThreeWindingsTransformerStream();
            case BATTERY -> network.getBatteryStream();
            case DANGLING_LINE -> network.getDanglingLineStream();
            case LCC_CONVERTER_STATION -> network.getLccConverterStationStream();
            case VSC_CONVERTER_STATION -> network.getVscConverterStationStream();
            case LOAD -> network.getLoadStream();
            case SHUNT_COMPENSATOR -> network.getShuntCompensatorStream();
            case STATIC_VAR_COMPENSATOR -> network.getStaticVarCompensatorStream();
            default -> throw new IllegalStateException("Unexpected connectable type:" + elementType);
        };
    }

    public Set<Country> getCountries(UUID networkUuid, String variantId) {
        Network network = getNetwork(networkUuid, PreloadingStrategy.COLLECTION, variantId);
        return network.getSubstationStream()
                .map(Substation::getCountry)
                .flatMap(Optional::stream)
                .collect(Collectors.toSet());
    }

    public Set<Double> getNominalVoltages(UUID networkUuid, String variantId) {
        Network network = getNetwork(networkUuid, PreloadingStrategy.COLLECTION, variantId);
        return network.getVoltageLevelStream()
                .map(VoltageLevel::getNominalV)
                .collect(Collectors.toSet());
    }
}
