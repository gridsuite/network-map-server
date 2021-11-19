/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map;

import com.google.common.io.ByteStreams;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Battery;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.LccConverterStation;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.PhaseTapChanger;
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.VariantManagerConstants;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.VscConverterStation;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.network.store.client.NetworkStoreService;
import com.powsybl.network.store.client.PreloadingStrategy;
import com.powsybl.network.store.iidm.impl.NetworkFactoryImpl;
import com.powsybl.sld.iidm.extensions.BranchStatus;
import com.powsybl.sld.iidm.extensions.BranchStatusAdder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@RunWith(SpringRunner.class)
@WebMvcTest(NetworkMapController.class)
@ContextConfiguration(classes = {NetworkMapApplication.class})
public class NetworkMapControllerTest {

    private static final UUID NETWORK_UUID = UUID.fromString("7928181c-7977-4592-ba19-88027e4254e4");

    private static final UUID NOT_FOUND_NETWORK_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

    private static final String VARIANT_ID = "variant_1";
    private static final String VARIANT_ID_NOT_FOUND = "variant_notFound";

    @Autowired
    private MockMvc mvc;

    @MockBean
    private  NetworkStoreService networkStoreService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        Network network = EurostagTutorialExample1Factory.create(new NetworkFactoryImpl());
        Line l1 = network.getLine("NHV1_NHV2_1");
        l1.getTerminal1().setP(1.1)
                .setQ(2.2);
        l1.getTerminal2().setP(3.33)
                .setQ(4.44);
        l1.newCurrentLimits1().setPermanentLimit(700.4).add();
        l1.newCurrentLimits2().setPermanentLimit(800.8).add();
        network.getSubstation("P2").setCountry(null);

        TwoWindingsTransformer t1 = network.getTwoWindingsTransformer("NHV2_NLOAD");
        t1.getTerminal1().setP(5.5)
                .setQ(6.6);
        t1.getTerminal2().setP(7.77)
                .setQ(8.88);
        t1.newCurrentLimits1().setPermanentLimit(900.5).add();
        t1.newCurrentLimits2().setPermanentLimit(950.5).add();
        t1.getRatioTapChanger().setTapPosition(2);

        TwoWindingsTransformer t2 = network.getTwoWindingsTransformer("NGEN_NHV1");
        t2.getTerminal1().setP(11.1)
                .setQ(12.2);
        t2.getTerminal2().setP(13.33)
                .setQ(14.44);
        t2.newCurrentLimits1().setPermanentLimit(750.4).add();
        t2.newCurrentLimits2().setPermanentLimit(780.6).add();
        t2.newPhaseTapChanger()
                .beginStep()
                .setAlpha(1)
                .setRho(0.85f)
                .setR(0.0)
                .setX(0.0)
                .setG(0.0)
                .setB(0.0)
                .endStep()
                .beginStep()
                .setAlpha(1)
                .setRho(0.90f)
                .setR(0.0)
                .setX(0.0)
                .setG(0.0)
                .setB(0.0)
                .endStep()
                .setTapPosition(1)
                .setRegulating(true)
                .setRegulationMode(PhaseTapChanger.RegulationMode.CURRENT_LIMITER)
                .setRegulationValue(10)
                .setRegulationTerminal(t2.getTerminal1())
                .setTargetDeadband(0)
                .add();

        Generator gen = network.getGenerator("GEN");
        gen.getTerminal().setP(25);
        gen.getTerminal().setQ(32);
        gen.setTargetP(28);

