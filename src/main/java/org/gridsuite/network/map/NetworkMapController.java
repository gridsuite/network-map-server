/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.gridsuite.network.map.dto.ElementInfos;
import org.gridsuite.network.map.dto.View;
import org.gridsuite.network.map.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@RestController
@RequestMapping(value = "/" + NetworkMapController.API_VERSION + "/")
@Tag(name = "network-map-server")
@ComponentScan(basePackageClasses = NetworkMapService.class)
public class NetworkMapController {

    public static final String API_VERSION = "v1";

    @Autowired
    private NetworkMapService networkMapService;

    @GetMapping(value = "/networks/{networkUuid}/generators", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get generators description")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Generators description")})
    public @ResponseBody List<GeneratorMapData> getGenerators(@Parameter(description = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                                              @Parameter(description = "Variant Id") @RequestParam(name = "variantId", required = false) String variantId,
                                                              @Parameter(description = "Substations id") @RequestParam(name = "substationId", required = false) List<String> substationsIds) {
        return networkMapService.getGenerators(networkUuid, variantId, substationsIds);
    }

    @GetMapping(value = "/networks/{networkUuid}/elements-ids", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get equipments ids")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Equipments ids")})
    public @ResponseBody List<String> getEquipmentsIds(@Parameter(description = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                                       @Parameter(description = "Variant Id") @RequestParam(name = "variantId", required = false) String variantId,
                                                       @Parameter(description = "Substations ids") @RequestParam(name = "substationsIds", required = false) List<String> substationsIds,
                                                       @Parameter(description = "Element type") @RequestParam(name = "elementType") ElementType elementType) {
        return networkMapService.getElementsIds(networkUuid, variantId, substationsIds, elementType);
    }

    @GetMapping(value = "/networks/{networkUuid}/generators/{generatorId}", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get generator description")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Generator description")})
    public @ResponseBody GeneratorMapData getGenerator(@Parameter(description = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                             @Parameter(description = "Generator id") @PathVariable("generatorId") String generatorId,
                                             @Parameter(description = "Variant Id") @RequestParam(name = "variantId", required = false) String variantId) {
        return networkMapService.getGenerator(networkUuid, variantId, generatorId);
    }

    @GetMapping(value = "/networks/{networkUuid}/2-windings-transformers", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get 2 windings transformers description")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "2 windings transformers description")})
    public @ResponseBody List<TwoWindingsTransformerMapData> getTwoWindingsTransformers(@Parameter(description = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                                                                        @Parameter(description = "Variant Id") @RequestParam(name = "variantId", required = false) String variantId,
                                                                                        @Parameter(description = "Substations id") @RequestParam(name = "substationId", required = false) List<String> substationsIds) {
        return networkMapService.getTwoWindingsTransformers(networkUuid, variantId, substationsIds);
    }

    @GetMapping(value = "/networks/{networkUuid}/2-windings-transformers/{twoWindingsTransformerId}", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get 2 windings transformer description")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "2 windings transformer description")})
    public @ResponseBody TwoWindingsTransformerMapData getTwoWindingsTransformer(@Parameter(description = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                                                                 @Parameter(description = "Two windings transformer id") @PathVariable("twoWindingsTransformerId") String twoWindingsTransformerId,
                                                                                        @Parameter(description = "Variant Id") @RequestParam(name = "variantId", required = false) String variantId) {
        return networkMapService.getTwoWindingsTransformer(networkUuid, variantId, twoWindingsTransformerId);
    }

    @GetMapping(value = "/networks/{networkUuid}/3-windings-transformers", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get 3 windings transformers description")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "3 windings transformers description")})
    public @ResponseBody List<ThreeWindingsTransformerMapData> getThreeWindingsTransformers(@Parameter(description = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                                                                            @Parameter(description = "Variant Id") @RequestParam(name = "variantId", required = false) String variantId,
                                                                                            @Parameter(description = "Substations id") @RequestParam(name = "substationId", required = false) List<String> substationsIds) {
        return networkMapService.getThreeWindingsTransformers(networkUuid, variantId, substationsIds);
    }

    @GetMapping(value = "/networks/{networkUuid}/all", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get all equipments descriptions")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "all equipments descriptions")})
    public @ResponseBody AllMapData getAll(@Parameter(description = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                           @Parameter(description = "Variant Id") @RequestParam(name = "variantId", required = false) String variantId,
                                           @Parameter(description = "Substations id") @RequestParam(name = "substationId", required = false) List<String> substationsIds) {
        return networkMapService.getAll(networkUuid, variantId, substationsIds);
    }

    @GetMapping(value = "/networks/{networkUuid}/batteries", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get batteries description")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Batteries description")})
    public @ResponseBody List<BatteryMapData> getBatteries(@Parameter(description = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                                           @Parameter(description = "Variant Id") @RequestParam(name = "variantId", required = false) String variantId,
                                                           @Parameter(description = "Substations id") @RequestParam(name = "substationId", required = false) List<String> substationsIds) {
        return networkMapService.getBatteries(networkUuid, variantId, substationsIds);
    }

    @GetMapping(value = "/networks/{networkUuid}/dangling-lines", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get dangling lines description")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Dangling lines description")})
    public @ResponseBody List<DanglingLineMapData> getDanglingLines(@Parameter(description = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                                                    @Parameter(description = "Variant Id") @RequestParam(name = "variantId", required = false) String variantId,
                                                                    @Parameter(description = "Substations id") @RequestParam(name = "substationId", required = false) List<String> substationsIds) {
        return networkMapService.getDanglingLines(networkUuid, variantId, substationsIds);
    }

    @GetMapping(value = "/networks/{networkUuid}/lcc-converter-stations", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get lcc converter stations description")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Lcc converter stations description")})
    public @ResponseBody List<LccConverterStationMapData> getLccConverterStations(@Parameter(description = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                                                                  @Parameter(description = "Variant Id") @RequestParam(name = "variantId", required = false) String variantId,
                                                                                  @Parameter(description = "Substations id") @RequestParam(name = "substationId", required = false) List<String> substationsIds) {
        return networkMapService.getLccConverterStations(networkUuid, variantId, substationsIds);
    }

    @GetMapping(value = "/networks/{networkUuid}/elements/list_view", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get network elements")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Elements description")})
    @JsonView(View.ListView.class)
    public @ResponseBody List<ElementInfos> getListViewElementsInfos(@Parameter(description = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                                                     @Parameter(description = "Variant Id") @RequestParam(name = "variantId", required = false) String variantId,
                                                                     @Parameter(description = "Substations id") @RequestParam(name = "substationsIds", required = false) List<String> substationsIds,
                                                                     @Parameter(description = "Element type") @RequestParam(name = "elementType") ElementType elementType,
                                                                     @Parameter(description = "Info type") @RequestParam(name = "infoType") ElementInfos.InfoType infoType) {
        return networkMapService.getElementsInfos(networkUuid, variantId, substationsIds, elementType, infoType);
    }

    @GetMapping(value = "/networks/{networkUuid}/elements", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get network elements")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Elements description")})
    public @ResponseBody List<ElementInfos> getElementsInfos(@Parameter(description = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                                             @Parameter(description = "Variant Id") @RequestParam(name = "variantId", required = false) String variantId,
                                                             @Parameter(description = "Substations id") @RequestParam(name = "substationsIds", required = false) List<String> substationsIds,
                                                             @Parameter(description = "Element type") @RequestParam(name = "elementType") ElementType elementType,
                                                             @Parameter(description = "Info type") @RequestParam(name = "infoType") ElementInfos.InfoType infoType) {
        return networkMapService.getElementsInfos(networkUuid, variantId, substationsIds, elementType, infoType);
    }

    @GetMapping(value = "/networks/{networkUuid}/elements/{elementId}", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get network element infos")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Network Element infos")})
    public @ResponseBody ElementInfos getElementInfos(@Parameter(description = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                                      @Parameter(description = "Element id") @PathVariable("elementId") String elementId,
                                                      @Parameter(description = "Variant Id") @RequestParam(name = "variantId", required = false) String variantId,
                                                      @Parameter(description = "Element type") @RequestParam(name = "elementType") ElementType elementType,
                                                      @Parameter(description = "Info type") @RequestParam(name = "infoType") ElementInfos.InfoType infoType) {
        return networkMapService.getElementInfos(networkUuid, variantId, elementType, infoType, elementId);
    }

    @GetMapping(value = "/networks/{networkUuid}/shunt-compensators", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get shunt compensators description")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Shunt compensators description")})
    public @ResponseBody List<ShuntCompensatorMapData> getShuntCompensators(@Parameter(description = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                                                            @Parameter(description = "Variant Id") @RequestParam(name = "variantId", required = false) String variantId,
                                                                            @Parameter(description = "Substations id") @RequestParam(name = "substationId", required = false) List<String> substationsIds) {
        return networkMapService.getShuntCompensators(networkUuid, variantId, substationsIds);
    }

    @GetMapping(value = "/networks/{networkUuid}/shunt-compensators/{shuntCompensatorId}", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get shunt compensator description")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Shunt compensator description")})
    public @ResponseBody ShuntCompensatorMapData getShuntCompensator(@Parameter(description = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                             @Parameter(description = "Shunt compensator id") @PathVariable("shuntCompensatorId") String shuntCompensatorId,
                                             @Parameter(description = "Variant Id") @RequestParam(name = "variantId", required = false) String variantId) {
        return networkMapService.getShuntCompensator(networkUuid, variantId, shuntCompensatorId);
    }

    @GetMapping(value = "/networks/{networkUuid}/static-var-compensators", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get static var compensators description")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Static var compensators description")})
    public @ResponseBody List<StaticVarCompensatorMapData> getStaticVarCompensators(@Parameter(description = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                                                                    @Parameter(description = "Variant Id") @RequestParam(name = "variantId", required = false) String variantId,
                                                                                    @Parameter(description = "Substations id") @RequestParam(name = "substationId", required = false) List<String> substationsIds) {
        return networkMapService.getStaticVarCompensators(networkUuid, variantId, substationsIds);
    }

    @GetMapping(value = "/networks/{networkUuid}/vsc-converter-stations", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get vsc converter stations description")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Vsc converter stations description")})
    public @ResponseBody List<VscConverterStationMapData> getVscConverterStations(@Parameter(description = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                                                                  @Parameter(description = "Variant Id") @RequestParam(name = "variantId", required = false) String variantId,
                                                                                  @Parameter(description = "Substations id") @RequestParam(name = "substationId", required = false) List<String> substationsIds) {
        return networkMapService.getVscConverterStations(networkUuid, variantId, substationsIds);
    }

    @GetMapping(value = "/networks/{networkUuid}/voltage-levels/{voltageLevelId}/configured-buses", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get buses description for a voltage level")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Buses description")})
    public @ResponseBody List<BusMapData> getVoltageLevelBuses(@Parameter(description = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                                               @Parameter(description = "Voltage level id") @PathVariable("voltageLevelId") String voltageLevelId,
                                                               @Parameter(description = "Variant Id") @RequestParam(name = "variantId", required = false) String variantId) {
        return networkMapService.getVoltageLevelBuses(networkUuid, voltageLevelId, variantId);
    }

    @GetMapping(value = "/networks/{networkUuid}/voltage-levels/{voltageLevelId}/busbar-sections", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get busbar sections description for a voltage level")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Busbar section description")})
    public @ResponseBody List<BusbarSectionMapData> getVoltageLevelBusBarSections(@Parameter(description = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                                                                  @Parameter(description = "Voltage level id") @PathVariable("voltageLevelId") String voltageLevelId,
                                                                                  @Parameter(description = "Variant Id") @RequestParam(name = "variantId", required = false) String variantId) {
        return networkMapService.getVoltageLevelBusbarSections(networkUuid, voltageLevelId, variantId);
    }

    @GetMapping(value = "/networks/{networkUuid}/voltage-levels/{voltageLevelId}/busbar-sections/ids", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get busbar sections ids for a voltage level")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Busbar section ids")})
    public @ResponseBody List<String> getVoltageLevelBusBarSectionsIds(@Parameter(description = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                                                                  @Parameter(description = "Voltage level id") @PathVariable("voltageLevelId") String voltageLevelId,
                                                                                  @Parameter(description = "Variant Id") @RequestParam(name = "variantId", required = false) String variantId) {
        return networkMapService.getVoltageLevelBusbarSectionsIds(networkUuid, voltageLevelId, variantId);
    }

    @GetMapping(value = "/networks/{networkUuid}/voltage-levels-equipments", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get voltage level and its equipments description")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Voltage level and its equipments description")})
    public @ResponseBody List<org.gridsuite.network.map.model.VoltageLevelsEquipmentsMapData> getVoltageLevelsAndEquipments(@Parameter(description = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                                                                                                            @Parameter(description = "Variant Id") @RequestParam(name = "variantId", required = false) String variantId,
                                                                                                                            @Parameter(description = "Substations id") @RequestParam(name = "substationId", required = false) List<String> substationsIds) {
        return networkMapService.getVoltageLevelsAndConnectable(networkUuid, variantId, substationsIds);
    }

    @GetMapping(value = "/networks/{networkUuid}/voltage-level-equipments/{voltageLevelId}", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get Voltage level equipements")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Voltage level equipements")})
    public @ResponseBody List<org.gridsuite.network.map.model.VoltageLevelConnectableMapData> getVoltageLevelEquipments(@Parameter(description = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                                                                                                        @Parameter(description = "Voltage level id") @PathVariable("voltageLevelId") String voltageLevelId,
                                                                                                                        @Parameter(description = "Variant Id") @RequestParam(name = "variantId", required = false) String variantId,
                                                                                                                        @Parameter(description = "Substations id") @RequestParam(name = "substationId", required = false) List<String> substationsIds) {
        return networkMapService.getVoltageLevelEquipements(networkUuid, voltageLevelId, variantId, substationsIds);
    }

    @GetMapping(value = "/networks/{networkUuid}/branch-or-3wt/{equipmentId}", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "From an equipment ID, get the associated line or 2WT or 3WT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Line or 2WT or 3WT description"),
            @ApiResponse(responseCode = "204", description = "No element found")
    })
    public @ResponseBody Object getBranchOrThreeWindingsTransformer(@Parameter(description = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                                                    @Parameter(description = "Equipment ID") @PathVariable("equipmentId") String equipmentId,
                                                                    @Parameter(description = "Variant ID") @RequestParam(name = "variantId", required = false) String variantId) {
        return networkMapService.getBranchOrThreeWindingsTransformer(networkUuid, variantId, equipmentId);
    }
}
