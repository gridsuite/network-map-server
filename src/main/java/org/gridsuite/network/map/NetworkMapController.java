/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map;

import com.powsybl.iidm.network.Country;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.gridsuite.network.map.dto.*;
import org.gridsuite.network.map.dto.definition.hvdc.HvdcShuntCompensatorsInfos;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.*;

import java.util.*;

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

    private final NetworkMapService networkMapService;

    public NetworkMapController(NetworkMapService networkMapService) {
        this.networkMapService = networkMapService;
    }

    @PostMapping(value = "/networks/{networkUuid}/elements-ids", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get elements ids")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Equipments ids")})
    public List<String> getElementsIds(@Parameter(description = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                       @Parameter(description = "Variant Id") @RequestParam(name = "variantId", required = false) String variantId,
                                       @Parameter(description = "Nominal Voltages") @RequestParam(name = "nominalVoltages", required = false) List<Double> nominalVoltages,
                                       @Parameter(description = "Element type") @RequestParam(name = "elementType") ElementType elementType,
                                       @RequestBody(required = false) List<String> substationsIds
                                       ) {
        return networkMapService.getElementsIds(networkUuid, variantId, substationsIds, elementType, nominalVoltages);
    }

    @GetMapping(value = "/networks/{networkUuid}/all", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get all equipments descriptions")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "all equipments descriptions")})
    public AllElementsInfos getAll(@Parameter(description = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                   @Parameter(description = "Variant Id") @RequestParam(name = "variantId", required = false) String variantId,
                                   @Parameter(description = "Substations id") @RequestParam(name = "substationId", required = false) List<String> substationsIds) {
        return networkMapService.getAllElementsInfos(networkUuid, variantId, substationsIds);
    }

    @PostMapping(value = "/networks/{networkUuid}/elements", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get network elements")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Elements description")})
    public List<ElementInfos> getElementsInfos(@Parameter(description = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                               @Parameter(description = "Variant Id") @RequestParam(name = "variantId", required = false) String variantId,
                                               @Parameter(description = "Nominal Voltages") @RequestParam(name = "nominalVoltages", required = false) List<Double> nominalVoltages,
                                               @Parameter(description = "Element type") @RequestParam(name = "elementType") ElementType elementType,
                                               @Parameter(description = "Info type parameters") InfoTypeParameters infoTypeParameters,
                                               @RequestBody(required = false) List<String> substationsIds) {
        return networkMapService.getElementsInfos(networkUuid, variantId, substationsIds, elementType, infoTypeParameters, nominalVoltages);
    }

    @GetMapping(value = "/networks/{networkUuid}/elements/{elementId}", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get network element infos")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Element description")})
    public ElementInfos getElementInfos(@Parameter(description = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                        @Parameter(description = "Element id") @PathVariable("elementId") String elementId,
                                        @Parameter(description = "Variant Id") @RequestParam(name = "variantId", required = false) String variantId,
                                        @Parameter(description = "Element type") @RequestParam(name = "elementType") ElementType elementType,
                                        @Parameter(description = "Info type parameters") InfoTypeParameters infoTypeParameters) {
        return networkMapService.getElementInfos(networkUuid, variantId, elementType, infoTypeParameters, elementId);
    }

    @GetMapping(value = "/networks/{networkUuid}/voltage-levels/{voltageLevelId}/configured-buses", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get buses description for a voltage level")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Buses description")})
    public List<ElementInfos> getVoltageLevelBuses(@Parameter(description = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                                   @Parameter(description = "Voltage level id") @PathVariable("voltageLevelId") String voltageLevelId,
                                                   @Parameter(description = "Variant Id") @RequestParam(name = "variantId", required = false) String variantId) {
        return networkMapService.getVoltageLevelBuses(networkUuid, voltageLevelId, variantId);
    }

    @GetMapping(value = "/networks/{networkUuid}/voltage-levels/{voltageLevelId}/busbar-sections", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get busbar sections description for a voltage level")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Busbar section description")})
    public List<ElementInfos> getVoltageLevelBusBarSections(@Parameter(description = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                                            @Parameter(description = "Voltage level id") @PathVariable("voltageLevelId") String voltageLevelId,
                                                            @Parameter(description = "Variant Id") @RequestParam(name = "variantId", required = false) String variantId) {
        return networkMapService.getVoltageLevelBusbarSections(networkUuid, voltageLevelId, variantId);
    }

    @GetMapping(value = "/networks/{networkUuid}/voltage-levels/{voltageLevelId}/busbar-sections/ids", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get busbar sections ids for a voltage level")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Busbar section ids")})
    public List<String> getVoltageLevelBusBarSectionsIds(@Parameter(description = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                                         @Parameter(description = "Voltage level id") @PathVariable("voltageLevelId") String voltageLevelId,
                                                         @Parameter(description = "Variant Id") @RequestParam(name = "variantId", required = false) String variantId) {
        return networkMapService.getVoltageLevelBusbarSectionsIds(networkUuid, voltageLevelId, variantId);
    }

    @GetMapping(value = "/networks/{networkUuid}/voltage-levels/{voltageLevelId}/equipments", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get Voltage level equipments")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Voltage level equipments")})
    public List<ElementInfos> getVoltageLevelEquipments(@Parameter(description = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                                               @Parameter(description = "Voltage level id") @PathVariable("voltageLevelId") String voltageLevelId,
                                                               @Parameter(description = "Variant Id") @RequestParam(name = "variantId", required = false) String variantId,
                                                               @Parameter(description = "Substations id") @RequestParam(name = "substationId", required = false) List<String> substationsIds) {
        return networkMapService.getVoltageLevelEquipments(networkUuid, voltageLevelId, variantId, substationsIds);
    }

    @GetMapping(value = "/networks/{networkUuid}/branch-or-3wt/{equipmentId}", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "From an equipment ID, get the associated line or 2WT or 3WT")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Line or 2WT or 3WT description"),
        @ApiResponse(responseCode = "204", description = "No element found")
    })
    public Object getBranchOrThreeWindingsTransformer(@Parameter(description = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                                      @Parameter(description = "Equipment ID") @PathVariable("equipmentId") String equipmentId,
                                                      @Parameter(description = "Variant ID") @RequestParam(name = "variantId", required = false) String variantId) {
        return networkMapService.getBranchOrThreeWindingsTransformer(networkUuid, variantId, equipmentId);
    }

    @GetMapping(value = "/networks/{networkUuid}/hvdc-lines/{hvdcId}/shunt-compensators", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "For a given HVDC line, get its related shunt compensators in case of LCC converter station")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Hvdc line type and its shunt compensators on each side"),
        @ApiResponse(responseCode = "204", description = "No element found")
    })
    public HvdcShuntCompensatorsInfos getHvdcLineShuntCompensators(@Parameter(description = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                                                   @Parameter(description = "Hcdc Line ID") @PathVariable("hvdcId") String hvdcId,
                                                                   @Parameter(description = "Variant ID") @RequestParam(name = "variantId", required = false) String variantId) {
        return networkMapService.getHvdcLineShuntCompensators(networkUuid, variantId, hvdcId);
    }

    @GetMapping(value = "/networks/{networkUuid}/countries")
    @Operation(summary = "Get the list of countries present in the network")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Countries are found the in substations of the network")
    })
    public List<Country> getCountries(@Parameter(description = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                      @Parameter(description = "Variant ID") @RequestParam(name = "variantId", required = false) String variantId) {
        return networkMapService.getCountries(networkUuid, variantId).stream().sorted(Comparator.comparing(Country::getName)).toList();
    }

    @GetMapping(value = "/networks/{networkUuid}/nominal-voltages")
    @Operation(summary = "Get the list of nominal voltages present in the network")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Nominal voltages are found the in voltage levels of the network")
    })
    public @ResponseBody List<Double> getNominalVoltages(@Parameter(description = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                                   @Parameter(description = "Variant ID") @RequestParam(name = "variantId", required = false) String variantId) {
        return networkMapService.getNominalVoltages(networkUuid, variantId).stream().sorted().toList();
    }
}