        Substation p1 = network.getSubstation("P1");
        VoltageLevel vlnew2 = p1.newVoltageLevel()
                .setId("VLNEW2")
                .setNominalV(225.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vlnew2.getBusBreakerView().newBus()
                .setId("NNEW2")
            .add();

        make3WindingsTransformer(p1, "TWT", ThreeWindingsTransformer::getLeg1, ThreeWindingsTransformer::getLeg3);
        make3WindingsTransformer(p1, "TWT21", ThreeWindingsTransformer::getLeg2, ThreeWindingsTransformer::getLeg1);
        make3WindingsTransformer(p1, "TWT32", ThreeWindingsTransformer::getLeg3, ThreeWindingsTransformer::getLeg2);

        Substation p3 = network.newSubstation()
                .setId("P3")
                .setCountry(Country.FR)
                .setTso("RTE")
                .setGeographicalTags("A")
                .add();
        VoltageLevel vlgen3 = p3.newVoltageLevel()
                .setId("VLGEN3")
                .setNominalV(24.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
            .add();
        vlgen3.getBusBreakerView().newBus()
                .setId("NGEN3")
                .add();
        Line line3 = network.newLine()
                .setId("LINE3")
                .setVoltageLevel1("VLGEN")
                .setBus1("NGEN")
                .setConnectableBus1("NGEN")
                .setVoltageLevel2("VLGEN3")
                .setBus2("NGEN3")
                .setConnectableBus2("NGEN3")
                .setR(3.0)
                .setX(33.0)
                .setG1(0.0)
                .setB1(386E-6 / 2)
                .setG2(0.0)
                .setB2(386E-6 / 2)
            .add();
        line3.newExtension(BranchStatusAdder.class).withStatus(BranchStatus.Status.PLANNED_OUTAGE).add();

        Battery b1 = vlnew2.newBattery()
                .setId("BATTERY1")
                .setName("BATTERY1")
                .setMinP(0)
                .setMaxP(10)
                .setP0(1)
                .setQ0(1)
                .setConnectableBus("NNEW2")
                .add();
        b1.getTerminal().setP(50);
        b1.getTerminal().setQ(70);

        vlgen3.newBattery()
                .setId("BATTERY2")
                .setName("BATTERY2")
                .setMinP(0)
                .setMaxP(10)
                .setP0(1)
                .setQ0(1)
                .setConnectableBus("NGEN3")
                .add();

        VoltageLevel vl1 = network.getVoltageLevel("VLGEN");
        DanglingLine dl = vl1.newDanglingLine()
                .setId("DL1")
                .setName("DL1")
                .setR(1)
                .setX(2)
                .setB(3)
                .setG(4)
                .setP0(50)
                .setQ0(30)
                .setUcteXnodeCode("xnode1")
                .setConnectableBus("NGEN")
                .setBus("NGEN")
                .add();
        dl.getTerminal().setP(45);
        dl.getTerminal().setQ(75);

        vlgen3.newDanglingLine()
                .setId("DL2")
                .setName("DL2")
                .setR(1)
                .setX(2)
                .setB(3)
                .setG(4)
                .setP0(50)
                .setQ0(30)
                .setUcteXnodeCode("xnode1")
                .setConnectableBus("NGEN3")
                .setBus("NGEN3")
                .add();

        VscConverterStation vsc1 = vlnew2.newVscConverterStation()
                .setId("VSC1")
                .setName("VSC1")
                .setLossFactor(1)
                .setReactivePowerSetpoint(40)
                .setVoltageRegulatorOn(true)
                .setVoltageSetpoint(150)
                .setConnectableBus("NNEW2")
                .setBus("NNEW2")
                .add();
        vsc1.getTerminal().setP(10);
        vsc1.getTerminal().setQ(30);

        vlgen3.newVscConverterStation()
                .setId("VSC2")
                .setName("VSC2")
                .setLossFactor(1)
                .setReactivePowerSetpoint(40)
                .setVoltageRegulatorOn(true)
                .setVoltageSetpoint(150)
                .setConnectableBus("NGEN3")
                .setBus("NGEN3")
                .add();

        vl1.newLccConverterStation()
                .setId("LCC1")
                .setName("LCC1")
                .setLossFactor(1)
                .setPowerFactor(0.5F)
                .setConnectableBus("NGEN")
                .setBus("NGEN")
                .add();

        LccConverterStation lcc2 = vlnew2.newLccConverterStation()
                .setId("LCC2")
                .setName("LCC2")
                .setLossFactor(1)
                .setPowerFactor(0.5F)
                .setConnectableBus("NNEW2")
                .setBus("NNEW2")
                .add();
        lcc2.getTerminal().setP(110);
        lcc2.getTerminal().setQ(310);

        network.newHvdcLine()
                .setId("HVDC1")
                .setName("HVDC1")
                .setR(1)
                .setMaxP(100)
                .setConvertersMode(HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER)
                .setNominalV(225)
                .setActivePowerSetpoint(500)
                .setConverterStationId1("VSC1")
                .setConverterStationId2("LCC2")
                .add();

        ShuntCompensator shunt1 = vlnew2.newShuntCompensator()
                .setId("SHUNT1")
                .setName("SHUNT1")
                .newLinearModel()
                .setMaximumSectionCount(3)
                .setBPerSection(1)
                .setGPerSection(2)
                .add()
                .setSectionCount(2)
                .setTargetV(225)
                .setVoltageRegulatorOn(true)
                .setTargetDeadband(10)
                .setConnectableBus("NNEW2")
                .setBus("NNEW2")
                .add();
        shunt1.getTerminal().setQ(90);

        vlgen3.newShuntCompensator()
                .setId("SHUNT2")
                .setName("SHUNT2")
                .newLinearModel()
                .setMaximumSectionCount(3)
                .setBPerSection(1)
                .setGPerSection(2)
                .add()
                .setSectionCount(2)
                .setTargetV(225)
                .setVoltageRegulatorOn(true)
                .setTargetDeadband(10)
                .setConnectableBus("NGEN3")
                .setBus("NGEN3")
                .add();

        StaticVarCompensator svc1 = vl1.newStaticVarCompensator()
                .setId("SVC1")
                .setName("SVC1")
                .setRegulationMode(StaticVarCompensator.RegulationMode.VOLTAGE)
                .setVoltageSetpoint(200)
                .setReactivePowerSetpoint(100)
                .setBmin(2)
                .setBmax(30)
                .setConnectableBus("NGEN")
                .setBus("NGEN")
                .add();
        svc1.getTerminal().setP(120);
        svc1.getTerminal().setQ(43);

        vlnew2.newStaticVarCompensator()
                .setId("SVC2")
                .setName("SVC2")
                .setRegulationMode(StaticVarCompensator.RegulationMode.VOLTAGE)
                .setVoltageSetpoint(200)
                .setReactivePowerSetpoint(100)
                .setBmin(2)
                .setBmax(30)
                .setConnectableBus("NNEW2")
                .setBus("NNEW2")
                .add();

        Substation p4 = network.newSubstation()
            .setId("P4")
            .setCountry(Country.FR)
            .setTso("RTE")
            .setGeographicalTags("A")
            .add();
        VoltageLevel vlgen4 = p4.newVoltageLevel()
            .setId("VLGEN4")
            .setNominalV(24.0)
            .setTopologyKind(TopologyKind.NODE_BREAKER)
            .add();
        vlgen4.getNodeBreakerView().newBusbarSection()
            .setId("NGEN4")
            .setName("NGEN4")
            .setNode(0)
            .add();

        // Add new variant
        network.getVariantManager().cloneVariant(VariantManagerConstants.INITIAL_VARIANT_ID, VARIANT_ID);
        network.getVariantManager().setWorkingVariant(VARIANT_ID);

        // Create a shunt compensator only in variant VARIANT_ID
        ShuntCompensator shunt3 = vlgen3.newShuntCompensator()
            .setId("SHUNT3")
            .setName("SHUNT3")
            .newLinearModel()
            .setMaximumSectionCount(3)
            .setBPerSection(1)
            .setGPerSection(2)
            .add()
            .setSectionCount(2)
            .setTargetV(225)
            .setVoltageRegulatorOn(true)
            .setTargetDeadband(10)
            .setConnectableBus("NGEN3")
            .setBus("NGEN3")
            .add();
        shunt3.getTerminal().setQ(90);

        network.getVariantManager().setWorkingVariant(VariantManagerConstants.INITIAL_VARIANT_ID);

        given(networkStoreService.getNetwork(NETWORK_UUID, PreloadingStrategy.COLLECTION)).willReturn(network);
        given(networkStoreService.getNetwork(NETWORK_UUID, PreloadingStrategy.NONE)).willReturn(network);
        given(networkStoreService.getNetwork(NOT_FOUND_NETWORK_ID, PreloadingStrategy.COLLECTION)).willThrow(new PowsyblException("Network " + NOT_FOUND_NETWORK_ID + " not found"));
        given(networkStoreService.getNetwork(NOT_FOUND_NETWORK_ID, PreloadingStrategy.NONE)).willThrow(new PowsyblException("Network " + NOT_FOUND_NETWORK_ID + " not found"));
    }

    private void make3WindingsTransformer(Substation p1, String id,
                                          Function<ThreeWindingsTransformer, ThreeWindingsTransformer.Leg> getPhaseLeg,
                                          Function<ThreeWindingsTransformer, ThreeWindingsTransformer.Leg> getRatioLeg
    ) {
        ThreeWindingsTransformer threeWindingsTransformer = p1.newThreeWindingsTransformer()
                .setId(id)
                .setName(id)
                .setRatedU0(234)
                .newLeg1()
                .setVoltageLevel("VLHV1")
                .setBus("NHV1")
                .setR(45)
                .setX(35)
                .setG(25)
                .setB(15)
                .setRatedU(5)
                .add()
                .newLeg2()
                .setVoltageLevel("VLNEW2")
                .setBus("NNEW2")
                .setR(47)
                .setX(37)
                .setG(27)
                .setB(17)
                .setRatedU(7)
                .add()
                .newLeg3()
                .setVoltageLevel("VLGEN")
                .setBus("NGEN")
                .setR(49)
                .setX(39)
                .setG(29)
                .setB(19)
                .setRatedU(9)
                .add()
                .add();
        threeWindingsTransformer.getTerminal(ThreeWindingsTransformer.Side.ONE).setP(375);
        threeWindingsTransformer.getTerminal(ThreeWindingsTransformer.Side.TWO).setP(225);
        threeWindingsTransformer.getTerminal(ThreeWindingsTransformer.Side.THREE).setP(200);
        threeWindingsTransformer.getTerminal(ThreeWindingsTransformer.Side.ONE).setQ(48);
        threeWindingsTransformer.getTerminal(ThreeWindingsTransformer.Side.TWO).setQ(28);
        threeWindingsTransformer.getTerminal(ThreeWindingsTransformer.Side.THREE).setQ(18);

        getPhaseLeg.apply(threeWindingsTransformer).newPhaseTapChanger()
                .setLowTapPosition(0)
                .setTapPosition(1)
                .setRegulating(true)
                .setRegulationMode(PhaseTapChanger.RegulationMode.CURRENT_LIMITER)
                .setRegulationValue(25)
                .setRegulationTerminal(threeWindingsTransformer.getTerminal(ThreeWindingsTransformer.Side.ONE))
                .setTargetDeadband(22)
                .beginStep()
                .setAlpha(-10)
                .setRho(0.99)
                .setR(1.)
                .setX(4.)
                .setG(0.5)
                .setB(1.5)
                .endStep()
                .beginStep()
                .setAlpha(0)
                .setRho(1)
                .setR(1.1)
                .setX(4.1)
                .setG(0.6)
                .setB(1.6)
                .endStep()
                .beginStep()
                .setAlpha(10)
                .setRho(1.01)
                .setR(1.2)
                .setX(4.2)
                .setG(0.7)
                .setB(1.7)
                .endStep()
                .add();
        getRatioLeg.apply(threeWindingsTransformer).newRatioTapChanger()
                .setLowTapPosition(0)
                .setTapPosition(2)
                .setRegulating(false)
                .setRegulationTerminal(threeWindingsTransformer.getTerminal(ThreeWindingsTransformer.Side.ONE))
                .setTargetDeadband(22)
                .setTargetV(220)
                .beginStep()
                .setRho(0.99)
                .setR(1.)
                .setX(4.)
                .setG(0.5)
                .setB(1.5)
                .endStep()
                .beginStep()
                .setRho(1)
                .setR(1.1)
                .setX(4.1)
                .setG(0.6)
                .setB(1.6)
                .endStep()
                .beginStep()
                .setRho(1.01)
                .setR(1.2)
                .setX(4.2)
                .setG(0.7)
                .setB(1.7)
                .endStep()
                .add();

        getPhaseLeg.apply(threeWindingsTransformer)
                .newCurrentLimits()
                .setPermanentLimit(25)
                .add();
        getRatioLeg.apply(threeWindingsTransformer)
                .newCurrentLimits()
                .setPermanentLimit(54)
                .add();
    }

    private String resourceToString(String resource) throws IOException {
        return new String(ByteStreams.toByteArray(getClass().getResourceAsStream(resource)), StandardCharsets.UTF_8);
    }

    private String buildUrl(String equipments, String variantId, List<String> substationsIds) {
        final String[] url = {"/v1/" + equipments + "/{networkUuid}"};
        if (variantId != null) {
            url[0] += "?variantId=" + variantId;
        }
        if (substationsIds != null) {
            if (variantId == null) {
                url[0] += "?";
            } else {
                url[0] += "&";
            }
            url[0] += "substationId=" + substationsIds.stream().findFirst().orElse("");
            substationsIds.stream().skip(1).forEach(id -> url[0] += "&substationId=" + id);
        }
        return url[0];
    }

    private void failingTest(String equipments, UUID networkUuid, String variantId, List<String> substationsIds) throws Exception {
        mvc.perform(get(buildUrl(equipments, variantId, substationsIds), networkUuid))
            .andExpect(status().isNotFound());
    }

    private void succeedingTest(String equipments, UUID networkUuid, String variantId, List<String> substationsIds, String expectedJson) throws Exception {
        MvcResult res = mvc.perform(get(buildUrl(equipments, variantId, substationsIds), networkUuid))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andReturn();
        JSONAssert.assertEquals(res.getResponse().getContentAsString(), expectedJson, JSONCompareMode.NON_EXTENSIBLE);
    }

    private String buildUrlVoltageLevels(String equipments, String variantId, List<String> substationsIds) {
        final String[] url = {"/v1/networks/{networkUuid}/" + equipments};
        if (variantId != null) {
            url[0] += "?variantId=" + variantId;
        }
        if (substationsIds != null) {
            if (variantId == null) {
                url[0] += "?";
            } else {
                url[0] += "&";
            }
            url[0] += "substationId=" + substationsIds.stream().findFirst().orElse("");
            substationsIds.stream().skip(1).forEach(id -> url[0] += "&substationId=" + id);
        }
        return url[0];
    }

    private void failingVoltageLevelsTest(String equipments, UUID networkUuid, String variantId, List<String> substationsIds) throws Exception {
        mvc.perform(get(buildUrlVoltageLevels(equipments, variantId, substationsIds), networkUuid))
            .andExpect(status().isNotFound());
    }

    private void succeedingVoltageLevelsTest(String equipments, UUID networkUuid, String variantId, List<String> substationsIds, String expectedJson) throws Exception {
        MvcResult res = mvc.perform(get(buildUrlVoltageLevels(equipments, variantId, substationsIds), networkUuid))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andReturn();
        JSONAssert.assertEquals(res.getResponse().getContentAsString(), expectedJson, JSONCompareMode.NON_EXTENSIBLE);
    }

    private String buildUrlBusOrBusbarSection(String equipments, String variantId) {
        final String[] url = {"/v1/networks/{networkUuid}/voltage-levels/{voltageLevelId}/" + equipments};
        if (variantId != null) {
            url[0] += "?variantId=" + variantId;
        }
        return url[0];
    }

    private void failingBusOrBusbarSectionTest(String equipments, UUID networkUuid, String voltageLevelId, String variantId) throws Exception {
        mvc.perform(get(buildUrlBusOrBusbarSection(equipments, variantId), networkUuid, voltageLevelId))
            .andExpect(status().isNotFound());
    }

    private void succeedingBusOrBusbarSectionTest(String equipments, UUID networkUuid, String voltageLevelId, String variantId, String expectedJson) throws Exception {
        MvcResult res = mvc.perform(get(buildUrlBusOrBusbarSection(equipments, variantId), networkUuid, voltageLevelId))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andReturn();
        JSONAssert.assertEquals(res.getResponse().getContentAsString(), expectedJson, JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    public void shouldReturnSubstationsMapData() throws Exception {
        succeedingTest("substations", NETWORK_UUID, null, null, resourceToString("/substations-map-data.json"));
        succeedingTest("substations", NETWORK_UUID, VARIANT_ID, null, resourceToString("/substations-map-data.json"));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfSubstationsMapData() throws Exception {
        failingTest("substations", NOT_FOUND_NETWORK_ID, null, null);
        failingTest("substations", NETWORK_UUID, VARIANT_ID_NOT_FOUND, null);
    }

    @Test
    public void shouldReturnSubstationsMapDataFromIds() throws Exception {
        succeedingTest("substations", NETWORK_UUID, null, List.of("P1"), resourceToString("/partial-substations-map-data.json"));
        succeedingTest("substations", NETWORK_UUID, VARIANT_ID, List.of("P1"), resourceToString("/partial-substations-map-data.json"));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfSubstationsMapDataFromIds() throws Exception {
        failingTest("substations", NOT_FOUND_NETWORK_ID, null, List.of("P1", "P2"));
        failingTest("substations", NETWORK_UUID, VARIANT_ID_NOT_FOUND, List.of("P1", "P2"));
    }

    @Test
    public void shouldReturnLinesMapData() throws Exception {
        succeedingTest("lines", NETWORK_UUID, null, null, resourceToString("/lines-map-data.json"));
        succeedingTest("lines", NETWORK_UUID, VARIANT_ID, null, resourceToString("/lines-map-data.json"));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfLinesMapData() throws Exception {
        failingTest("lines", NOT_FOUND_NETWORK_ID, null, null);
        failingTest("lines", NETWORK_UUID, VARIANT_ID_NOT_FOUND, null);
    }

    @Test
    public void shouldReturnLinesMapDataFromIds() throws Exception {
        succeedingTest("lines", NETWORK_UUID, null, List.of("P3"), resourceToString("/partial-lines-map-data.json"));
        succeedingTest("lines", NETWORK_UUID, VARIANT_ID, List.of("P3"), resourceToString("/partial-lines-map-data.json"));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfLinesMapDataFromIds() throws Exception {
        failingTest("lines", NOT_FOUND_NETWORK_ID, null, List.of("P1"));
        failingTest("lines", NETWORK_UUID, VARIANT_ID_NOT_FOUND, List.of("P1"));
    }

    @Test
    public void shouldReturnGeneratorsMapData() throws Exception {
        succeedingTest("generators", NETWORK_UUID, null, null, resourceToString("/generators-map-data.json"));
        succeedingTest("generators", NETWORK_UUID, VARIANT_ID, null, resourceToString("/generators-map-data.json"));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfGeneratorsMapData() throws Exception {
        failingTest("generators", NOT_FOUND_NETWORK_ID, null, null);
        failingTest("generators", NETWORK_UUID, VARIANT_ID_NOT_FOUND, null);
    }

    @Test
    public void shouldReturnGeneratorsMapDataFromIds() throws Exception {
        succeedingTest("generators", NETWORK_UUID, null, List.of("P2"), "[]");
        succeedingTest("generators", NETWORK_UUID, VARIANT_ID, List.of("P2"), "[]");
    }

    @Test
    public void shouldReturnAnErrorInsteadOfGeneratorsMapDataFromIds() throws Exception {
        failingTest("generators", NOT_FOUND_NETWORK_ID, null, List.of("P1"));
        failingTest("generators", NETWORK_UUID, VARIANT_ID_NOT_FOUND, List.of("P1"));
    }

    @Test
    public void shouldReturnTwoWindingsTransformersMapData() throws Exception {
        succeedingTest("2-windings-transformers", NETWORK_UUID, null, null, resourceToString("/2-windings-transformers-map-data.json"));
        succeedingTest("2-windings-transformers", NETWORK_UUID, VARIANT_ID, null, resourceToString("/2-windings-transformers-map-data.json"));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfTwoWindingsTransformersMapData() throws Exception {
        failingTest("2-windings-transformers", NOT_FOUND_NETWORK_ID, null, null);
        failingTest("2-windings-transformers", NETWORK_UUID, VARIANT_ID_NOT_FOUND, null);
    }

    @Test
    public void shouldReturnTwoWindingsTransformersMapDataFromIds() throws Exception {
        succeedingTest("2-windings-transformers", NETWORK_UUID, null, List.of("P1"), resourceToString("/partial-2-windings-transformers-map-data.json"));
        succeedingTest("2-windings-transformers", NETWORK_UUID, VARIANT_ID, List.of("P1"), resourceToString("/partial-2-windings-transformers-map-data.json"));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfTwoWindingsTransformersMapDataFromIds() throws Exception {
        failingTest("2-windings-transformers", NOT_FOUND_NETWORK_ID, null, List.of("P1"));
        failingTest("2-windings-transformers", NETWORK_UUID, VARIANT_ID_NOT_FOUND, List.of("P1"));
    }

    @Test
    public void shouldReturnThreeWindingsTransformersMapData() throws Exception {
        succeedingTest("3-windings-transformers", NETWORK_UUID, null, null, resourceToString("/3-windings-transformers-map-data.json"));
        succeedingTest("3-windings-transformers", NETWORK_UUID, VARIANT_ID, null, resourceToString("/3-windings-transformers-map-data.json"));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfThreeWindingsTransformersMapData() throws Exception {
        failingTest("3-windings-transformers", NOT_FOUND_NETWORK_ID, null, null);
        failingTest("3-windings-transformers", NETWORK_UUID, VARIANT_ID_NOT_FOUND, null);
    }

    @Test
    public void shouldReturnThreeWindingsTransformersMapDataFromIds() throws Exception {
        succeedingTest("3-windings-transformers", NETWORK_UUID, null, List.of("P3"), "[]");
        succeedingTest("3-windings-transformers", NETWORK_UUID, VARIANT_ID, List.of("P3"), "[]");
    }

    @Test
    public void shouldReturnAnErrorInsteadOfThreeWindingsTransformersMapDataFromIds() throws Exception {
        failingTest("3-windings-transformers", NOT_FOUND_NETWORK_ID, null, List.of("P1"));
        failingTest("3-windings-transformers", NETWORK_UUID, VARIANT_ID_NOT_FOUND, List.of("P1"));
    }

    @Test
    public void shouldReturnAllMapData() throws Exception {
        succeedingTest("all", NETWORK_UUID, null, null, resourceToString("/all-map-data.json"));
        succeedingTest("all", NETWORK_UUID, VARIANT_ID, null, resourceToString("/all-map-data-in-variant.json"));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfAllMapData() throws Exception {
        failingTest("all", NOT_FOUND_NETWORK_ID, null, null);
        failingTest("all", NETWORK_UUID, VARIANT_ID_NOT_FOUND, null);
    }

    @Test
    public void shouldReturnAllMapDataFromIds() throws Exception {
        succeedingTest("all", NETWORK_UUID, null, List.of("P3"), resourceToString("/partial-all-map-data.json"));
        succeedingTest("all", NETWORK_UUID, VARIANT_ID, List.of("P3"), resourceToString("/partial-all-map-data-in-variant.json"));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfAllMapDataFromIds() throws Exception {
        failingTest("all", NOT_FOUND_NETWORK_ID, null, List.of("P1"));
        failingTest("all", NETWORK_UUID, VARIANT_ID_NOT_FOUND, List.of("P1"));
    }

    @Test
    public void shouldReturnBatteriesMapData() throws Exception {
        succeedingTest("batteries", NETWORK_UUID, null, null, resourceToString("/batteries-map-data.json"));
        succeedingTest("batteries", NETWORK_UUID, VARIANT_ID, null, resourceToString("/batteries-map-data.json"));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfBatteriesMapData() throws Exception {
        failingTest("batteries", NOT_FOUND_NETWORK_ID, null, null);
        failingTest("batteries", NETWORK_UUID, VARIANT_ID_NOT_FOUND, null);
    }

    @Test
    public void shouldReturnBatteriesMapDataFromIds() throws Exception {
        succeedingTest("batteries", NETWORK_UUID, null, List.of("P1"), resourceToString("/partial-batteries-map-data.json"));
        succeedingTest("batteries", NETWORK_UUID, VARIANT_ID, List.of("P1"), resourceToString("/partial-batteries-map-data.json"));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfBatteriesMapDataFromIds() throws Exception {
        failingTest("batteries", NOT_FOUND_NETWORK_ID, null, List.of("P1", "P2"));
        failingTest("batteries", NETWORK_UUID, VARIANT_ID_NOT_FOUND, List.of("P1", "P2"));
    }

    @Test
    public void shouldReturnDanglingLinesMapData() throws Exception {
        succeedingTest("dangling-lines", NETWORK_UUID, null, null, resourceToString("/dangling-lines-map-data.json"));
        succeedingTest("dangling-lines", NETWORK_UUID, VARIANT_ID, null, resourceToString("/dangling-lines-map-data.json"));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfDanglingLinesMapData() throws Exception {
        failingTest("dangling-lines", NOT_FOUND_NETWORK_ID, null, null);
        failingTest("dangling-lines", NETWORK_UUID, VARIANT_ID_NOT_FOUND, null);
    }

    @Test
    public void shouldReturnDanglingLinesMapDataFromIds() throws Exception {
        succeedingTest("dangling-lines", NETWORK_UUID, null, List.of("P2"), "[]");
        succeedingTest("dangling-lines", NETWORK_UUID, VARIANT_ID, List.of("P2"), "[]");
    }

    @Test
    public void shouldReturnAnErrorInsteadOfDanglingLinesMapDataFromIds() throws Exception {
        failingTest("dangling-lines", NOT_FOUND_NETWORK_ID, null, List.of("P1", "P2"));
        failingTest("dangling-lines", NETWORK_UUID, VARIANT_ID_NOT_FOUND, List.of("P1", "P2"));
    }

    @Test
    public void shouldReturnLoadsMapData() throws Exception {
        succeedingTest("loads", NETWORK_UUID, null, null, resourceToString("/loads-map-data.json"));
        succeedingTest("loads", NETWORK_UUID, VARIANT_ID, null, resourceToString("/loads-map-data.json"));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfLoadsMapData() throws Exception {
        failingTest("loads", NOT_FOUND_NETWORK_ID, null, null);
        failingTest("loads", NETWORK_UUID, VARIANT_ID_NOT_FOUND, null);
    }

    @Test
    public void shouldReturnLoadsMapDataFromIds() throws Exception {
        succeedingTest("loads", NETWORK_UUID, null, List.of("P2"), resourceToString("/partial-loads-map-data.json"));
        succeedingTest("loads", NETWORK_UUID, VARIANT_ID, List.of("P2"), resourceToString("/partial-loads-map-data.json"));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfLoadsMapDataFromIds() throws Exception {
        failingTest("loads", NOT_FOUND_NETWORK_ID, null, List.of("P1"));
        failingTest("loads", NETWORK_UUID, VARIANT_ID_NOT_FOUND, List.of("P1"));
    }

    @Test
    public void shouldReturnShuntCompensatorsMapData() throws Exception {
        succeedingTest("shunt-compensators", NETWORK_UUID, null, null, resourceToString("/shunt-compensators-map-data.json"));
        succeedingTest("shunt-compensators", NETWORK_UUID, VARIANT_ID, null, resourceToString("/shunt-compensators-map-data-in-variant.json"));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfShuntCompensatorsMapData() throws Exception {
        failingTest("shunt-compensators", NOT_FOUND_NETWORK_ID, null, null);
        failingTest("shunt-compensators", NETWORK_UUID, VARIANT_ID_NOT_FOUND, null);
    }

    @Test
    public void shouldReturnShuntCompensatorsMapDataFromIds() throws Exception {
        succeedingTest("shunt-compensators", NETWORK_UUID, null, List.of("P1"), resourceToString("/partial-shunt-compensators-map-data.json"));
        succeedingTest("shunt-compensators", NETWORK_UUID, VARIANT_ID, List.of("P1", "P3"), resourceToString("/partial-shunt-compensators-map-data-in-variant.json"));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfShuntCompensatorsMapDataFromIds() throws Exception {
        failingTest("shunt-compensators", NOT_FOUND_NETWORK_ID, null, List.of("P1"));
        failingTest("shunt-compensators", NETWORK_UUID, VARIANT_ID_NOT_FOUND, List.of("P1"));
    }

    @Test
    public void shouldReturnStaticVarCompensatorsMapData() throws Exception {
        succeedingTest("static-var-compensators", NETWORK_UUID, null, null, resourceToString("/static-var-compensators-map-data.json"));
        succeedingTest("static-var-compensators", NETWORK_UUID, VARIANT_ID, null, resourceToString("/static-var-compensators-map-data.json"));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfStaticVarCompensatorsMapData() throws Exception {
        failingTest("static-var-compensators", NOT_FOUND_NETWORK_ID, null, null);
        failingTest("static-var-compensators", NETWORK_UUID, VARIANT_ID_NOT_FOUND, null);
    }

    @Test
    public void shouldReturnStaticVarCompensatorsMapDataFromIds() throws Exception {
        succeedingTest("static-var-compensators", NETWORK_UUID, null, List.of("P1"), resourceToString("/partial-static-var-compensators-map-data.json"));
        succeedingTest("static-var-compensators", NETWORK_UUID, VARIANT_ID, List.of("P1"), resourceToString("/partial-static-var-compensators-map-data.json"));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfStaticVarCompensatorsMapDataFromIds() throws Exception {
        failingTest("static-var-compensators", NOT_FOUND_NETWORK_ID, null, List.of("P1"));
        failingTest("static-var-compensators", NETWORK_UUID, VARIANT_ID_NOT_FOUND, List.of("P1"));
    }

    @Test
    public void shouldReturnLccConverterStationsMapData() throws Exception {
        succeedingTest("lcc-converter-stations", NETWORK_UUID, null, null, resourceToString("/lcc-converter-stations-map-data.json"));
        succeedingTest("lcc-converter-stations", NETWORK_UUID, VARIANT_ID, null, resourceToString("/lcc-converter-stations-map-data.json"));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfLccConverterStationsMapData() throws Exception {
        failingTest("lcc-converter-stations", NOT_FOUND_NETWORK_ID, null, null);
        failingTest("lcc-converter-stations", NETWORK_UUID, VARIANT_ID_NOT_FOUND, null);
    }

    @Test
    public void shouldReturnLccConverterStationsMapDataFromIds() throws Exception {
        succeedingTest("lcc-converter-stations", NETWORK_UUID, null, List.of("P1"), resourceToString("/partial-lcc-converter-stations-map-data.json"));
        succeedingTest("lcc-converter-stations", NETWORK_UUID, VARIANT_ID, List.of("P1"), resourceToString("/partial-lcc-converter-stations-map-data.json"));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfLccConverterStationsMapDataFromIds() throws Exception {
        failingTest("lcc-converter-stations", NOT_FOUND_NETWORK_ID, null, List.of("P1"));
        failingTest("lcc-converter-stations", NETWORK_UUID, VARIANT_ID_NOT_FOUND, List.of("P1"));
    }

    @Test
    public void shouldReturnVscConverterStationsMapData() throws Exception {
        succeedingTest("vsc-converter-stations", NETWORK_UUID, null, null, resourceToString("/vsc-converter-stations-map-data.json"));
        succeedingTest("vsc-converter-stations", NETWORK_UUID, VARIANT_ID, null, resourceToString("/vsc-converter-stations-map-data.json"));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfVscConverterStationsMapData() throws Exception {
        failingTest("vsc-converter-stations", NOT_FOUND_NETWORK_ID, null, null);
        failingTest("vsc-converter-stations", NETWORK_UUID, VARIANT_ID_NOT_FOUND, null);
    }

    @Test
    public void shouldReturnVscConverterStationsMapDataFromIds() throws Exception {
        succeedingTest("vsc-converter-stations", NETWORK_UUID, null, List.of("P1"), resourceToString("/partial-vsc-converter-stations-map-data.json"));
        succeedingTest("vsc-converter-stations", NETWORK_UUID, VARIANT_ID, List.of("P1"), resourceToString("/partial-vsc-converter-stations-map-data.json"));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfVscConverterStationsMapDataFromIds() throws Exception {
        failingTest("vsc-converter-stations", NOT_FOUND_NETWORK_ID, null, List.of("P1"));
        failingTest("vsc-converter-stations", NETWORK_UUID, VARIANT_ID_NOT_FOUND, List.of("P1"));
    }

    @Test
    public void shouldReturnHvdcLinesMapData() throws Exception {
        succeedingTest("hvdc-lines", NETWORK_UUID, null, null, resourceToString("/hvdc-lines-map-data.json"));
        succeedingTest("hvdc-lines", NETWORK_UUID, VARIANT_ID, null, resourceToString("/hvdc-lines-map-data.json"));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfHvdcLinesMapData() throws Exception {
        failingTest("hvdc-lines", NOT_FOUND_NETWORK_ID, null, null);
        failingTest("hvdc-lines", NETWORK_UUID, VARIANT_ID_NOT_FOUND, null);
    }

    @Test
    public void shouldReturnHvdcLinesMapDataFromIds() throws Exception {
        succeedingTest("hvdc-lines", NETWORK_UUID, null, List.of("P3"), "[]");
        succeedingTest("hvdc-lines", NETWORK_UUID, VARIANT_ID, List.of("P3"), "[]");
    }

    @Test
    public void shouldReturnAnErrorInsteadOfHvdcLinesMapDataFromIds() throws Exception {
        failingTest("hvdc-lines", NOT_FOUND_NETWORK_ID, null, List.of("P1", "P2"));
        failingTest("hvdc-lines", NETWORK_UUID, VARIANT_ID_NOT_FOUND, List.of("P1", "P2"));
    }

    @Test
    public void shouldReturnVoltageLevelsMapData() throws Exception {
        succeedingVoltageLevelsTest("voltage-levels", NETWORK_UUID, null, null, resourceToString("/voltage-levels-map-data.json"));
        succeedingVoltageLevelsTest("voltage-levels", NETWORK_UUID, VARIANT_ID, null, resourceToString("/voltage-levels-map-data.json"));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfVoltageLevelsMapData() throws Exception {
        failingVoltageLevelsTest("voltage-levels", NOT_FOUND_NETWORK_ID, null, null);
        failingVoltageLevelsTest("voltage-levels", NETWORK_UUID, VARIANT_ID_NOT_FOUND, null);
    }

    @Test
    public void shouldReturnVoltageLevelsMapDataFromIds() throws Exception {
        succeedingVoltageLevelsTest("voltage-levels", NETWORK_UUID, null, List.of("P3"), resourceToString("/partial-voltage-levels-map-data.json"));
        succeedingVoltageLevelsTest("voltage-levels", NETWORK_UUID, VARIANT_ID, List.of("P3"), resourceToString("/partial-voltage-levels-map-data.json"));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfVoltageLevelsMapDataFromIds() throws Exception {
        failingVoltageLevelsTest("voltage-levels", NOT_FOUND_NETWORK_ID, null, List.of("P1", "P2"));
        failingVoltageLevelsTest("voltage-levels", NETWORK_UUID, VARIANT_ID_NOT_FOUND, List.of("P1", "P2"));
    }

    @Test
    public void shouldReturnBusesMapData() throws Exception {
        succeedingBusOrBusbarSectionTest("configured-buses", NETWORK_UUID, "VLGEN", null, resourceToString("/buses-map-data.json"));
        succeedingBusOrBusbarSectionTest("configured-buses", NETWORK_UUID, "VLGEN", VARIANT_ID, resourceToString("/buses-map-data.json"));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfBusesMapData() throws Exception {
        failingBusOrBusbarSectionTest("configured-buses", NOT_FOUND_NETWORK_ID, "VLGEN", null);
        failingBusOrBusbarSectionTest("configured-buses", NETWORK_UUID, "VLGEN", VARIANT_ID_NOT_FOUND);
    }

    @Test
    public void shouldReturnBusbarSectionsMapData() throws Exception {
        succeedingBusOrBusbarSectionTest("busbar-sections", NETWORK_UUID, "VLGEN4", null, resourceToString("/busbar-sections-map-data.json"));
        succeedingBusOrBusbarSectionTest("busbar-sections", NETWORK_UUID, "VLGEN4", VARIANT_ID, resourceToString("/busbar-sections-map-data.json"));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfBusbarSectionMapData() throws Exception {
        failingBusOrBusbarSectionTest("busbar-sections", NOT_FOUND_NETWORK_ID, "VLGEN4", null);
        failingBusOrBusbarSectionTest("busbar-sections", NETWORK_UUID, "VLGEN4", VARIANT_ID_NOT_FOUND);
    }
}
