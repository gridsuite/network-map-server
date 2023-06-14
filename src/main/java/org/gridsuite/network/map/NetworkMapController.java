/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.gridsuite.network.map.dto.AllElementsInfos;
import org.gridsuite.network.map.dto.ElementInfos;
import org.gridsuite.network.map.dto.View;
import org.gridsuite.network.map.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.converter.json.MappingJacksonValue;
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

    @GetMapping(value = "/networks/{networkUuid}/elements-ids", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get elements ids")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Equipments ids")})
    public @ResponseBody List<String> getElementsIds(@Parameter(description = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                                     @Parameter(description = "Variant Id") @RequestParam(name = "variantId", required = false) String variantId,
                                                     @Parameter(description = "Substations ids") @RequestParam(name = "substationsIds", required = false) List<String> substationsIds,
                                                     @Parameter(description = "Element type") @RequestParam(name = "elementType") ElementType elementType) {
        return networkMapService.getElementsIds(networkUuid, variantId, substationsIds, elementType);
    }

    @GetMapping(value = "/networks/{networkUuid}/all", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get all equipments descriptions")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "all equipments descriptions")})
    public @ResponseBody AllElementsInfos getAll(@Parameter(description = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                                 @Parameter(description = "Variant Id") @RequestParam(name = "variantId", required = false) String variantId,
                                                 @Parameter(description = "Substations id") @RequestParam(name = "substationId", required = false) List<String> substationsIds) {
        return networkMapService.getAllElementsInfos(networkUuid, variantId, substationsIds);
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
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Element description")})
    public @ResponseBody MappingJacksonValue getElementInfos(@Parameter(description = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                                      @Parameter(description = "Element id") @PathVariable("elementId") String elementId,
                                                      @Parameter(description = "Variant Id") @RequestParam(name = "variantId", required = false) String variantId,
                                                      @Parameter(description = "Element type") @RequestParam(name = "elementType") ElementType elementType,
                                                      @Parameter(description = "Info type") @RequestParam(name = "infoType") ElementInfos.InfoType infoType) {
        MappingJacksonValue elementInfos = new MappingJacksonValue(networkMapService.getElementInfos(networkUuid, variantId, elementType, infoType, elementId));
        elementInfos.setSerializationView(View.infoTypeToView(infoType));
        return elementInfos;
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
    public @ResponseBody List<ElementInfos> getVoltageLevelBusBarSections(@Parameter(description = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
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
    public @ResponseBody List<VoltageLevelsEquipmentsMapData> getVoltageLevelsAndEquipments(@Parameter(description = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                                                                            @Parameter(description = "Variant Id") @RequestParam(name = "variantId", required = false) String variantId,
                                                                                            @Parameter(description = "Substations id") @RequestParam(name = "substationId", required = false) List<String> substationsIds) {
        return networkMapService.getVoltageLevelsAndConnectable(networkUuid, variantId, substationsIds);
    }

    @GetMapping(value = "/networks/{networkUuid}/voltage-level-equipments/{voltageLevelId}", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get Voltage level equipements")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Voltage level equipements")})
    public @ResponseBody List<VoltageLevelConnectableMapData> getVoltageLevelEquipments(@Parameter(description = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
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
