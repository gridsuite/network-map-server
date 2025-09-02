/* Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map;

import org.gridsuite.network.map.dto.ElementType;
import org.gridsuite.network.map.dto.InfoTypeParameters;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.*;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests to assure the {@link NetworkMapController} handle empty {@link List} and {@code null} values the same in endpoints parameters.
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@WebMvcTest(controllers = NetworkMapController.class)
class ListHandlingControllerTest {
    private static final UUID NETWORK_ID = UUID.randomUUID();

    @Autowired
    private MockMvc mvc;

    @MockBean
    private NetworkMapService networkMapService;

    @BeforeEach
    void setUp() {
        Mockito.verifyNoInteractions(networkMapService);
    }

    @AfterEach
    void setDown() {
        Mockito.verifyNoMoreInteractions(networkMapService);
    }

    @AfterAll
    static void tearDown() {
        Mockito.validateMockitoUsage();
    }

    private static Stream<Optional<String>> testParameterList() {
        return Stream.of(Optional.empty(), Optional.of(""));
    }

    private MockHttpServletRequestBuilder requestWithOptionalSubstationId(final MockHttpServletRequestBuilder requestBuilder, final Optional<String> parameter) {
        return parameter.map(s -> requestBuilder.queryParam("substationId", s)).orElse(requestBuilder);
    }

    /** Case of {@code substationId} for {@link NetworkMapController#getVoltageLevelEquipments(UUID, String, String, List)} */
    @ParameterizedTest
    @MethodSource("testParameterList")
    void getVoltageLevelEquipmentsSubstationsIds(final Optional<String> parameter) throws Exception {
        mvc.perform(requestWithOptionalSubstationId(get("/v1/networks/{networkUuid}/voltage-levels/{voltageLevelId}/equipments", NETWORK_ID, "testId"), parameter))
            .andExpect(status().is2xxSuccessful());
        verify(networkMapService).getVoltageLevelEquipments(eq(NETWORK_ID), eq("testId"), isNull());
    }

    /** Case of {@code substationId} for {@link NetworkMapController#getAll(UUID, String, List)} */
    @ParameterizedTest
    @MethodSource("testParameterList")
    void getAllSubstationsIds(final Optional<String> parameter) throws Exception {
        mvc.perform(requestWithOptionalSubstationId(get("/v1/networks/{networkUuid}/all", NETWORK_ID), parameter))
            .andExpect(status().is2xxSuccessful());
        verify(networkMapService).getAllElementsInfos(eq(NETWORK_ID), isNull(), eq(Collections.emptyList()), eq(new InfoTypeParameters(null, Map.of())));
    }

    /** Case of {@code substationsIds} for {@link NetworkMapController#getElementsInfos(UUID, String, List, ElementType, InfoTypeParameters, Optional)} */
    @ParameterizedTest
    @MethodSource("testParameterList")
    void getElementsInfosSubstationsIds(final Optional<String> parameter) throws Exception {
        mvc.perform(requestWithOptionalSubstationId(post("/v1/networks/{networkUuid}/elements", NETWORK_ID)
                .queryParam("elementType", ElementType.LINE.toString()), parameter))
            .andExpect(status().is2xxSuccessful());
        verify(networkMapService).getElementsInfos(eq(NETWORK_ID), isNull(), eq(Collections.emptyList()), same(ElementType.LINE), eq(new InfoTypeParameters(null, Map.of())), isNull());
    }

    /** Case of {@code substationsIds} for {@link NetworkMapController#getElementsIds(UUID, String, List, ElementType, Optional)} */
    @ParameterizedTest
    @MethodSource("testParameterList")
    void getElementsIdsSubstationsIds(final Optional<String> parameter) throws Exception {
        mvc.perform(requestWithOptionalSubstationId(post("/v1/networks/{networkUuid}/elements-ids", NETWORK_ID)
                .queryParam("elementType", ElementType.LINE.toString()), parameter))
            .andExpect(status().is2xxSuccessful());
        verify(networkMapService).getElementsIds(eq(NETWORK_ID), isNull(), eq(Collections.emptyList()), same(ElementType.LINE), isNull());
    }
}
