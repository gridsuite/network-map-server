/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import org.apache.commons.lang3.tuple.Pair;
import org.gridsuite.network.map.dto.ElementInfos.InfoType;
import org.gridsuite.network.map.dto.ElementType;
import org.gridsuite.network.map.services.SchemaService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.lang.NonNull;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.log;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SchemaController.class)
@Import(SchemaService.class)
class SchemaControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoSpyBean
    private SchemaService schemaService;

    private static Stream<Arguments> schemaRequestValues() {
        final List<Pair<ElementType, InfoType>> cases = new ArrayList<>();
        for (ElementType elementType : ElementType.values()) {
            for (InfoType infoType : InfoType.values()) {
                cases.add(Pair.of(elementType, infoType));
            }
        }
        return cases.stream().map(e1 -> Arguments.of(e1.getKey(), e1.getValue()));
    }

    @ParameterizedTest(name = "{0} (view {1})")
    @MethodSource("schemaRequestValues")
    void schemaRequest(@NonNull final ElementType eType, @NonNull final InfoType iType) throws Exception {
        ResultActions result = this.mockMvc.perform(get("/v1/schemas/{eType}/{iType}", eType, iType)).andDo(log());
        if (iType.equals(InfoType.TAB)) {
            JsonSchema schema = schemaService.getSchema(eType, iType);
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            String json = ow.writeValueAsString(schema);
            result.andExpectAll(
                    status().isOk(),
                    content().contentType(SchemaController.APPLICATION_JSON_SCHEMA_VALUE),
                    content().json(json)
            );
        } else {
            result.andExpect(status().isNotImplemented());
        }
    }
}
