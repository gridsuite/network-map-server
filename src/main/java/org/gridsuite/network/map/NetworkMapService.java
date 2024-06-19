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
import org.gridsuite.network.map.dto.ElementType;
import org.gridsuite.network.map.dto.InfoTypeParameters;
import org.gridsuite.network.map.dto.definition.hvdc.HvdcShuntCompensatorsInfos;
import org.gridsuite.network.map.dto.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
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

    private PreloadingStrategy getPreloadingStrategy(List<String> substationsIds) {
        return substationsIds == null ? PreloadingStrategy.COLLECTION : PreloadingStrategy.NONE;
    }

    public List<String> getSubstationsIds(UUID networkUuid, String variantId) {
        Network network = getNetwork(networkUuid, PreloadingStrategy.COLLECTION, variantId);
        return network.getSubstationStream()
                .map(Substation::getId).collect(Collectors.toList());
    }

    public AllElementsInfos getAllElementsInfos(UUID networkUuid, String variantId, List<String> substationsId) {
        Network network = getNetwork(networkUuid, PreloadingStrategy.COLLECTION, variantId);
        return AllElementsInfos.builder()
                .substations(getSubstationsInfos(network, substationsId, InfoTypeParameters.TAB))
                .voltageLevels(getVoltageLevelsInfos(network, substationsId, InfoTypeParameters.TAB))
                .hvdcLines(getHvdcLinesInfos(network, substationsId, InfoTypeParameters.TAB))
                .lines(getElementsInfos(network, substationsId, ElementType.LINE, InfoTypeParameters.TAB))
                .loads(getElementsInfos(network, substationsId, ElementType.LOAD, InfoTypeParameters.TAB))
                .generators(getElementsInfos(network, substationsId, ElementType.GENERATOR, InfoTypeParameters.TAB))
                .twoWindingsTransformers(getElementsInfos(network, substationsId, ElementType.TWO_WINDINGS_TRANSFORMER, InfoTypeParameters.TAB))
                .threeWindingsTransformers(getElementsInfos(network, substationsId, ElementType.THREE_WINDINGS_TRANSFORMER, InfoTypeParameters.TAB))
                .batteries(getElementsInfos(network, substationsId, ElementType.BATTERY, InfoTypeParameters.TAB))
                .danglingLines(getElementsInfos(network, substationsId, ElementType.DANGLING_LINE, InfoTypeParameters.TAB))
                .tieLines(getTieLinesInfos(network, substationsId, InfoTypeParameters.TAB))
                .lccConverterStations(getElementsInfos(network, substationsId, ElementType.LCC_CONVERTER_STATION, InfoTypeParameters.TAB))
                .shuntCompensators(getElementsInfos(network, substationsId, ElementType.SHUNT_COMPENSATOR, InfoTypeParameters.TAB))
                .staticVarCompensators(getElementsInfos(network, substationsId, ElementType.STATIC_VAR_COMPENSATOR, InfoTypeParameters.TAB))
                .vscConverterStations(getElementsInfos(network, substationsId, ElementType.VSC_CONVERTER_STATION, InfoTypeParameters.TAB))
                .buses(getBusesInfos(network, substationsId, InfoTypeParameters.TAB))
                .build();
    }

    public List<String> getVoltageLevelsIds(UUID networkUuid, String variantId, List<String> substationsIds) {
        Network network = getNetwork(networkUuid, getPreloadingStrategy(substationsIds), variantId);
        return substationsIds == null ?
                network.getVoltageLevelStream().map(VoltageLevel::getId).collect(Collectors.toList()) :
                substationsIds.stream().flatMap(id -> network.getSubstation(id).getVoltageLevelStream().map(VoltageLevel::getId)).collect(Collectors.toList());
    }

    public List<ElementInfos> getVoltageLevelEquipments(UUID networkUuid, String voltageLevelId, String variantId, List<String> substationsId) {
        Network network = getNetwork(networkUuid, getPreloadingStrategy(substationsId), variantId);
        List<VoltageLevel> voltageLevels = substationsId == null ?
                List.of(network.getVoltageLevel(voltageLevelId)) :
                substationsId.stream().flatMap(id -> network.getSubstation(id).getVoltageLevelStream().filter(voltageLevel -> voltageLevelId.equals(voltageLevel.getId()))).collect(Collectors.toList());

        return voltageLevels.stream()
                .flatMap(VoltageLevel::getConnectableStream)
                .map(ElementInfosMapper::toInfosWithType)
                .collect(Collectors.toList());
    }

    public List<ElementInfos> getVoltageLevelBuses(UUID networkUuid, String voltageLevelId, String variantId) {
        Network network = getNetwork(networkUuid, PreloadingStrategy.NONE, variantId);
        return network.getVoltageLevel(voltageLevelId).getBusBreakerView().getBusStream()
                .map(BusInfosMapper::toListInfos).collect(Collectors.toList());
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

    public List<String> getTieLinesIds(UUID networkUuid, String variantId, List<String> substationsIds) {
        Network network = getNetwork(networkUuid, getPreloadingStrategy(substationsIds), variantId);
        if (substationsIds == null) {
            return network.getTieLineStream()
                    .map(TieLine::getId).toList();
        } else {
            return substationsIds.stream()
                    .flatMap(substationsId -> network.getSubstation(substationsId).getVoltageLevelStream())
                    .flatMap(voltageLevel -> voltageLevel.getConnectableStream(DanglingLine.class))
                    .map(DanglingLine::getTieLine)
                    .flatMap(Optional::stream)
                    .map(TieLine::getId)
                    .distinct()
                    .toList();
        }
    }

    private List<ElementInfos> getSubstationsInfos(Network network, List<String> substationsId, InfoTypeParameters infoTypeParameters) {
        Stream<Substation> substations = substationsId == null ? network.getSubstationStream() : substationsId.stream().map(network::getSubstation);
        return substations
                .map(c -> ElementType.SUBSTATION.getInfosGetter().apply(c, infoTypeParameters))
                .collect(Collectors.toList());
    }

    public List<ElementInfos> getVoltageLevelsInfos(Network network, List<String> substationsId, InfoTypeParameters infoTypeParameters) {
        Stream<VoltageLevel> voltageLevels = substationsId == null ? network.getVoltageLevelStream() : substationsId.stream().flatMap(id -> network.getSubstation(id).getVoltageLevelStream());
        return voltageLevels
                .map(c -> ElementType.VOLTAGE_LEVEL.getInfosGetter().apply(c, infoTypeParameters))
                .collect(Collectors.toList());
    }

    public List<ElementInfos> getHvdcLinesInfos(Network network, List<String> substationsId, InfoTypeParameters infoTypeParameters) {
        Stream<HvdcLine> hvdcLines = substationsId == null ? network.getHvdcLineStream() :
                substationsId.stream()
                        .map(network::getSubstation)
                        .flatMap(Substation::getVoltageLevelStream)
                        .flatMap(vl -> vl.getConnectableStream(HvdcConverterStation.class))
                        .map(HvdcConverterStation::getHvdcLine)
                        .filter(Objects::nonNull)
                        .distinct();
        return hvdcLines
                .map(c -> ElementType.HVDC_LINE.getInfosGetter().apply(c, infoTypeParameters))
                .distinct()
                .collect(Collectors.toList());
    }

    public List<ElementInfos> getTieLinesInfos(Network network, List<String> substationsId, InfoTypeParameters infoTypeParameters) {
        Stream<TieLine> tieLines = substationsId == null ? network.getTieLineStream() :
                substationsId.stream()
                        .map(network::getSubstation)
                        .flatMap(Substation::getVoltageLevelStream)
                        .flatMap(vl -> vl.getConnectableStream(DanglingLine.class))
                        .map(DanglingLine::getTieLine)
                        .flatMap(Optional::stream)
                        .distinct();
        return tieLines
                .map(c -> ElementType.TIE_LINE.getInfosGetter().apply(c, infoTypeParameters))
                .distinct()
                .toList();
    }

    public List<ElementInfos> getBusesInfos(Network network, List<String> substationsId, InfoTypeParameters infoTypeParameters) {
        Stream<Bus> buses = substationsId == null ? network.getBusView().getBusStream() :
                network.getBusView().getBusStream()
                        .filter(bus -> bus.getVoltageLevel().getSubstation().stream().anyMatch(substation -> substationsId.contains(substation.getId())))
                        .filter(Objects::nonNull)
                        .distinct();
        return buses
                .map(c -> ElementType.BUS.getInfosGetter().apply(c, infoTypeParameters))
                .distinct()
                .toList();
    }

    private List<ElementInfos> getElementsInfos(Network network, List<String> substationsIds, ElementType elementType, InfoTypeParameters infoTypeParameters) {
        Class<? extends Connectable> elementClass = (Class<? extends Connectable>) elementType.getElementClass();
        Stream<? extends Connectable> connectables = substationsIds == null ?
                getConnectableStream(network, elementType) :
                substationsIds.stream()
                        .flatMap(substationId -> network.getSubstation(substationId).getVoltageLevelStream())
                        .flatMap(voltageLevel -> voltageLevel.getConnectableStream(elementClass))
                        .distinct();
        return connectables
                .map(c -> elementType.getInfosGetter().apply(c, infoTypeParameters))
                .collect(Collectors.toList());
    }

    public List<ElementInfos> getElementsInfos(UUID networkUuid, String variantId, List<String> substationsIds, ElementType equipmentType, InfoTypeParameters infoTypeParameters) {
        Network network = getNetwork(networkUuid, getPreloadingStrategy(substationsIds), variantId);
        switch (equipmentType) {
            case SUBSTATION:
                return getSubstationsInfos(network, substationsIds, infoTypeParameters);
            case VOLTAGE_LEVEL:
                return getVoltageLevelsInfos(network, substationsIds, infoTypeParameters);
            case HVDC_LINE:
                return getHvdcLinesInfos(network, substationsIds, infoTypeParameters);
            case TIE_LINE:
                return getTieLinesInfos(network, substationsIds, infoTypeParameters);
            case BUS:
                return getBusesInfos(network, substationsIds, infoTypeParameters);
            default:
                return getElementsInfos(network, substationsIds, equipmentType, infoTypeParameters);
        }
    }

    public ElementInfos getElementInfos(UUID networkUuid, String variantId, ElementType elementType, InfoTypeParameters infoTypeParameters, String elementId) {
        Network network = getNetwork(networkUuid, PreloadingStrategy.NONE, variantId);
        Identifiable<?> identifiable = network.getIdentifiable(elementId);
        if (identifiable == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return elementType.getInfosGetter().apply(identifiable, infoTypeParameters);
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
            case TIE_LINE:
                return getTieLinesIds(networkUuid, variantId, substationsIds);
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
            case BUS:
                return network.getBusbarSectionStream();
            default:
                throw new IllegalStateException("Unexpected connectable type:" + elementType);
        }
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
