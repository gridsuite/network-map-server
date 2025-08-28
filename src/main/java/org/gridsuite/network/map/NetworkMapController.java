/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map;

import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.ThreeSides;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.gridsuite.network.map.dto.*;
import org.gridsuite.network.map.dto.definition.hvdc.HvdcShuntCompensatorsInfos;
import org.gridsuite.network.map.services.NetworkMapService;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@RestController
@RequestMapping(value = "/" + NetworkMapApi.API_VERSION + "/")
@Tag(name = "Network map server")
@ComponentScan(basePackageClasses = NetworkMapService.class)
@AllArgsConstructor
public class NetworkMapController {
    private final NetworkMapService networkMapService;

    @PostMapping(value = "/networks/{networkUuid}/elements-ids", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get elements ids")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Equipments ids")})
    public List<String> getElementsIds(@Parameter(description = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                       @Parameter(description = "Variant Id") @RequestParam(name = "variantId", required = false) String variantId,
                                       @Parameter(description = "Nominal Voltages") @RequestParam(name = "nominalVoltages", required = false) List<Double> nominalVoltages,
                                       @Parameter(description = "Element type") @RequestParam(name = "elementType") ElementType elementType,
                                       @RequestBody(required = false) Optional<List<String>> substationsIds) {
        return networkMapService.getElementsIds(networkUuid, variantId, substationsIds.orElseGet(List::of), elementType, nominalVoltages);
    }

    @GetMapping(value = "/networks/{networkUuid}/all", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get all equipments descriptions")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "all equipments descriptions")})
    public AllElementsInfos getAll(@Parameter(description = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                   @Parameter(description = "Variant Id") @RequestParam(name = "variantId", required = false) String variantId,
                                   @Parameter(description = "Substations id") @RequestParam(name = "substationId", defaultValue = "") List<String> substationsIds) {
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
                                               @RequestBody(required = false) Optional<List<String>> substationsIds) {
        return networkMapService.getElementsInfos(networkUuid, variantId, substationsIds.orElseGet(List::of), elementType, infoTypeParameters, nominalVoltages);
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

    @GetMapping(value = "/networks/{networkUuid}/voltage-levels/{voltageLevelId}/buses-or-busbar-sections", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get buses or busbar sections description for a voltage level")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Buses or Busbar section description")})
    public List<ElementInfos> getVoltageLevelBusesOrBusBarSections(@Parameter(description = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                                            @Parameter(description = "Voltage level id") @PathVariable("voltageLevelId") String voltageLevelId,
                                                            @Parameter(description = "Variant Id") @RequestParam(name = "variantId", required = false) String variantId) {
        return networkMapService.getVoltageLevelBusesOrBusbarSections(networkUuid, voltageLevelId, variantId);
    }

    @GetMapping(value = "/networks/{networkUuid}/voltage-levels/{voltageLevelId}/switches", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get switches description for a voltage level")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Switches description")})
    public List<SwitchInfos> getVoltageLevelSwitches(@Parameter(description = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                                     @Parameter(description = "Voltage level id") @PathVariable("voltageLevelId") String voltageLevelId,
                                                     @Parameter(description = "Variant Id") @RequestParam(name = "variantId", required = false) String variantId) {
        return networkMapService.getVoltageLevelSwitches(networkUuid, voltageLevelId, variantId);
    }

    @GetMapping(value = "/networks/{networkUuid}/voltage-levels/{voltageLevelId}/substation-id", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get substation ID for a voltage level")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "substation ID for a voltage level")})
    public String getVoltageLevelSubstationId(@Parameter(description = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                                            @Parameter(description = "Voltage level id") @PathVariable("voltageLevelId") String voltageLevelId,
                                                            @Parameter(description = "Variant Id") @RequestParam(name = "variantId", required = false) String variantId) {
        return networkMapService.getVoltageLevelSubstationID(networkUuid, voltageLevelId, variantId);
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
                                                               @Parameter(description = "Variant Id") @RequestParam(name = "variantId", required = false) String variantId) {
        return networkMapService.getVoltageLevelEquipments(networkUuid, voltageLevelId, variantId);
    }

    @GetMapping(value = "/networks/{networkUuid}/branch-or-3wt/{equipmentId}/voltage-level-id", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get the voltage level id for a branch or a 3wt side")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved branch or 3WT side voltage level id"),
        @ApiResponse(responseCode = "204", description = "No voltage level id found")
    })
    public String getBranchOr3WTVoltageLevelId(
            @Parameter(description = "Network UUID")
            @PathVariable("networkUuid") UUID networkUuid,
            @Parameter(description = "Equipment ID")
            @PathVariable("equipmentId") String equipmentId,
            @Parameter(description = "Variant ID") @RequestParam(name = "variantId", required = false) String variantId,
            @Parameter(description = "Side") @RequestParam(name = "side") ThreeSides side) {
        return networkMapService.getBranchOr3WTVoltageLevelId(networkUuid, variantId, equipmentId, side);
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
    public List<Double> getNominalVoltages(@Parameter(description = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                           @Parameter(description = "Variant ID") @RequestParam(name = "variantId", required = false) String variantId) {
        return networkMapService.getNominalVoltages(networkUuid, variantId).stream().sorted(Comparator.reverseOrder()).toList();
    }
}
