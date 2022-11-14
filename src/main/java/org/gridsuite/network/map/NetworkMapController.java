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
import org.gridsuite.network.map.model.AllMapData;
import org.gridsuite.network.map.model.BatteryMapData;
import org.gridsuite.network.map.model.BusMapData;
import org.gridsuite.network.map.model.BusbarSectionMapData;
import org.gridsuite.network.map.model.DanglingLineMapData;
import org.gridsuite.network.map.model.GeneratorMapData;
import org.gridsuite.network.map.model.HvdcLineMapData;
import org.gridsuite.network.map.model.LccConverterStationMapData;
import org.gridsuite.network.map.model.LineMapData;
import org.gridsuite.network.map.model.LoadMapData;
import org.gridsuite.network.map.model.ShuntCompensatorMapData;
import org.gridsuite.network.map.model.StaticVarCompensatorMapData;
import org.gridsuite.network.map.model.SubstationMapData;
import org.gridsuite.network.map.model.ThreeWindingsTransformerMapData;
import org.gridsuite.network.map.model.TwoWindingsTransformerMapData;
import org.gridsuite.network.map.model.VoltageLevelMapData;
import org.gridsuite.network.map.model.VscConverterStationMapData;
import org.gridsuite.network.map.model.MapEquipmentsData;
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

    @GetMapping(value = "/networks/{networkUuid}/substations", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get substations description")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Substations description")})
    public @ResponseBody List<SubstationMapData> getSubstations(@Parameter(description = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                                                @Parameter(description = "Variant Id") @RequestParam(name = "variantId", required = false) String variantId,
                                                                @Parameter(description = "Substations id") @RequestParam(name = "substationId", required = false) List<String> substationsIds) {
        return networkMapService.getSubstations(networkUuid, variantId, substationsIds);
    }

    @GetMapping(value = "/networks/{networkUuid}/substations/{substationId}", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get substation description")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Substation description")})
    public @ResponseBody SubstationMapData getSubstation(@Parameter(description = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                                         @Parameter(description = "Substation id") @PathVariable(name = "substationId") String substationId,
                                                                @Parameter(description = "Variant Id") @RequestParam(name = "variantId", required = false) String variantId) {
        return networkMapService.getSubstation(networkUuid, variantId, substationId);
    }

    @GetMapping(value = "/networks/{networkUuid}/lines", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get lines description")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Lines description")})
    public @ResponseBody List<LineMapData> getLines(@Parameter(description = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                                    @Parameter(description = "Variant Id") @RequestParam(name = "variantId", required = false) String variantId,
                                                    @Parameter(description = "Substations id") @RequestParam(name = "substationId", required = false) List<String> substationsIds) {
        return networkMapService.getLines(networkUuid, variantId, substationsIds);
    }

    @GetMapping(value = "/networks/{networkUuid}/lines/{lineId}", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get line description")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Line description")})
    public @ResponseBody LineMapData getLine(@Parameter(description = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                             @Parameter(description = "Line id") @PathVariable("lineId") String lineId,
                                             @Parameter(description = "Variant Id") @RequestParam(name = "variantId", required = false) String variantId) {
        return networkMapService.getLine(networkUuid, variantId, lineId);
    }

    @GetMapping(value = "/networks/{networkUuid}/generators", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get generators description")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Generators description")})
    public @ResponseBody List<GeneratorMapData> getGenerators(@Parameter(description = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                                              @Parameter(description = "Variant Id") @RequestParam(name = "variantId", required = false) String variantId,
                                                              @Parameter(description = "Substations id") @RequestParam(name = "substationId", required = false) List<String> substationsIds) {
        return networkMapService.getGenerators(networkUuid, variantId, substationsIds);
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

    @GetMapping(value = "/networks/{networkUuid}/hvdc-lines", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get hvdc lines description")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Hvdc lines description")})
    public @ResponseBody List<HvdcLineMapData> getHvdcLines(@Parameter(description = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                                            @Parameter(description = "Variant Id") @RequestParam(name = "variantId", required = false) String variantId,
                                                            @Parameter(description = "Substations id") @RequestParam(name = "substationId", required = false) List<String> substationsIds) {
        return networkMapService.getHvdcLines(networkUuid, variantId, substationsIds);
    }

    @GetMapping(value = "/networks/{networkUuid}/lcc-converter-stations", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get lcc converter stations description")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Lcc converter stations description")})
    public @ResponseBody List<LccConverterStationMapData> getLccConverterStations(@Parameter(description = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                                                                  @Parameter(description = "Variant Id") @RequestParam(name = "variantId", required = false) String variantId,
                                                                                  @Parameter(description = "Substations id") @RequestParam(name = "substationId", required = false) List<String> substationsIds) {
        return networkMapService.getLccConverterStations(networkUuid, variantId, substationsIds);
    }

    @GetMapping(value = "/networks/{networkUuid}/loads", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get loads description")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Loads description")})
    public @ResponseBody List<LoadMapData> getLoads(@Parameter(description = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                                    @Parameter(description = "Variant Id") @RequestParam(name = "variantId", required = false) String variantId,
                                                    @Parameter(description = "Substations id") @RequestParam(name = "substationId", required = false) List<String> substationsIds) {
        return networkMapService.getLoads(networkUuid, variantId, substationsIds);
    }

    @GetMapping(value = "/networks/{networkUuid}/loads/{loadId}", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get load description")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Load description")})
    public @ResponseBody LoadMapData getLoad(@Parameter(description = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                                    @Parameter(description = "Load id") @PathVariable("loadId") String loadId,
                                                    @Parameter(description = "Variant Id") @RequestParam(name = "variantId", required = false) String variantId) {
        return networkMapService.getLoad(networkUuid, variantId, loadId);
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

    @GetMapping(value = "/networks/{networkUuid}/voltage-levels", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get voltage levels description")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Voltage levels description")})
    public @ResponseBody List<VoltageLevelMapData> getVoltageLevels(@Parameter(description = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                                                    @Parameter(description = "Variant Id") @RequestParam(name = "variantId", required = false) String variantId,
                                                                    @Parameter(description = "Substations id") @RequestParam(name = "substationId", required = false) List<String> substationsIds) {
        return networkMapService.getVoltageLevels(networkUuid, variantId, substationsIds);
    }

    @GetMapping(value = "/networks/{networkUuid}/voltage-levels/{voltageLevelId}", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get voltage level description")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Voltage level description")})
    public @ResponseBody VoltageLevelMapData getVoltageLevel(@Parameter(description = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                                                     @Parameter(description = "Voltage level id") @PathVariable("voltageLevelId") String voltageLevelId,
                                                                     @Parameter(description = "Variant Id") @RequestParam(name = "variantId", required = false) String variantId) {
        return networkMapService.getVoltageLevel(networkUuid, variantId, voltageLevelId);
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

    @GetMapping(value = "/networks/{networkUuid}/voltage-levels-equipments", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get voltage level and its equipments description")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Voltage level and its equipments description")})
    public @ResponseBody List<org.gridsuite.network.map.model.VoltageLevelsEquipmentsMapData> getVoltageLevelsAndEquipments(@Parameter(description = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                                                                                                            @Parameter(description = "Variant Id") @RequestParam(name = "variantId", required = false) String variantId,
                                                                                                                            @Parameter(description = "Substations id") @RequestParam(name = "substationId", required = false) List<String> substationsIds) {
        return networkMapService.getVoltageLevelsAndConnectable(networkUuid, variantId, substationsIds);
    }

    @GetMapping(value = "/networks/{networkUuid}/map-equipments-data", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get lines description")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Lines description")})
    public @ResponseBody MapEquipmentsData getMapEquipmentsData(@Parameter(description = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                                                @Parameter(description = "Variant Id") @RequestParam(name = "variantId", required = false) String variantId,
                                                                @Parameter(description = "Substations id") @RequestParam(name = "substationId", required = false) List<String> substationsIds) {
        return networkMapService.getMapEquipmentsData(networkUuid, variantId, substationsIds);
    }
}
