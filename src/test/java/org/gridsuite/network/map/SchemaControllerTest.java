package org.gridsuite.network.map;

import org.assertj.core.api.WithAssertions;
import org.gridsuite.network.map.dto.ElementInfos.InfoType;
import org.gridsuite.network.map.dto.ElementType;
import org.gridsuite.network.map.services.SchemaService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.log;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SchemaController.class)
class SchemaControllerTest implements WithAssertions {
    @Autowired
    private MockMvc mockMvc;

    @SpyBean //need to init it for the controller
    private SchemaService schemaService;

    final ClassLoader cl = this.getClass().getClassLoader();

    private static Stream<Arguments> schemaRequestValues() {
        final Map<ElementType, Map<InfoType, String>> cases = new EnumMap<>(ElementType.class);
        for (ElementType elementType : ElementType.values()) {
            final Map<InfoType, String> subCases = new EnumMap<>(InfoType.class);
            cases.put(elementType, subCases);
            for (InfoType infoType : InfoType.values()) {
                subCases.put(infoType, null);
            }
        }
        final String dtoPath = "schemas/org/gridsuite/network/map/dto/definition/";
        cases.get(ElementType.BATTERY).put(InfoType.TAB, dtoPath + "battery/BatteryTabInfos-schema.json");
        cases.get(ElementType.BRANCH).put(InfoType.TAB, dtoPath + "branch/BranchTabInfos-schema.json");
        cases.get(ElementType.BUS).put(InfoType.TAB, dtoPath + "bus/BusTabInfos-schema.json");
        cases.get(ElementType.BUSBAR_SECTION).put(InfoType.TAB, dtoPath + "busbarsection/BusBarSectionTabInfos-schema.json");
        cases.get(ElementType.DANGLING_LINE).put(InfoType.TAB, dtoPath + "danglingline/DanglingLineTabInfos-schema.json");
        cases.get(ElementType.GENERATOR).put(InfoType.TAB, dtoPath + "generator/GeneratorTabInfos-schema.json");
        final String hvdcTabInfosSchema = dtoPath + "hvdc/HvdcTabInfos-schema.json";
        cases.get(ElementType.HVDC_LINE).put(InfoType.TAB, hvdcTabInfosSchema);
        cases.get(ElementType.HVDC_LINE_LCC).put(InfoType.TAB, hvdcTabInfosSchema);
        cases.get(ElementType.HVDC_LINE_VSC).put(InfoType.TAB, hvdcTabInfosSchema);
        cases.get(ElementType.LCC_CONVERTER_STATION).put(InfoType.TAB, dtoPath + "lccconverterstation/LccConverterStationTabInfos-schema.json");
        cases.get(ElementType.LINE).put(InfoType.TAB, dtoPath + "branch/line/LineTabInfos-schema.json");
        cases.get(ElementType.LOAD).put(InfoType.TAB, dtoPath + "load/LoadTabInfos-schema.json");
        cases.get(ElementType.SHUNT_COMPENSATOR).put(InfoType.TAB, dtoPath + "shuntcompensator/ShuntCompensatorTabInfos-schema.json");
        cases.get(ElementType.STATIC_VAR_COMPENSATOR).put(InfoType.TAB, dtoPath + "staticvarcompensator/StaticVarCompensatorTabInfos-schema.json");
        cases.get(ElementType.SUBSTATION).put(InfoType.TAB, dtoPath + "substation/SubstationTabInfos-schema.json");
        cases.get(ElementType.THREE_WINDINGS_TRANSFORMER).put(InfoType.TAB, dtoPath + "threewindingstransformer/ThreeWindingsTransformerTabInfos-schema.json");
        cases.get(ElementType.TIE_LINE).put(InfoType.TAB, dtoPath + "tieline/TieLineTabInfos-schema.json");
        cases.get(ElementType.TWO_WINDINGS_TRANSFORMER).put(InfoType.TAB, dtoPath + "branch/twowindingstransformer/TwoWindingsTransformerTabInfos-schema.json");
        cases.get(ElementType.VOLTAGE_LEVEL).put(InfoType.TAB, dtoPath + "voltagelevel/VoltageLevelTabInfos-schema.json");
        cases.get(ElementType.VSC_CONVERTER_STATION).put(InfoType.TAB, dtoPath + "vscconverterstation/VscConverterStationTabInfos-schema.json");
        return cases.entrySet().stream().flatMap(e1 -> e1.getValue().entrySet().stream().map(e2 -> Arguments.of(e1.getKey(), e2.getKey(), e2.getValue())));
    }

    @ParameterizedTest(name = "{0} (view {1})")
    @MethodSource("schemaRequestValues")
    void schemaRequest(@NonNull final ElementType eType, @NonNull final InfoType iType, @Nullable final String resultPath) throws Exception {
        ResultActions result = this.mockMvc.perform(get("/v1/schemas/{eType}/{iType}", eType, iType)).andDo(log());
        if (resultPath != null) {
            try (final InputStream json = cl.getResourceAsStream(resultPath)) {
                result.andExpectAll(
                    status().isOk(),
                    content().contentType(SchemaController.APPLICATION_JSON_SCHEMA_VALUE),
                    content().json(new String(json.readAllBytes(), StandardCharsets.UTF_8), true)
                );
            }
        } else {
            result.andExpectAll(status().isNotImplemented());
        }
    }
}
