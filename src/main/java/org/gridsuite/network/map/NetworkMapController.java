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
import org.gridsuite.network.map.model.VscConverterStationMapData;
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

    @GetMapping(value = "/substations/{networkUuid}", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get substations description")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Substations description")})
    public @ResponseBody List<SubstationMapData> getSubstations(@Parameter(description = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                                                @Parameter(description = "Substations id") @RequestParam(name = "substationId", required = false) List<String> substationsIds) {
        return networkMapService.getSubstations(networkUuid, substationsIds);
    }

    @GetMapping(value = "/lines/{networkUuid}", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get lines description")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Lines description")})
    public @ResponseBody List<LineMapData> getLines(@Parameter(description = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                                    @Parameter(description = "Substations id") @RequestParam(name = "substationId", required = false) List<String> substationsIds) {
        return networkMapService.getLines(networkUuid, substationsIds);
    }

    @GetMapping(value = "/generators/{networkUuid}", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get generators description")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Generators description")})
    public @ResponseBody List<GeneratorMapData> getGenerators(@Parameter(description = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                                              @Parameter(description = "Substations id") @RequestParam(name = "substationId", required = false) List<String> substationsIds) {
        return networkMapService.getGenerators(networkUuid, substationsIds);
    }

    @GetMapping(value = "/2-windings-transformers/{networkUuid}", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get 2 windings transformers description")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "2 windings transformers description")})
    public @ResponseBody List<TwoWindingsTransformerMapData> getTwoWindingsTransformers(@Parameter(description = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                                                                        @Parameter(description = "Substations id") @RequestParam(name = "substationId", required = false) List<String> substationsIds) {
        return networkMapService.getTwoWindingsTransformers(networkUuid, substationsIds);
    }

    @GetMapping(value = "/3-windings-transformers/{networkUuid}", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get 3 windings transformers description")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "3 windings transformers description")})
    public @ResponseBody List<ThreeWindingsTransformerMapData> getThreeWindingsTransformers(@Parameter(description = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                                                                            @Parameter(description = "Substations id") @RequestParam(name = "substationId", required = false) List<String> substationsIds) {
        return networkMapService.getThreeWindingsTransformers(networkUuid, substationsIds);
    }

    @GetMapping(value = "/all/{networkUuid}", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get all equipments descriptions")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "all equipments descriptions")})
    public @ResponseBody AllMapData getAll(@Parameter(description = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                                 @Parameter(description = "Substations id") @RequestParam(name = "substationId", required = false) List<String> substationsIds) {
        return networkMapService.getAll(networkUuid, substationsIds);
    }

    @GetMapping(value = "/batteries/{networkUuid}", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get batteries description")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Batteries description")})
    public @ResponseBody List<BatteryMapData> getBatteries(@Parameter(description = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                                                           @Parameter(description = "Substations id") @RequestParam(name = "substationId", required = false) List<String> substationsIds) {
        return networkMapService.getBatteries(networkUuid, substationsIds);
    }

    @GetMapping(value = "/dangling-lines/{networkUuid}", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get dangling lines description")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Dangling lines description")})
    public @ResponseBody List<DanglingLineMapData> getDanglingLines(@Parameter(description = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                                                @Parameter(description = "Substations id") @RequestParam(name = "substationId", required = false) List<String> substationsIds) {
        return networkMapService.getDanglingLines(networkUuid, substationsIds);
    }

    @GetMapping(value = "/hvdc-lines/{networkUuid}", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get hvdc lines description")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Hvdc lines description")})
    public @ResponseBody List<HvdcLineMapData> getHvdcLines(@Parameter(description = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                                                @Parameter(description = "Substations id") @RequestParam(name = "substationId", required = false) List<String> substationsIds) {
        return networkMapService.getHvdcLines(networkUuid, substationsIds);
    }

    @GetMapping(value = "/lcc-converter-stations/{networkUuid}", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get lcc converter stations description")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Lcc converter stations description")})
    public @ResponseBody List<LccConverterStationMapData> getLccConverterStations(@Parameter(description = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                                                       @Parameter(description = "Substations id") @RequestParam(name = "substationId", required = false) List<String> substationsIds) {
        return networkMapService.getLccConverterStations(networkUuid, substationsIds);
    }

    @GetMapping(value = "/loads/{networkUuid}", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get loads description")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Loads description")})
    public @ResponseBody List<LoadMapData> getLoads(@Parameter(description = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                                    @Parameter(description = "Substations id") @RequestParam(name = "substationId", required = false) List<String> substationsIds) {
        return networkMapService.getLoads(networkUuid, substationsIds);
    }

    @GetMapping(value = "/shunt-compensators/{networkUuid}", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get shunt compensators description")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Shunt compensators description")})
    public @ResponseBody List<ShuntCompensatorMapData> getShuntCompensators(@Parameter(description = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                                                 @Parameter(description = "Substations id") @RequestParam(name = "substationId", required = false) List<String> substationsIds) {
        return networkMapService.getShuntCompensators(networkUuid, substationsIds);
    }

    @GetMapping(value = "/static-var-compensators/{networkUuid}", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get static var compensators description")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Static var compensators description")})
    public @ResponseBody List<StaticVarCompensatorMapData> getStaticVarCompensators(@Parameter(description = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                                                                @Parameter(description = "Substations id") @RequestParam(name = "substationId", required = false) List<String> substationsIds) {
        return networkMapService.getStaticVarCompensators(networkUuid, substationsIds);
    }

    @GetMapping(value = "/vsc-converter-stations/{networkUuid}", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get vsc converter stations description")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Vsc converter stations description")})
    public @ResponseBody List<VscConverterStationMapData> getVscConverterStations(@Parameter(description = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                                                                  @Parameter(description = "Substations id") @RequestParam(name = "substationId", required = false) List<String> substationsIds) {
        return networkMapService.getVscConverterStations(networkUuid, substationsIds);
    }
}
