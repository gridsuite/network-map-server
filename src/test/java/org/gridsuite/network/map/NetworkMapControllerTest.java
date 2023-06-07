/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map;

import com.google.common.io.ByteStreams;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.*;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.network.store.client.NetworkStoreService;
import com.powsybl.network.store.client.PreloadingStrategy;
import com.powsybl.network.store.iidm.impl.NetworkFactoryImpl;
import lombok.SneakyThrows;
import org.gridsuite.network.map.dto.ElementInfos;
import org.gridsuite.network.map.model.ElementType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.LinkedMultiValueMap;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
public class NetworkMapControllerTest {

    private static final UUID NETWORK_UUID = UUID.fromString("7928181c-7977-4592-ba19-88027e4254e4");

    private static final UUID NOT_FOUND_NETWORK_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

    private static final String VARIANT_ID = "variant_1";
    private static final String VARIANT_ID_NOT_FOUND = "variant_notFound";

    public static final String QUERY_PARAM_SUBSTATION_ID = "substationId";

    public static final String QUERY_PARAM_VARIANT_ID = "variantId";
    public static final String QUERY_PARAM_SUBSTATIONS_IDS = "substationsIds";
    public static final String QUERY_PARAM_ELEMENT_TYPE = "elementType";
    public static final String QUERY_PARAM_INFO_TYPE = "infoType";

    @Autowired
    private MockMvc mvc;

    @MockBean
    private NetworkStoreService networkStoreService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        Network network = EurostagTutorialExample1Factory.createWithMoreGenerators(new NetworkFactoryImpl());
        Line l2 = network.getLine("NHV1_NHV2_2");
        l2.newCurrentLimits1().setPermanentLimit(Double.NaN).add();
        l2.newCurrentLimits2().setPermanentLimit(Double.NaN).add();
        Line l1 = network.getLine("NHV1_NHV2_1");
        l1.getTerminal1().setP(1.1)
                .setQ(2.2);
        l1.getTerminal2().setP(3.33)
                .setQ(4.44);
        l1.setB1(5).setB2(6).setG1(7).setG2(8);
        l1.setR(9).setX(10);
        l1.newCurrentLimits1().setPermanentLimit(700.4)
                .beginTemporaryLimit()
                .setName("IT5")
                .setValue(250.0)
                .setAcceptableDuration(300)
                .endTemporaryLimit()
                .add();
        l1.newCurrentLimits2().setPermanentLimit(800.8)
                .beginTemporaryLimit()
                .setName("IT10")
                .setValue(200.0)
                .setAcceptableDuration(600)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                .setName("IT20")
                .setValue(300.0)
                .setAcceptableDuration(1200)
                .endTemporaryLimit()
                .add();
        l1.newExtension(ConnectablePositionAdder.class)
                .newFeeder1()
                .withName("feederName1")
                .withOrder(0)
                .withDirection(ConnectablePosition.Direction.TOP).add()
                .newFeeder2()
                .withName("feederName2")
                .withOrder(0)
                .withDirection(ConnectablePosition.Direction.BOTTOM).add()
                .add();
        network.getSubstation("P2").setCountry(null);

        TwoWindingsTransformer t1 = network.getTwoWindingsTransformer("NHV2_NLOAD");
        t1.getTerminal1().setP(5.5)
                .setQ(6.6);
        t1.getTerminal2().setP(7.77)
                .setQ(8.88);
        t1.getRatioTapChanger().setTapPosition(2);
        t1.newExtension(BranchStatusAdder.class).withStatus(BranchStatus.Status.PLANNED_OUTAGE).add();
        t1.newExtension(ConnectablePositionAdder.class)
                .newFeeder1()
                .withName("feederName1")
                .withOrder(0)
                .withDirection(ConnectablePosition.Direction.TOP).add()
                .newFeeder2()
                .withName("feederName2")
                .withOrder(0)
                .withDirection(ConnectablePosition.Direction.BOTTOM).add()
                .add();

        TwoWindingsTransformer t2 = network.getTwoWindingsTransformer("NGEN_NHV1");
        t2.getTerminal1().setP(11.1)
                .setQ(12.2);
        t2.getTerminal2().setP(13.33)
                .setQ(14.44);
        t2.newCurrentLimits1().setPermanentLimit(750.4)
                .beginTemporaryLimit()
                .setName("IT5")
                .setValue(300)
                .setAcceptableDuration(2087)
                .endTemporaryLimit()
                .add();
        t2.newCurrentLimits2().setPermanentLimit(780.6)
                .beginTemporaryLimit()
                .setName("N/A")
                .setValue(2147483647)
                .setAcceptableDuration(664)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                .setName("IT20")
                .setValue(1200)
                .setAcceptableDuration(961)
                .endTemporaryLimit()
                .add();
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
        t2.newExtension(BranchStatusAdder.class).withStatus(BranchStatus.Status.PLANNED_OUTAGE).add();
        t2.newExtension(ConnectablePositionAdder.class)
                .newFeeder1()
                .withName("feederName1")
                .withOrder(0)
                .withDirection(ConnectablePosition.Direction.TOP).add()
                .newFeeder2()
                .withName("feederName2")
                .withOrder(0)
                .withDirection(ConnectablePosition.Direction.BOTTOM).add()
                .add();

        Substation p1 = network.getSubstation("P1");

        TwoWindingsTransformerAdder twoWindingsTransformerAdder = p1.newTwoWindingsTransformer();
        twoWindingsTransformerAdder.setId("NGEN_NHV2")
                .setVoltageLevel1("VLGEN")
                .setBus1("NGEN")
                .setConnectableBus1("NGEN")
                .setRatedU1(400.)
                .setVoltageLevel2("VLHV1")
                .setBus2("NHV1")
                .setConnectableBus2("NHV1")
                .setRatedU2(158.)
                .setR(47)
                .setG(27)
                .setB(17)
                .setX(23)
                .add();

        TwoWindingsTransformer t3 = network.getTwoWindingsTransformer("NGEN_NHV2");
        t3.newCurrentLimits1().setPermanentLimit(300.4)
                .beginTemporaryLimit()
                .setName("IT20")
                .setValue(400)
                .setAcceptableDuration(87)
                .endTemporaryLimit()
                .add();
        t3.newCurrentLimits2().setPermanentLimit(280.6)
                .beginTemporaryLimit()
                .setName("N/A")
                .setValue(98)
                .setAcceptableDuration(34)
                .endTemporaryLimit()
                .add();

        Generator gen = network.getGenerator("GEN");
        gen.getTerminal().setP(25);
        gen.getTerminal().setQ(32);
        gen.setTargetP(28);
        gen.setRatedS(27);
        gen.newExtension(ActivePowerControlAdder.class).withParticipate(true).withDroop(4).add();
        gen.newExtension(ConnectablePositionAdder.class)
                .newFeeder()
                .withName("feederName")
                .withOrder(0)
                .withDirection(ConnectablePosition.Direction.TOP).add()
                .add();
        gen.setRegulatingTerminal(network.getLine("NHV1_NHV2_1").getTerminal("VLHV1"));
        gen.newMinMaxReactiveLimits().setMinQ(-500)
                .setMaxQ(500)
                .add();
        gen.newExtension(ConnectablePositionAdder.class)
                .newFeeder()
                .withName("feederName")
                .withOrder(0)
                .withDirection(ConnectablePosition.Direction.TOP).add()
                .add();
        gen.newExtension(CoordinatedReactiveControlAdder.class)
                .withQPercent(10.0)
                .add();

        Generator gen2 = network.getGenerator("GEN2");
        //Setting regulating terminal to gen terminal itself should make "regulatingTerminal" to empty in json
        gen2.setRegulatingTerminal(gen2.getTerminal());
        gen2.newReactiveCapabilityCurve().beginPoint()
                .setP(0)
                .setMinQ(6)
                .setMaxQ(7)
                .endPoint()
                .beginPoint()
                .setP(1)
                .setMaxQ(5)
                .setMinQ(4)
                .endPoint()
                .beginPoint()
                .setP(3)
                .setMinQ(4)
                .setMaxQ(5)
                .endPoint()
                .add();
        gen2.newExtension(ConnectablePositionAdder.class)
                .newFeeder()
                .withName("feederName")
                .withOrder(0)
                .withDirection(ConnectablePosition.Direction.TOP).add()
                .add();
        gen2.newExtension(CoordinatedReactiveControlAdder.class)
                .withQPercent(10.0)
                .add();

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
        line3.newExtension(ConnectablePositionAdder.class)
                .newFeeder1()
                .withName("feederName1")
                .withOrder(0)
                .withDirection(ConnectablePosition.Direction.TOP).add()
                .newFeeder2()
                .withName("feederName2")
                .withOrder(0)
                .withDirection(ConnectablePosition.Direction.BOTTOM).add()
                .add();
        Battery b1 = vlnew2.newBattery()
                .setId("BATTERY1")
                .setName("BATTERY1")
                .setMinP(0)
                .setMaxP(10)
                .setTargetP(1)
                .setTargetQ(1)
                .setConnectableBus("NNEW2")
                .add();
        b1.getTerminal().setP(50);
        b1.getTerminal().setQ(70);

        vlgen3.newBattery()
                .setId("BATTERY2")
                .setName("BATTERY2")
                .setMinP(0)
                .setMaxP(10)
                .setTargetP(1)
                .setTargetQ(1)
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

        VscConverterStation vsc3 = vlnew2.newVscConverterStation()
                .setId("VSC3")
                .setName("VSC3")
                .setLossFactor(1)
                .setReactivePowerSetpoint(40)
                .setVoltageRegulatorOn(false)
                .setVoltageSetpoint(Double.NaN)
                .setConnectableBus("NNEW2")
                .setBus("NNEW2")
                .add();
        vsc3.getTerminal().setP(10);
        vsc3.getTerminal().setQ(30);

        VscConverterStation vsc4 = vlnew2.newVscConverterStation()
                .setId("VSC4")
                .setName("VSC4")
                .setLossFactor(1)
                .setReactivePowerSetpoint(40)
                .setVoltageRegulatorOn(false)
                .setConnectableBus("NNEW2")
                .setBus("NNEW2")
                .add();
        vsc4.getTerminal().setP(10);
        vsc4.getTerminal().setQ(30);

        VscConverterStation vsc5 = vlnew2.newVscConverterStation()
                .setId("VSC5")
                .setName("VSC5")
                .setLossFactor(1)
                .setReactivePowerSetpoint(Double.NaN)
                .setVoltageRegulatorOn(true)
                .setVoltageSetpoint(150)
                .setConnectableBus("NNEW2")
                .setBus("NNEW2")
                .add();
        vsc5.getTerminal().setP(10);
        vsc5.getTerminal().setQ(30);

        VscConverterStation vsc6 = vlnew2.newVscConverterStation()
                .setId("VSC6")
                .setName("VSC6")
                .setLossFactor(1)
                .setVoltageRegulatorOn(true)
                .setVoltageSetpoint(150)
                .setConnectableBus("NNEW2")
                .setBus("NNEW2")
                .add();
        vsc6.getTerminal().setP(10);
        vsc6.getTerminal().setQ(30);
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
        network.newHvdcLine()
                .setId("HVDC3")
                .setName("HVDC3")
                .setR(1)
                .setMaxP(100)
                .setConvertersMode(HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER)
                .setNominalV(225)
                .setActivePowerSetpoint(500)
                .setConverterStationId1("VSC3")
                .setConverterStationId2("VSC4")
                .add();
        network.newHvdcLine()
                .setId("HVDC4")
                .setName("HVDC4")
                .setR(1)
                .setMaxP(100)
                .setConvertersMode(HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER)
                .setNominalV(225)
                .setActivePowerSetpoint(500)
                .setConverterStationId1("VSC5")
                .setConverterStationId2("VSC6")
                .add();

        HvdcLine hvdcLineWithExtension = network.newHvdcLine()
                .setId("HVDC2")
                .setName("HVDC2")
                .setR(1)
                .setMaxP(100)
                .setConvertersMode(HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER)
                .setNominalV(225)
                .setActivePowerSetpoint(500)
                .setConverterStationId1("VSC1")
                .setConverterStationId2("LCC2")
                .add();

        hvdcLineWithExtension.newExtension(HvdcOperatorActivePowerRangeAdder.class)
                .withOprFromCS2toCS1(900F)
                .withOprFromCS1toCS2(1000F)
                .add();

        hvdcLineWithExtension.newExtension(HvdcAngleDroopActivePowerControlAdder.class)
                .withP0(190F)
                .withDroop(180F)
                .withEnabled(true)
                .add();

        ShuntCompensator shunt1 = vlnew2.newShuntCompensator()
                .setId("SHUNT1")
                .setName("SHUNT1")
                .newLinearModel()
                .setMaximumSectionCount(8)
                .setBPerSection(5)
                .setGPerSection(2)
                .add()
                .setSectionCount(4)
                .setTargetV(225)
                .setVoltageRegulatorOn(true)
                .setTargetDeadband(10)
                .setConnectableBus("NNEW2")
                .setBus("NNEW2")
                .add();
        shunt1.getTerminal().setQ(90);
        shunt1.newExtension(ConnectablePositionAdder.class)
                .newFeeder()
                .withName("feederName")
                .withOrder(0)
                .withDirection(ConnectablePosition.Direction.TOP).add()
                .add();

        var shunt2 = vlgen3.newShuntCompensator()
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
        shunt2.newExtension(ConnectablePositionAdder.class)
                .newFeeder()
                .withName("feederName")
                .withOrder(0)
                .withDirection(ConnectablePosition.Direction.TOP).add()
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
        vlgen4.getNodeBreakerView()
                .getBusbarSection("NGEN4")
                .newExtension(BusbarSectionPositionAdder.class)
                .withBusbarIndex(1)
                .withSectionIndex(2)
                .add();
        Substation p5 = network.newSubstation()
                .setId("P5")
                .setCountry(Country.FR)
                .setTso("RTE")
                .setGeographicalTags("A")
                .add();
        VoltageLevel vlgen5 = p5.newVoltageLevel()
                .setId("VLGEN5")
                .setNominalV(24.0)
                .setHighVoltageLimit(30)
                .setLowVoltageLimit(20)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        vlgen5.getNodeBreakerView().newBusbarSection()
                .setId("NGEN5")
                .setName("NGEN5")
                .setNode(0)
                .add();
        vlgen5.newExtension(IdentifiableShortCircuitAdder.class).withIpMin(0.0).withIpMax(100.0).add();

        // Create a connected shunt compensator on a NODE_BREAKER voltage level
        vlgen4.newShuntCompensator().setId("SHUNT_VLNB")
                .setName("SHUNT_VLNB")
                .newLinearModel()
                .setMaximumSectionCount(3)
                .setBPerSection(1)
                .setGPerSection(2)
                .add()
                .setSectionCount(2)
                .setTargetV(225)
                .setVoltageRegulatorOn(true)
                .setTargetDeadband(10)
                .setNode(2)
                .add();
        createSwitch(vlgen4, "VL4_BBS_SHUNT_DISCONNECTOR", SwitchKind.DISCONNECTOR, false, 0, 1);
        createSwitch(vlgen4, "VL4_SHUNT_BREAKER", SwitchKind.BREAKER, false, 1, 2);

        vlnew2.newLoad().setId("LOAD_WITH_NULL_NAME").setBus("NNEW2").setConnectableBus("NNEW2").setP0(600.0).setQ0(200.0).setName(null).add();
        vlnew2.newLoad().setId("LOAD_ID").setBus("NNEW2").setConnectableBus("NNEW2").setP0(600.0).setQ0(200.0).setName("LOAD_NAME").add();

        network.getLoad("LOAD")
                .newExtension(ConnectablePositionAdder.class)
                .newFeeder()
                .withName("feederName")
                .withOrder(0)
                .withDirection(ConnectablePosition.Direction.TOP).add()
                .add();

        // Add new variant
        network.getVariantManager().cloneVariant(VariantManagerConstants.INITIAL_VARIANT_ID, VARIANT_ID);
        network.getVariantManager().setWorkingVariant(VARIANT_ID);

        // Disconnect shunt compensator "SHUNT_VLNB" in variant VARIANT_ID to test SJB retrieval even if equipment disconnected
        network.getSwitch("VL4_SHUNT_BREAKER").setOpen(true);

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
        shunt3.newExtension(ConnectablePositionAdder.class)
                .newFeeder()
                .withName("feederName")
                .withOrder(0)
                .withDirection(ConnectablePosition.Direction.TOP).add()
                .add();

        network.getVariantManager().setWorkingVariant(VariantManagerConstants.INITIAL_VARIANT_ID);

        given(networkStoreService.getNetwork(NETWORK_UUID, PreloadingStrategy.COLLECTION)).willReturn(network);
        given(networkStoreService.getNetwork(NETWORK_UUID, PreloadingStrategy.NONE)).willReturn(network);
        given(networkStoreService.getNetwork(NOT_FOUND_NETWORK_ID, PreloadingStrategy.COLLECTION)).willThrow(new PowsyblException("Network " + NOT_FOUND_NETWORK_ID + " not found"));
        given(networkStoreService.getNetwork(NOT_FOUND_NETWORK_ID, PreloadingStrategy.NONE)).willThrow(new PowsyblException("Network " + NOT_FOUND_NETWORK_ID + " not found"));
    }

    private static void createSwitch(VoltageLevel vl, String id, SwitchKind kind, boolean open, int node1, int node2) {
        vl.getNodeBreakerView().newSwitch()
                .setId(id)
                .setName(id)
                .setKind(kind)
                .setRetained(kind.equals(SwitchKind.BREAKER))
                .setOpen(open)
                .setFictitious(false)
                .setNode1(node1)
                .setNode2(node2)
                .add();
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

    private String buildUrlForList(String equipmentsType, String variantId, List<String> immutableListSubstationIds, boolean onlyIds) {
        List<String> substationsIds = immutableListSubstationIds == null ? List.of() : immutableListSubstationIds.stream().collect(Collectors.toList());
        StringBuffer url =
                onlyIds ? new StringBuffer("/v1/networks/{networkUuid}" + "/elements-ids") :
                        new StringBuffer("/v1/networks/{networkUuid}/" + equipmentsType);
        if (variantId == null && substationsIds.isEmpty()) {
            if (onlyIds) {
                url.append("?elementType=" + equipmentsType);
            }
            return url.toString();
        }

        url.append("?");
        url.append(variantId != null ? "variantId=" + variantId : "substationsIds=" + substationsIds.remove(0));
        url.append(String.join("", substationsIds.stream().map(id -> String.format("&substationsIds=%s", id)).collect(Collectors.toList())));
        if (onlyIds) {
            url.append("&elementType=" + equipmentsType);
        }
        return url.toString();
    }

    private String buildUrlForElement(String equipments, String variantId, List<String> immutableListSubstationIds) {
        List<String> substationsIds = immutableListSubstationIds == null ? List.of() : immutableListSubstationIds.stream().collect(Collectors.toList());
        StringBuffer url = new StringBuffer("/v1/networks/{networkUuid}/" + equipments + "/{elementId}");
        if (variantId == null && substationsIds.isEmpty()) {
            return url.toString();
        }

        url.append("?");
        url.append(variantId != null ? "variantId=" + variantId : "substationId=" + substationsIds.remove(0));
        url.append(String.join("", substationsIds.stream().map(id -> String.format("&substationId=%s", id)).collect(Collectors.toList())));

        return url.toString();
    }

    private void failingTestForList(String equipments, UUID networkUuid, String variantId, List<String> substationsIds, boolean onlyIds) throws Exception {
        mvc.perform(get(buildUrlForList(equipments, variantId, substationsIds, onlyIds), networkUuid))
                .andExpect(status().isNotFound());
    }

    private void succeedingTestForList(String equipments, UUID networkUuid, String variantId, List<String> substationsIds, boolean onlyIds, String expectedJson) throws Exception {
        MvcResult res = mvc.perform(get(buildUrlForList(equipments, variantId, substationsIds, onlyIds), networkUuid))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn();
        JSONAssert.assertEquals(expectedJson, res.getResponse().getContentAsString(), JSONCompareMode.NON_EXTENSIBLE);
    }

    private void succeedingTestForElement(String equipments, UUID networkUuid, String variantId, List<String> substationsIds, String elementId, String expectedJson) throws Exception {
        MvcResult res = mvc.perform(get(buildUrlForElement(equipments, variantId, substationsIds), networkUuid, elementId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn();

        JSONAssert.assertEquals(expectedJson, res.getResponse().getContentAsString(), JSONCompareMode.NON_EXTENSIBLE);
    }

    @SneakyThrows
    private void succeedingTestForElementInfos(UUID networkUuid, String variantId, ElementType elementType, ElementInfos.InfoType infoType, String elementId, String expectedJson) {
        MvcResult mvcResult = mvc.perform(get("/v1/networks/{networkUuid}/elements/{elementId}", networkUuid, elementId)
                        .queryParam(QUERY_PARAM_VARIANT_ID, variantId)
                        .queryParam(QUERY_PARAM_ELEMENT_TYPE, elementType.name())
                        .queryParam(QUERY_PARAM_INFO_TYPE, infoType.name())
                )
                .andExpect(status().isOk())
                .andReturn();

        JSONAssert.assertEquals(expectedJson, mvcResult.getResponse().getContentAsString(), JSONCompareMode.NON_EXTENSIBLE);
    }

    @SneakyThrows
    private void succeedingTestForElementsInfos(UUID networkUuid, String variantId, ElementType elementType, ElementInfos.InfoType infoType, List<String> substationsIds, String expectedJson) {
        LinkedMultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.add(QUERY_PARAM_VARIANT_ID, variantId);
        queryParams.add(QUERY_PARAM_ELEMENT_TYPE, elementType.name());
        queryParams.add(QUERY_PARAM_INFO_TYPE, infoType.name());
        if (substationsIds != null) {
            queryParams.addAll(QUERY_PARAM_SUBSTATIONS_IDS, substationsIds);
        }
        MvcResult mvcResult = mvc.perform(get("/v1/networks/{networkUuid}/elements", networkUuid).queryParams(queryParams))
                .andExpect(status().isOk())
                .andReturn();

        JSONAssert.assertEquals(expectedJson, mvcResult.getResponse().getContentAsString(), JSONCompareMode.NON_EXTENSIBLE);
    }

    @SneakyThrows
    private void succeedingTestForEquipmentsIds(UUID networkUuid, String variantId, ElementType elementType, List<String> substationsIds, String expectedJson) {
        LinkedMultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.add(QUERY_PARAM_VARIANT_ID, variantId);
        queryParams.add(QUERY_PARAM_ELEMENT_TYPE, elementType.name());
        if (substationsIds != null) {
            queryParams.addAll(QUERY_PARAM_SUBSTATIONS_IDS, substationsIds);
        }
        MvcResult mvcResult = mvc.perform(get("/v1/networks/{networkUuid}/elements-ids", networkUuid).queryParams(queryParams))
                .andExpect(status().isOk())
                .andReturn();

        JSONAssert.assertEquals(mvcResult.getResponse().getContentAsString(), expectedJson, JSONCompareMode.NON_EXTENSIBLE);
    }

    @SneakyThrows
    private void succeedingTestForEquipmentsInfos(UUID networkUuid, String variantId, String equipmentPath, List<String> substationsIds, String expectedJson) {
        LinkedMultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.add(QUERY_PARAM_VARIANT_ID, variantId);
        if (substationsIds != null) {
            queryParams.addAll(QUERY_PARAM_SUBSTATION_ID, substationsIds);
        }
        MvcResult mvcResult = mvc.perform(get("/v1/networks/{networkUuid}/{equipmentPath}", networkUuid, equipmentPath).queryParams(queryParams))
                .andExpect(status().isOk())
                .andReturn();

        JSONAssert.assertEquals(expectedJson, mvcResult.getResponse().getContentAsString(), JSONCompareMode.NON_EXTENSIBLE);
    }

    private void failingTestForElement(String equipments, UUID networkUuid, String variantId, List<String> substationsIds, String elementId) throws Exception {
        mvc.perform(get(buildUrlForElement(equipments, variantId, substationsIds), networkUuid, elementId))
                .andExpect(status().isNotFound());
    }

    private void shouldNotExistElement(String equipments, UUID networkUuid, String variantId, List<String> substationsIds, String elementId) throws Exception {
        mvc.perform(get(buildUrlForElement(equipments, variantId, substationsIds), networkUuid, elementId))
                .andExpect(status().isNoContent());
    }

    private String buildUrlVoltageLevels(String variantId, List<String> immutableListSubstationIds, boolean onlyIds) {
        List<String> substationsIds = immutableListSubstationIds == null ? List.of() : immutableListSubstationIds.stream().collect(Collectors.toList());
        StringBuffer url =
                onlyIds ? new StringBuffer("/v1/networks/{networkUuid}" + "/elements-ids") :
                        new StringBuffer("/v1/networks/{networkUuid}/voltage-levels");
        url.append("?");
        if (variantId == null && substationsIds.isEmpty()) {
            if (onlyIds) {
                url.append("elementType=" + ElementType.VOLTAGE_LEVEL);
            }
            return url.toString();
        }

        url.append(variantId != null ? "variantId=" + variantId : "substationsIds=" + substationsIds.remove(0));
        url.append(String.join("", substationsIds.stream().map(id -> String.format("&substationsIds=%s", id)).collect(Collectors.toList())));
        if (onlyIds) {
            url.append("&elementType=" + ElementType.VOLTAGE_LEVEL);
        }
        return url.toString();
    }

    private void failingVoltageLevelsTest(UUID networkUuid, String variantId, List<String> substationsIds, boolean onlyIds) throws Exception {
        mvc.perform(get(buildUrlVoltageLevels(variantId, substationsIds, onlyIds), networkUuid))
                .andExpect(status().isNotFound());
    }

    private void succeedingVoltageLevelsTest(UUID networkUuid, String variantId, List<String> substationsIds, boolean onlyIds, String expectedJson) throws Exception {
        MvcResult res = mvc.perform(get(buildUrlVoltageLevels(variantId, substationsIds, onlyIds), networkUuid))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn();
        JSONAssert.assertEquals(res.getResponse().getContentAsString(), expectedJson, JSONCompareMode.NON_EXTENSIBLE);
    }

    private String buildUrlBusOrBusbarSection(String equipments, String variantId) {
        StringBuffer url = new StringBuffer("/v1/networks/{networkUuid}/voltage-levels/{voltageLevelId}/");
        url.append(equipments);
        if (variantId != null) {
            url.append("?variantId=" + variantId);
        }
        return url.toString();
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
    public void shouldReturnSubstationsTabData() throws Exception {
        succeedingTestForElementsInfos(NETWORK_UUID, null, ElementType.SUBSTATION, ElementInfos.InfoType.TAB, null, resourceToString("/substations-tab-data.json"));
        succeedingTestForElementsInfos(NETWORK_UUID, VARIANT_ID, ElementType.SUBSTATION, ElementInfos.InfoType.TAB, null, resourceToString("/substations-tab-data.json"));
    }

    @Test
    public void shouldReturnSubstationsIds() throws Exception {
        succeedingTestForList(ElementType.SUBSTATION.toString(), NETWORK_UUID, null, null, true, List.of("P1", "P2", "P3", "P4", "P5").toString());
        succeedingTestForList(ElementType.SUBSTATION.toString(), NETWORK_UUID, VARIANT_ID, null, true, List.of("P1", "P2", "P3", "P4", "P5").toString());
    }

    @Test
    public void shouldReturnAnErrorInsteadOfSubstationsMapData() throws Exception {
        failingTestForList("substations", NOT_FOUND_NETWORK_ID, null, null, false);
        failingTestForList("substations", NETWORK_UUID, VARIANT_ID_NOT_FOUND, null, false);
    }

    @Test
    public void shouldReturnAnErrorInsteadOfSubstationsMapDataFromIds() throws Exception {
        failingTestForList("substations", NOT_FOUND_NETWORK_ID, null, List.of("P1", "P2"), false);
        failingTestForList("substations", NETWORK_UUID, VARIANT_ID_NOT_FOUND, List.of("P1", "P2"), false);
    }

    @Test
    public void shouldReturnSubstationMapData() throws Exception {
        succeedingTestForElementInfos(NETWORK_UUID, null, ElementType.SUBSTATION, ElementInfos.InfoType.MAP, "P4", resourceToString("/substation-map-data.json"));
        succeedingTestForElementInfos(NETWORK_UUID, VARIANT_ID, ElementType.SUBSTATION, ElementInfos.InfoType.MAP, "P4", resourceToString("/substation-map-data.json"));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfSubstationMapData() throws Exception {
        failingTestForElement("substations", NOT_FOUND_NETWORK_ID, null, null, "NOT_EXISTING_SUBSTATION");
        failingTestForElement("substations", NETWORK_UUID, VARIANT_ID_NOT_FOUND, null, "NOT_EXISTING_SUBSTATION");
    }

    @Test
    public void shouldReturnLinesFormData() throws Exception {
        succeedingTestForElementsInfos(NETWORK_UUID, null, ElementType.LINE, ElementInfos.InfoType.FORM, null, resourceToString("/lines-form-data.json"));
        succeedingTestForElementsInfos(NETWORK_UUID, VARIANT_ID, ElementType.LINE, ElementInfos.InfoType.FORM, null, resourceToString("/lines-form-data.json"));

        succeedingTestForElementsInfos(NETWORK_UUID, null, ElementType.LINE, ElementInfos.InfoType.FORM, List.of("P3"), resourceToString("/partial-lines-form-data.json"));
        succeedingTestForElementsInfos(NETWORK_UUID, VARIANT_ID, ElementType.LINE, ElementInfos.InfoType.FORM, List.of("P3"), resourceToString("/partial-lines-form-data.json"));
    }

    @Test
    public void shouldReturnLinesListData() throws Exception {
        succeedingTestForElementsInfos(NETWORK_UUID, null, ElementType.LINE, ElementInfos.InfoType.LIST, null, resourceToString("/lines-list-data.json"));
        succeedingTestForElementsInfos(NETWORK_UUID, VARIANT_ID, ElementType.LINE, ElementInfos.InfoType.LIST, null, resourceToString("/lines-list-data.json"));

        succeedingTestForElementsInfos(NETWORK_UUID, null, ElementType.LINE, ElementInfos.InfoType.LIST, List.of("P3"), resourceToString("/partial-lines-list-data.json"));
        succeedingTestForElementsInfos(NETWORK_UUID, VARIANT_ID, ElementType.LINE, ElementInfos.InfoType.LIST, List.of("P3"), resourceToString("/partial-lines-list-data.json"));
    }

    @Test
    public void shouldReturnLinesIds() throws Exception {
        succeedingTestForList(ElementType.LINE.name(), NETWORK_UUID, null, null, true, List.of("NHV1_NHV2_1", "NHV1_NHV2_2", "LINE3").toString());
        succeedingTestForList(ElementType.LINE.name(), NETWORK_UUID, VARIANT_ID, null, true, List.of("NHV1_NHV2_1", "NHV1_NHV2_2", "LINE3").toString());
        succeedingTestForList(ElementType.LINE.name(), NETWORK_UUID, VARIANT_ID, List.of("P1"), true, List.of("NHV1_NHV2_1", "NHV1_NHV2_2", "LINE3").toString());
    }

    @Test
    public void shouldReturnAnErrorInsteadOfLinesMapData() throws Exception {
        failingTestForList("lines", NOT_FOUND_NETWORK_ID, null, null, false);
        failingTestForList("lines", NETWORK_UUID, VARIANT_ID_NOT_FOUND, null, false);
    }

    @Test
    public void shouldReturnAnErrorInsteadOfLinesIds() throws Exception {
        failingTestForList(ElementType.LINE.name(), NOT_FOUND_NETWORK_ID, null, null, true);
        failingTestForList(ElementType.LINE.name(), NETWORK_UUID, VARIANT_ID_NOT_FOUND, null, true);
    }

    @Test
    public void shouldReturnAnErrorInsteadOfLinesMapDataFromIds() throws Exception {
        failingTestForList("lines", NOT_FOUND_NETWORK_ID, null, List.of("P1"), false);
        failingTestForList("lines", NETWORK_UUID, VARIANT_ID_NOT_FOUND, List.of("P1"), false);
    }

    @Test
    public void shouldReturnLineFormData() throws Exception {
        succeedingTestForElementInfos(NETWORK_UUID, null, ElementType.LINE, ElementInfos.InfoType.FORM, "NHV1_NHV2_1", resourceToString("/line-form-data.json"));
        succeedingTestForElementInfos(NETWORK_UUID, VARIANT_ID, ElementType.LINE, ElementInfos.InfoType.FORM, "NHV1_NHV2_1", resourceToString("/line-form-data.json"));
    }

    @Test
    public void shouldReturnHvdcLineMapData() throws Exception {
        succeedingTestForElementInfos(NETWORK_UUID, null, ElementType.HVDC_LINE, ElementInfos.InfoType.MAP, "HVDC1", resourceToString("/hvdc-line-map-data.json"));
        succeedingTestForElementInfos(NETWORK_UUID, VARIANT_ID, ElementType.HVDC_LINE, ElementInfos.InfoType.MAP, "HVDC1", resourceToString("/hvdc-line-map-data.json"));
    }

    @Test
    public void shouldReturnAnErrorinsteadOfLineMapData() throws Exception {
        failingTestForElement("lines", NETWORK_UUID, null, null, "NOT_EXISTING_LINE");
        failingTestForElement("lines", NETWORK_UUID, VARIANT_ID, null, "NOT_EXISTING_LINE");
    }

    @Test
    public void shouldReturnGeneratorsIds() throws Exception {
        succeedingTestForList(ElementType.GENERATOR.toString(), NETWORK_UUID, null, null, true, List.of("GEN", "GEN2").toString());
        succeedingTestForList(ElementType.GENERATOR.toString(), NETWORK_UUID, VARIANT_ID, null, true, List.of("GEN", "GEN2").toString());
        succeedingTestForList(ElementType.GENERATOR.toString(), NETWORK_UUID, VARIANT_ID, List.of("P1"), true, List.of("GEN", "GEN2").toString());
    }

    @Test
    public void shouldReturnAnErrorInsteadOfGeneratorsMapData() throws Exception {
        failingTestForList("generators", NOT_FOUND_NETWORK_ID, null, null, false);
        failingTestForList("generators", NETWORK_UUID, VARIANT_ID_NOT_FOUND, null, false);
    }

    @Test
    public void shouldReturnAnErrorInsteadOfGeneratorsIds() throws Exception {
        failingTestForList(ElementType.GENERATOR.name(), NOT_FOUND_NETWORK_ID, null, null, true);
        failingTestForList(ElementType.GENERATOR.name(), NETWORK_UUID, VARIANT_ID_NOT_FOUND, null, true);
    }

    @Test
    public void shouldReturnAnErrorInsteadOfGeneratorsMapDataFromIds() throws Exception {
        failingTestForList("generators", NOT_FOUND_NETWORK_ID, null, List.of("P1"), false);
        failingTestForList("generators", NETWORK_UUID, VARIANT_ID_NOT_FOUND, List.of("P1"), false);
    }

    @Test
    public void shouldReturnAnErrorinsteadOfGeneratorMapData() throws Exception {
        failingTestForElement("generators", NETWORK_UUID, null, null, "NOT_EXISTING_GEN");
        failingTestForElement("generators", NETWORK_UUID, VARIANT_ID, null, "NOT_EXISTING_GEN");
    }

    @Test
    public void shouldReturnTwoWindingsTransformersIds() {
        succeedingTestForEquipmentsIds(NETWORK_UUID, null, ElementType.TWO_WINDINGS_TRANSFORMER, null, List.of("NGEN_NHV1", "NGEN_NHV2", "NHV2_NLOAD").toString());
        succeedingTestForEquipmentsIds(NETWORK_UUID, VARIANT_ID, ElementType.TWO_WINDINGS_TRANSFORMER, null, List.of("NGEN_NHV1", "NGEN_NHV2", "NHV2_NLOAD").toString());
        succeedingTestForEquipmentsIds(NETWORK_UUID, VARIANT_ID, ElementType.TWO_WINDINGS_TRANSFORMER, List.of("P1"), List.of("NGEN_NHV1", "NGEN_NHV2").toString());
    }

    @Test
    public void shouldReturnAnErrorInsteadOfTwoWindingsTransformersMapData() throws Exception {
        failingTestForList("2-windings-transformers", NOT_FOUND_NETWORK_ID, null, null, false);
        failingTestForList("2-windings-transformers", NETWORK_UUID, VARIANT_ID_NOT_FOUND, null, false);
    }

    @Test
    public void shouldReturnAnErrorInsteadOfTwoWindingsTransformersIds() throws Exception {
        failingTestForList(ElementType.TWO_WINDINGS_TRANSFORMER.name(), NOT_FOUND_NETWORK_ID, null, null, true);
        failingTestForList(ElementType.TWO_WINDINGS_TRANSFORMER.name(), NETWORK_UUID, VARIANT_ID_NOT_FOUND, null, true);
    }

    @Test
    public void shouldReturnAnErrorInsteadOfTwoWindingsTransformersMapDataFromIds() throws Exception {
        failingTestForList("2-windings-transformers", NOT_FOUND_NETWORK_ID, null, List.of("P1"), false);
        failingTestForList("2-windings-transformers", NETWORK_UUID, VARIANT_ID_NOT_FOUND, List.of("P1"), false);
    }

    @Test
    public void shouldReturnAnErrorinsteadOfTwoWindingsTransformerMapData() throws Exception {
        failingTestForElement("2-windings-transformers", NETWORK_UUID, null, null, "NOT_EXISTING_2WT");
        failingTestForElement("2-windings-transformers", NETWORK_UUID, VARIANT_ID, null, "NOT_EXISTING_2WT");
    }

    @Test
    public void shouldReturnThreeWindingsTransformersIds() {
        succeedingTestForEquipmentsIds(NETWORK_UUID, null, ElementType.THREE_WINDINGS_TRANSFORMER, null, List.of("TWT", "TWT21", "TWT32").toString());
        succeedingTestForEquipmentsIds(NETWORK_UUID, VARIANT_ID, ElementType.THREE_WINDINGS_TRANSFORMER, null, List.of("TWT", "TWT21", "TWT32").toString());
    }

    @Test
    public void shouldReturnAnErrorInsteadOfThreeWindingsTransformersMapData() throws Exception {
        failingTestForList("3-windings-transformers", NOT_FOUND_NETWORK_ID, null, null, false);
        failingTestForList("3-windings-transformers", NETWORK_UUID, VARIANT_ID_NOT_FOUND, null, false);
    }

    @Test
    public void shouldReturnAnErrorInsteadOfThreeWindingsTransformersIds() throws Exception {
        failingTestForList(ElementType.THREE_WINDINGS_TRANSFORMER.name(), NOT_FOUND_NETWORK_ID, null, null, true);
        failingTestForList(ElementType.THREE_WINDINGS_TRANSFORMER.name(), NETWORK_UUID, VARIANT_ID_NOT_FOUND, null, true);
    }

    @Test
    public void shouldReturnAnErrorInsteadOfThreeWindingsTransformersMapDataFromIds() throws Exception {
        failingTestForList("3-windings-transformers", NOT_FOUND_NETWORK_ID, null, List.of("P1"), false);
        failingTestForList("3-windings-transformers", NETWORK_UUID, VARIANT_ID_NOT_FOUND, List.of("P1"), false);
    }

    @Test
    public void shouldReturnAllMapData() throws Exception {
        succeedingTestForEquipmentsInfos(NETWORK_UUID, null, "all", null, resourceToString("/all-map-data.json"));
        succeedingTestForEquipmentsInfos(NETWORK_UUID, null, "all", List.of("P3"), resourceToString("/partial-all-map-data.json"));
        succeedingTestForEquipmentsInfos(NETWORK_UUID, VARIANT_ID, "all", null, resourceToString("/all-map-data-in-variant.json"));
        succeedingTestForEquipmentsInfos(NETWORK_UUID, VARIANT_ID, "all", List.of("P3"), resourceToString("/partial-all-map-data-in-variant.json"));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfAllMapData() throws Exception {
        failingTestForList("all", NOT_FOUND_NETWORK_ID, null, null, false);
        failingTestForList("all", NETWORK_UUID, VARIANT_ID_NOT_FOUND, null, false);
    }

    @Test
    public void shouldReturnAnErrorInsteadOfAllMapDataFromIds() throws Exception {
        failingTestForList("all", NOT_FOUND_NETWORK_ID, null, List.of("P1"), false);
        failingTestForList("all", NETWORK_UUID, VARIANT_ID_NOT_FOUND, List.of("P1"), false);
    }

    @Test
    public void shouldReturnBatteriesIds() throws Exception {
        succeedingTestForList(ElementType.BATTERY.name(), NETWORK_UUID, null, null, true, List.of("BATTERY1", "BATTERY2").toString());
        succeedingTestForList(ElementType.BATTERY.name(), NETWORK_UUID, VARIANT_ID, null, true, List.of("BATTERY1", "BATTERY2").toString());
        succeedingTestForList(ElementType.BATTERY.name(), NETWORK_UUID, VARIANT_ID, List.of("P1"), true, List.of("BATTERY1").toString());
    }

    @Test
    public void shouldReturnAnErrorInsteadOfBatteriesMapData() throws Exception {
        failingTestForList("batteries", NOT_FOUND_NETWORK_ID, null, null, false);
        failingTestForList("batteries", NETWORK_UUID, VARIANT_ID_NOT_FOUND, null, false);
    }

    @Test
    public void shouldReturnAnErrorInsteadOfBatteriesIds() throws Exception {
        failingTestForList(ElementType.BATTERY.name(), NOT_FOUND_NETWORK_ID, null, null, true);
        failingTestForList(ElementType.BATTERY.name(), NETWORK_UUID, VARIANT_ID_NOT_FOUND, null, true);
    }

    @Test
    public void shouldReturnAnErrorInsteadOfBatteriesMapDataFromIds() throws Exception {
        failingTestForList("batteries", NOT_FOUND_NETWORK_ID, null, List.of("P1", "P2"), false);
        failingTestForList("batteries", NETWORK_UUID, VARIANT_ID_NOT_FOUND, List.of("P1", "P2"), false);
    }

    @Test
    public void shouldReturnDanglingIds() throws Exception {
        succeedingTestForList(ElementType.DANGLING_LINE.name(), NETWORK_UUID, null, null, true, List.of("DL1", "DL2").toString());
        succeedingTestForList(ElementType.DANGLING_LINE.name(), NETWORK_UUID, VARIANT_ID, null, true, List.of("DL1", "DL2").toString());
        succeedingTestForList(ElementType.DANGLING_LINE.name(), NETWORK_UUID, VARIANT_ID, List.of("P1", "P3"), true, List.of("DL1", "DL2").toString());
    }

    @Test
    public void shouldReturnAnErrorInsteadOfDanglingLinesMapData() throws Exception {
        failingTestForList("dangling-lines", NOT_FOUND_NETWORK_ID, null, null, false);
        failingTestForList("dangling-lines", NETWORK_UUID, VARIANT_ID_NOT_FOUND, null, false);
    }

    @Test
    public void shouldReturnAnErrorInsteadOfDanglingLinesIds() throws Exception {
        failingTestForList(ElementType.DANGLING_LINE.name(), NOT_FOUND_NETWORK_ID, null, null, true);
        failingTestForList(ElementType.DANGLING_LINE.name(), NETWORK_UUID, VARIANT_ID_NOT_FOUND, null, true);
    }

    @Test
    public void shouldReturnAnErrorInsteadOfDanglingLinesMapDataFromIds() throws Exception {
        failingTestForList("dangling-lines", NOT_FOUND_NETWORK_ID, null, List.of("P1", "P2"), false);
        failingTestForList("dangling-lines", NETWORK_UUID, VARIANT_ID_NOT_FOUND, List.of("P1", "P2"), false);
    }

    @Test
    public void shouldReturnLoadsFormData() throws Exception {
        succeedingTestForElementsInfos(NETWORK_UUID, null, ElementType.LOAD, ElementInfos.InfoType.FORM, null, resourceToString("/loads-form-data.json"));
        succeedingTestForElementsInfos(NETWORK_UUID, VARIANT_ID, ElementType.LOAD, ElementInfos.InfoType.FORM, null, resourceToString("/loads-form-data.json"));
        succeedingTestForElementsInfos(NETWORK_UUID, null, ElementType.LOAD, ElementInfos.InfoType.FORM, List.of("P2"), resourceToString("/partial-loads-form-data.json"));
        succeedingTestForElementsInfos(NETWORK_UUID, VARIANT_ID, ElementType.LOAD, ElementInfos.InfoType.FORM, List.of("P2"), resourceToString("/partial-loads-form-data.json"));
    }

    @Test
    public void shouldReturnLoadsIds() throws Exception {
        succeedingTestForList(ElementType.LOAD.name(), NETWORK_UUID, null, null, true, List.of("LOAD", "LOAD_WITH_NULL_NAME", "LOAD_ID").toString());
        succeedingTestForList(ElementType.LOAD.name(), NETWORK_UUID, VARIANT_ID, null, true, List.of("LOAD", "LOAD_WITH_NULL_NAME", "LOAD_ID").toString());
        succeedingTestForList(ElementType.LOAD.name(), NETWORK_UUID, VARIANT_ID, List.of("P1", "P2"), true, List.of("LOAD", "LOAD_WITH_NULL_NAME", "LOAD_ID").toString());
    }

    @Test
    public void shouldReturnAnErrorInsteadOfLoadsMapData() throws Exception {
        failingTestForList("loads", NOT_FOUND_NETWORK_ID, null, null, false);
        failingTestForList("loads", NETWORK_UUID, VARIANT_ID_NOT_FOUND, null, false);
    }

    @Test
    public void shouldReturnAnErrorInsteadOfLoadsIds() throws Exception {
        failingTestForList(ElementType.LOAD.name(), NOT_FOUND_NETWORK_ID, null, null, true);
        failingTestForList(ElementType.LOAD.name(), NETWORK_UUID, VARIANT_ID_NOT_FOUND, null, true);
    }

    @Test
    public void shouldReturnAnErrorInsteadOfLoadsMapDataFromIds() throws Exception {
        failingTestForList("loads", NOT_FOUND_NETWORK_ID, null, List.of("P1"), false);
        failingTestForList("loads", NETWORK_UUID, VARIANT_ID_NOT_FOUND, List.of("P1"), false);
    }

    @Test
    public void shouldReturnLoadFormData() throws Exception {
        succeedingTestForElementInfos(NETWORK_UUID, null, ElementType.LOAD, ElementInfos.InfoType.FORM, "LOAD", resourceToString("/load-form-data.json"));
        succeedingTestForElementInfos(NETWORK_UUID, VARIANT_ID, ElementType.LOAD, ElementInfos.InfoType.FORM, "LOAD", resourceToString("/load-form-data.json"));
        succeedingTestForElementInfos(NETWORK_UUID, null, ElementType.LOAD, ElementInfos.InfoType.FORM, "LOAD_WITH_NULL_NAME", resourceToString("/load-form-data-test-null-name.json"));
        succeedingTestForElementInfos(NETWORK_UUID, null, ElementType.LOAD, ElementInfos.InfoType.FORM, "LOAD_ID", resourceToString("/load-form-data-not-null-name.json"));
    }

    @Test
    public void shouldReturnAnErrorinsteadOfLoadMapData() throws Exception {
        failingTestForElement("loads", NETWORK_UUID, null, null, "LOAD2ef");
        failingTestForElement("loads", NETWORK_UUID, VARIANT_ID, null, "LOAD2dqs");
    }

    @Test
    public void shouldReturnShuntCompensatorsIds() throws Exception {
        succeedingTestForList(ElementType.SHUNT_COMPENSATOR.name(), NETWORK_UUID, null, null, true, List.of("SHUNT1", "SHUNT2", "SHUNT_VLNB").toString());
        succeedingTestForList(ElementType.SHUNT_COMPENSATOR.name(), NETWORK_UUID, VARIANT_ID, null, true, List.of("SHUNT1", "SHUNT2", "SHUNT3", "SHUNT_VLNB").toString());
        succeedingTestForList(ElementType.SHUNT_COMPENSATOR.name(), NETWORK_UUID, VARIANT_ID, List.of("P1", "P2", "P3"), true, List.of("SHUNT1", "SHUNT2", "SHUNT3").toString());
    }

    @Test
    public void shouldReturnAnErrorInsteadOfShuntCompensatorsMapData() throws Exception {
        failingTestForList("shunt-compensators", NOT_FOUND_NETWORK_ID, null, null, false);
        failingTestForList("shunt-compensators", NETWORK_UUID, VARIANT_ID_NOT_FOUND, null, false);
    }

    @Test
    public void shouldReturnAnErrorInsteadOfShuntCompensatorsIds() throws Exception {
        failingTestForList(ElementType.SHUNT_COMPENSATOR.name(), NOT_FOUND_NETWORK_ID, null, null, true);
        failingTestForList(ElementType.SHUNT_COMPENSATOR.name(), NETWORK_UUID, VARIANT_ID_NOT_FOUND, null, true);
    }

    @Test
    public void shouldReturnAnErrorInsteadOfShuntCompensatorsMapDataFromIds() throws Exception {
        failingTestForList("shunt-compensators", NOT_FOUND_NETWORK_ID, null, List.of("P1"), false);
        failingTestForList("shunt-compensators", NETWORK_UUID, VARIANT_ID_NOT_FOUND, List.of("P1"), false);
    }

    @Test
    public void shouldReturnAnErrorinsteadOfShuntCompensatorMapData() throws Exception {
        failingTestForElement("shunt-compensators", NETWORK_UUID, null, null, "NOT_EXISTING_SC");
        failingTestForElement("shunt-compensators", NETWORK_UUID, VARIANT_ID, null, "NOT_EXISTING_SC");
    }

    @Test
    public void shouldReturnStaticVarCompensatorsIds() throws Exception {
        succeedingTestForList(ElementType.STATIC_VAR_COMPENSATOR.name(), NETWORK_UUID, null, null, true, List.of("SVC1", "SVC2").toString());
        succeedingTestForList(ElementType.STATIC_VAR_COMPENSATOR.name(), NETWORK_UUID, VARIANT_ID, null, true, List.of("SVC1", "SVC2").toString());
        succeedingTestForList(ElementType.STATIC_VAR_COMPENSATOR.name(), NETWORK_UUID, VARIANT_ID, List.of("P1", "P2"), true, List.of("SVC1", "SVC2").toString());
    }

    @Test
    public void shouldReturnAnErrorInsteadOfStaticVarCompensatorsMapData() throws Exception {
        failingTestForList("static-var-compensators", NOT_FOUND_NETWORK_ID, null, null, false);
        failingTestForList("static-var-compensators", NETWORK_UUID, VARIANT_ID_NOT_FOUND, null, false);
    }

    @Test
    public void shouldReturnAnErrorInsteadOfStaticVarCompensatorsIds() throws Exception {
        failingTestForList(ElementType.STATIC_VAR_COMPENSATOR.name(), NOT_FOUND_NETWORK_ID, null, null, true);
        failingTestForList(ElementType.STATIC_VAR_COMPENSATOR.name(), NETWORK_UUID, VARIANT_ID_NOT_FOUND, null, true);
    }

    @Test
    public void shouldReturnAnErrorInsteadOfStaticVarCompensatorsMapDataFromIds() throws Exception {
        failingTestForList("static-var-compensators", NOT_FOUND_NETWORK_ID, null, List.of("P1"), false);
        failingTestForList("static-var-compensators", NETWORK_UUID, VARIANT_ID_NOT_FOUND, List.of("P1"), false);
    }

    @Test
    public void shouldReturnLccConverterStationsIds() throws Exception {
        succeedingTestForList(ElementType.LCC_CONVERTER_STATION.name(), NETWORK_UUID, null, null, true, List.of("LCC1", "LCC2").toString());
        succeedingTestForList(ElementType.LCC_CONVERTER_STATION.name(), NETWORK_UUID, VARIANT_ID, null, true, List.of("LCC1", "LCC2").toString());
        succeedingTestForList(ElementType.LCC_CONVERTER_STATION.name(), NETWORK_UUID, VARIANT_ID, List.of("P1", "P2"), true, List.of("LCC1", "LCC2").toString());
    }

    @Test
    public void shouldReturnAnErrorInsteadOfLccConverterStationsMapData() throws Exception {
        failingTestForList("lcc-converter-stations", NOT_FOUND_NETWORK_ID, null, null, false);
        failingTestForList("lcc-converter-stations", NETWORK_UUID, VARIANT_ID_NOT_FOUND, null, false);
    }

    @Test
    public void shouldReturnAnErrorInsteadOfLccConverterStationsIds() throws Exception {
        failingTestForList(ElementType.LCC_CONVERTER_STATION.name(), NOT_FOUND_NETWORK_ID, null, null, true);
        failingTestForList(ElementType.LCC_CONVERTER_STATION.name(), NETWORK_UUID, VARIANT_ID_NOT_FOUND, null, true);
    }

    @Test
    public void shouldReturnAnErrorInsteadOfLccConverterStationsMapDataFromIds() throws Exception {
        failingTestForList("lcc-converter-stations", NOT_FOUND_NETWORK_ID, null, List.of("P1"), false);
        failingTestForList("lcc-converter-stations", NETWORK_UUID, VARIANT_ID_NOT_FOUND, List.of("P1"), false);
    }

    @Test
    public void shouldReturnVscConverterStationsIds() throws Exception {
        succeedingTestForList(ElementType.TWO_WINDINGS_TRANSFORMER.name(), NETWORK_UUID, null, null, true, List.of("NGEN_NHV1", "NGEN_NHV2", "NHV2_NLOAD").toString());
        succeedingTestForList(ElementType.TWO_WINDINGS_TRANSFORMER.name(), NETWORK_UUID, VARIANT_ID, null, true, List.of("NGEN_NHV1", "NGEN_NHV2", "NHV2_NLOAD").toString());
        succeedingTestForList(ElementType.TWO_WINDINGS_TRANSFORMER.name(), NETWORK_UUID, VARIANT_ID, List.of("P1", "P2", "P3", "P4"), true, List.of("NGEN_NHV1", "NGEN_NHV2", "NHV2_NLOAD").toString());
    }

    @Test
    public void shouldReturnAnErrorInsteadOfVscConverterStationsMapData() throws Exception {
        failingTestForList("vsc-converter-stations", NOT_FOUND_NETWORK_ID, null, null, false);
        failingTestForList("vsc-converter-stations", NETWORK_UUID, VARIANT_ID_NOT_FOUND, null, false);
    }

    @Test
    public void shouldReturnAnErrorInsteadOfVscConverterStationsIds() throws Exception {
        failingTestForList(ElementType.VSC_CONVERTER_STATION.name(), NOT_FOUND_NETWORK_ID, null, null, true);
        failingTestForList(ElementType.VSC_CONVERTER_STATION.name(), NETWORK_UUID, VARIANT_ID_NOT_FOUND, null, true);
    }

    @Test
    public void shouldReturnAnErrorInsteadOfVscConverterStationsMapDataFromIds() throws Exception {
        failingTestForList("vsc-converter-stations", NOT_FOUND_NETWORK_ID, null, List.of("P1"), false);
        failingTestForList("vsc-converter-stations", NETWORK_UUID, VARIANT_ID_NOT_FOUND, List.of("P1"), false);
    }

    @Test
    public void shouldReturnHvdcLinesTabData() throws Exception {
        succeedingTestForElementsInfos(NETWORK_UUID, null, ElementType.HVDC_LINE, ElementInfos.InfoType.TAB, null, resourceToString("/hvdc-lines-tab-data.json"));
        succeedingTestForElementsInfos(NETWORK_UUID, VARIANT_ID, ElementType.HVDC_LINE, ElementInfos.InfoType.TAB, null, resourceToString("/hvdc-lines-tab-data.json"));
    }

    @Test
    public void shouldReturnHvdcLinesIds() throws Exception {
        succeedingTestForList(ElementType.HVDC_LINE.name(), NETWORK_UUID, null, null, true, List.of("HVDC1", "HVDC3", "HVDC4", "HVDC2").toString());
        succeedingTestForList(ElementType.HVDC_LINE.name(), NETWORK_UUID, VARIANT_ID, null, true, List.of("HVDC1", "HVDC3", "HVDC4", "HVDC2").toString());
        succeedingTestForList(ElementType.HVDC_LINE.name(), NETWORK_UUID, VARIANT_ID, List.of("P1", "P3", "P4"), true, List.of("HVDC1", "HVDC3", "HVDC4").toString());
    }

    @Test
    public void shouldReturnAnErrorInsteadOfHvdcLinesMapData() throws Exception {
        failingTestForList("hvdc-lines", NOT_FOUND_NETWORK_ID, null, null, false);
        failingTestForList("hvdc-lines", NETWORK_UUID, VARIANT_ID_NOT_FOUND, null, false);
    }

    @Test
    public void shouldReturnAnErrorInsteadOfHvdcLinesIds() throws Exception {
        failingTestForList(ElementType.HVDC_LINE.name(), NOT_FOUND_NETWORK_ID, null, null, true);
        failingTestForList(ElementType.HVDC_LINE.name(), NETWORK_UUID, VARIANT_ID_NOT_FOUND, null, true);
    }

    @Test
    public void shouldReturnAnErrorInsteadOfHvdcLinesMapDataFromIds() throws Exception {
        failingTestForList("hvdc-lines", NOT_FOUND_NETWORK_ID, null, List.of("P1", "P2"), false);
        failingTestForList("hvdc-lines", NETWORK_UUID, VARIANT_ID_NOT_FOUND, List.of("P1", "P2"), false);
    }

    @Test
    public void shouldReturnVoltageLevelsFormData() throws Exception {
        succeedingTestForElementsInfos(NETWORK_UUID, null, ElementType.VOLTAGE_LEVEL, ElementInfos.InfoType.FORM, null, resourceToString("/voltage-levels-form-data.json"));
        succeedingTestForElementsInfos(NETWORK_UUID, VARIANT_ID, ElementType.VOLTAGE_LEVEL, ElementInfos.InfoType.FORM, null, resourceToString("/voltage-levels-form-data.json"));

        succeedingTestForElementsInfos(NETWORK_UUID, null, ElementType.VOLTAGE_LEVEL, ElementInfos.InfoType.FORM, List.of("P3"), resourceToString("/partial-voltage-levels-form-data.json"));
        succeedingTestForElementsInfos(NETWORK_UUID, VARIANT_ID, ElementType.VOLTAGE_LEVEL, ElementInfos.InfoType.FORM, List.of("P3"), resourceToString("/partial-voltage-levels-form-data.json"));
    }

    @Test
    public void shouldReturnVotlageLevelsListData() throws Exception {
        succeedingTestForElementsInfos(NETWORK_UUID, null, ElementType.VOLTAGE_LEVEL, ElementInfos.InfoType.LIST, null, resourceToString("/voltage-levels-list-data.json"));
        succeedingTestForElementsInfos(NETWORK_UUID, VARIANT_ID, ElementType.VOLTAGE_LEVEL, ElementInfos.InfoType.LIST, null, resourceToString("/voltage-levels-list-data.json"));

        succeedingTestForElementsInfos(NETWORK_UUID, null, ElementType.VOLTAGE_LEVEL, ElementInfos.InfoType.LIST, List.of("P3"), resourceToString("/partial-voltage-levels-list-data.json"));
        succeedingTestForElementsInfos(NETWORK_UUID, VARIANT_ID, ElementType.VOLTAGE_LEVEL, ElementInfos.InfoType.LIST, List.of("P3"), resourceToString("/partial-voltage-levels-list-data.json"));
    }

    @Test
    public void shouldReturnVoltageLevelsIds() throws Exception {
        succeedingVoltageLevelsTest(NETWORK_UUID, null, null, true, List.of("VLGEN", "VLHV1", "VLHV2", "VLLOAD", "VLNEW2", "VLGEN3", "VLGEN4", "VLGEN5").toString());
        succeedingVoltageLevelsTest(NETWORK_UUID, VARIANT_ID, null, true, List.of("VLGEN", "VLHV1", "VLHV2", "VLLOAD", "VLNEW2", "VLGEN3", "VLGEN4", "VLGEN5").toString());
        succeedingVoltageLevelsTest(NETWORK_UUID, VARIANT_ID, List.of("P1", "P2", "P3", "P4", "P5"), true, List.of("VLGEN", "VLHV1", "VLHV2", "VLLOAD", "VLNEW2", "VLGEN3", "VLGEN4", "VLGEN5").toString());
    }

    @Test
    public void shouldReturnAnErrorInsteadOfVoltageLevelsMapData() throws Exception {
        failingVoltageLevelsTest(NOT_FOUND_NETWORK_ID, null, null, false);
        failingVoltageLevelsTest(NETWORK_UUID, VARIANT_ID_NOT_FOUND, null, false);
    }

    @Test
    public void shouldReturnAnErrorInsteadOfVoltageLevelsIds() throws Exception {
        failingVoltageLevelsTest(NOT_FOUND_NETWORK_ID, null, null, true);
        failingVoltageLevelsTest(NETWORK_UUID, VARIANT_ID_NOT_FOUND, null, true);
    }

    @Test
    public void shouldReturnAnErrorInsteadOfVoltageLevelsMapDataFromIds() throws Exception {
        failingVoltageLevelsTest(NOT_FOUND_NETWORK_ID, null, List.of("P1", "P2"), false);
        failingVoltageLevelsTest(NETWORK_UUID, VARIANT_ID_NOT_FOUND, List.of("P1", "P2"), false);
    }

    @Test
    public void shouldReturnVotlageLevelFormData() throws Exception {
        succeedingTestForElementInfos(NETWORK_UUID, null, ElementType.VOLTAGE_LEVEL, ElementInfos.InfoType.FORM, "VLGEN4", resourceToString("/voltage-level-form-data.json"));
        succeedingTestForElementInfos(NETWORK_UUID, VARIANT_ID, ElementType.VOLTAGE_LEVEL, ElementInfos.InfoType.FORM, "VLGEN4", resourceToString("/voltage-level-form-data.json"));
    }

    @Test
    public void shouldReturnAnErrorinsteadOfVotlageLevelMapData() throws Exception {
        failingTestForElement("voltage-levels", NETWORK_UUID, null, null, "NOT_EXISTING_VL");
        failingTestForElement("voltage-levels", NETWORK_UUID, VARIANT_ID, null, "NOT_EXISTING_VL");
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

    @Test
    public void shouldReturnVoltageLevelsAndEquipments() throws Exception {
        succeedingTestForEquipmentsInfos(NETWORK_UUID, null, "voltage-levels-equipments", null, resourceToString("/voltage-levels-equipments-map-data.json"));
        succeedingTestForEquipmentsInfos(NETWORK_UUID, VARIANT_ID, "voltage-levels-equipments", null, resourceToString("/voltage-levels-equipments-map-data-in-variant.json"));
    }

    @Test
    public void shouldReturnVoltageLevelEquipments() throws Exception {
        succeedingTestForList("voltage-level-equipments/VLGEN", NETWORK_UUID, null, null, false, resourceToString("/voltage-level-VLGEN-equipments.json"));
        succeedingTestForList("voltage-level-equipments/VLGEN", NETWORK_UUID, VARIANT_ID, null, false, resourceToString("/voltage-level-VLGEN-equipments.json"));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfVoltageLevelsEquipmentsMapData() throws Exception {
        failingTestForList("voltage-levels-equipments", NOT_FOUND_NETWORK_ID, null, null, false);
        failingTestForList("voltage-levels-equipments", NETWORK_UUID, VARIANT_ID_NOT_FOUND, null, false);
    }

    @Test
    public void shouldReturnSubstationsMapData() throws Exception {
        succeedingTestForElementsInfos(NETWORK_UUID, null, ElementType.SUBSTATION, ElementInfos.InfoType.MAP, null, resourceToString("/substations-map-data.json"));
        succeedingTestForElementsInfos(NETWORK_UUID, VARIANT_ID, ElementType.SUBSTATION, ElementInfos.InfoType.MAP, null, resourceToString("/substations-map-data.json"));

        succeedingTestForElementsInfos(NETWORK_UUID, null, ElementType.SUBSTATION, ElementInfos.InfoType.MAP, List.of("P1"), resourceToString("/partial-substations-map-data.json"));
        succeedingTestForElementsInfos(NETWORK_UUID, VARIANT_ID, ElementType.SUBSTATION, ElementInfos.InfoType.MAP, List.of("P1"), resourceToString("/partial-substations-map-data.json"));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfMapSubstationsData() throws Exception {
        failingTestForList("map-substations", NOT_FOUND_NETWORK_ID, null, null, false);
        failingTestForList("map-substations", NETWORK_UUID, VARIANT_ID_NOT_FOUND, null, false);
    }

    @Test
    public void shouldReturnAnErrorInsteadOfMapSubstationsDataFromSubstationId() throws Exception {
        failingTestForList("map-substations", NOT_FOUND_NETWORK_ID, null, List.of("P1"), false);
        failingTestForList("map-substations", NETWORK_UUID, VARIANT_ID_NOT_FOUND, List.of("P1"), false);
    }

    @Test
    public void shouldReturnLinesMapData() throws Exception {
        succeedingTestForElementsInfos(NETWORK_UUID, null, ElementType.LINE, ElementInfos.InfoType.MAP, null, resourceToString("/lines-map-data.json"));
        succeedingTestForElementsInfos(NETWORK_UUID, VARIANT_ID, ElementType.LINE, ElementInfos.InfoType.MAP, null, resourceToString("/lines-map-data.json"));

        succeedingTestForElementsInfos(NETWORK_UUID, null, ElementType.LINE, ElementInfos.InfoType.MAP, List.of("P1"), resourceToString("/partial-lines-map-data.json"));
        succeedingTestForElementsInfos(NETWORK_UUID, VARIANT_ID, ElementType.LINE, ElementInfos.InfoType.MAP, List.of("P1"), resourceToString("/partial-lines-map-data.json"));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfMapLinesData() throws Exception {
        failingTestForList("map-lines", NOT_FOUND_NETWORK_ID, null, null, false);
        failingTestForList("map-lines", NETWORK_UUID, VARIANT_ID_NOT_FOUND, null, false);
    }

    @Test
    public void shouldReturnAnErrorInsteadOfMapLinesDataFromSubstationId() throws Exception {
        failingTestForList("map-lines", NOT_FOUND_NETWORK_ID, null, List.of("P1"), false);
        failingTestForList("map-lines", NETWORK_UUID, VARIANT_ID_NOT_FOUND, List.of("P1"), false);
    }

    @Test
    public void shouldReturnHvdcLinesMapData() throws Exception {
        succeedingTestForElementsInfos(NETWORK_UUID, null, ElementType.HVDC_LINE, ElementInfos.InfoType.MAP, null, resourceToString("/hvdc-lines-map-data.json"));
        succeedingTestForElementsInfos(NETWORK_UUID, VARIANT_ID, ElementType.HVDC_LINE, ElementInfos.InfoType.MAP, null, resourceToString("/hvdc-lines-map-data.json"));

        succeedingTestForElementsInfos(NETWORK_UUID, null, ElementType.HVDC_LINE, ElementInfos.InfoType.MAP, List.of("P2"), "[]");
        succeedingTestForElementsInfos(NETWORK_UUID, VARIANT_ID, ElementType.HVDC_LINE, ElementInfos.InfoType.MAP, List.of("P2"), "[]");

        succeedingTestForElementsInfos(NETWORK_UUID, null, ElementType.HVDC_LINE, ElementInfos.InfoType.MAP, List.of("P1"), resourceToString("/partial-map-hvdc-lines-data.json"));
        succeedingTestForElementsInfos(NETWORK_UUID, VARIANT_ID, ElementType.HVDC_LINE, ElementInfos.InfoType.MAP, List.of("P1"), resourceToString("/partial-map-hvdc-lines-data.json"));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfMapHvdcLinesData() throws Exception {
        failingTestForList("map-hvdc-lines", NOT_FOUND_NETWORK_ID, null, null, false);
        failingTestForList("map-hvdc-lines", NETWORK_UUID, VARIANT_ID_NOT_FOUND, null, false);
    }

    @Test
    public void shouldReturnAnErrorInsteadOfMapHvdcLinesDataFromSubstationId() throws Exception {
        failingTestForList("map-hvdc-lines", NOT_FOUND_NETWORK_ID, null, List.of("P1"), false);
        failingTestForList("map-hvdc-lines", NETWORK_UUID, VARIANT_ID_NOT_FOUND, List.of("P1"), false);
    }

    public void shouldReturnLineWhenGetBranchOrThreeWindingsTransformer() throws Exception {
        succeedingTestForElement("branch-or-3wt", NETWORK_UUID, null, null, "NHV1_NHV2_1", resourceToString("/line-map-data-with-vl.json"));
        succeedingTestForElement("branch-or-3wt", NETWORK_UUID, VARIANT_ID, null, "NHV1_NHV2_1", resourceToString("/line-map-data-with-vl.json"));
    }

    @Test
    public void shouldReturn2WTWhenGetBranchOrThreeWindingsTransformer() throws Exception {
        succeedingTestForElement("branch-or-3wt", NETWORK_UUID, null, null, "NGEN_NHV1", resourceToString("/2-windings-transformer-list-data.json"));
        succeedingTestForElement("branch-or-3wt", NETWORK_UUID, VARIANT_ID, null, "NGEN_NHV1", resourceToString("/2-windings-transformer-list-data.json"));
    }

    @Test
    public void shouldReturn3WTWhenGetBranchOrThreeWindingsTransformer() throws Exception {
        succeedingTestForElement("branch-or-3wt", NETWORK_UUID, null, null, "TWT", resourceToString("/3-windings-transformer-list-data.json"));
        succeedingTestForElement("branch-or-3wt", NETWORK_UUID, VARIANT_ID, null, "TWT", resourceToString("/3-windings-transformer-list-data.json"));
    }

    @Test
    public void shoulNotExistElementWhenGetBranchOrThreeWindingsTransformer() throws Exception {
        shouldNotExistElement("branch-or-3wt", NETWORK_UUID, null, null, "NOT_EXISTING_EQUIPMENT");
        shouldNotExistElement("branch-or-3wt", NETWORK_UUID, VARIANT_ID, null, "NOT_EXISTING_EQUIPMENT");
    }

    @Test
    public void shouldReturnGeneratorsFormData() throws Exception {
        succeedingTestForElementInfos(NETWORK_UUID, null, ElementType.GENERATOR, ElementInfos.InfoType.FORM, "GEN", resourceToString("/generator-map-data.json"));
        succeedingTestForElementInfos(NETWORK_UUID, VARIANT_ID, ElementType.GENERATOR, ElementInfos.InfoType.FORM, "GEN", resourceToString("/generator-map-data.json"));
    }

    @Test
    public void shouldReturnGeneratorsTabData() throws Exception {
        succeedingTestForElementsInfos(NETWORK_UUID, null, ElementType.GENERATOR, ElementInfos.InfoType.TAB, null, resourceToString("/generators-map-data.json"));
        succeedingTestForElementsInfos(NETWORK_UUID, VARIANT_ID, ElementType.GENERATOR, ElementInfos.InfoType.TAB, null, resourceToString("/generators-map-data.json"));
    }

    @Test
    public void shouldReturnBatteryTabData() throws Exception {
        succeedingTestForElementsInfos(NETWORK_UUID, null, ElementType.BATTERY, ElementInfos.InfoType.TAB, null, resourceToString("/batteries-map-data.json"));
        succeedingTestForElementsInfos(NETWORK_UUID, VARIANT_ID, ElementType.BATTERY, ElementInfos.InfoType.TAB, null, resourceToString("/batteries-map-data.json"));
        succeedingTestForElementsInfos(NETWORK_UUID, null, ElementType.BATTERY, ElementInfos.InfoType.TAB, List.of("P1"), resourceToString("/partial-batteries-map-data.json"));
        succeedingTestForElementsInfos(NETWORK_UUID, VARIANT_ID, ElementType.BATTERY, ElementInfos.InfoType.TAB, List.of("P1"), resourceToString("/partial-batteries-map-data.json"));
    }

    @Test
    public void shouldReturnShuntCompensatorTabData() throws Exception {
        succeedingTestForElementsInfos(NETWORK_UUID, null, ElementType.SHUNT_COMPENSATOR, ElementInfos.InfoType.TAB, null, resourceToString("/shunt-compensators-map-data.json"));
        succeedingTestForElementsInfos(NETWORK_UUID, VARIANT_ID, ElementType.SHUNT_COMPENSATOR, ElementInfos.InfoType.TAB, null, resourceToString("/shunt-compensators-map-data-in-variant.json"));
    }

    @Test
    public void shouldReturnShuntCompensatorFormData() throws Exception {
        succeedingTestForElementInfos(NETWORK_UUID, null, ElementType.SHUNT_COMPENSATOR, ElementInfos.InfoType.FORM, "SHUNT_VLNB", resourceToString("/shunt-compensator-map-data.json"));
        succeedingTestForElementInfos(NETWORK_UUID, VARIANT_ID, ElementType.SHUNT_COMPENSATOR, ElementInfos.InfoType.FORM, "SHUNT_VLNB", resourceToString("/shunt-compensator-map-data-in-variant.json"));
    }

    @Test
    public void shouldReturnDanglingLineTabData() throws Exception {
        succeedingTestForElementsInfos(NETWORK_UUID, null, ElementType.DANGLING_LINE, ElementInfos.InfoType.TAB, null, resourceToString("/dangling-lines-map-data.json"));
        succeedingTestForElementsInfos(NETWORK_UUID, VARIANT_ID, ElementType.DANGLING_LINE, ElementInfos.InfoType.TAB, null, resourceToString("/dangling-lines-map-data.json"));
    }

    @Test
    public void shouldReturnStaticVarCompensatorTabData() throws Exception {
        succeedingTestForElementsInfos(NETWORK_UUID, null, ElementType.STATIC_VAR_COMPENSATOR, ElementInfos.InfoType.TAB, null, resourceToString("/static-var-compensators-map-data.json"));
        succeedingTestForElementsInfos(NETWORK_UUID, VARIANT_ID, ElementType.STATIC_VAR_COMPENSATOR, ElementInfos.InfoType.TAB, null, resourceToString("/static-var-compensators-map-data.json"));
    }

    @Test
    public void shouldReturnLccConverterStationTabData() throws Exception {
        succeedingTestForElementsInfos(NETWORK_UUID, null, ElementType.LCC_CONVERTER_STATION, ElementInfos.InfoType.TAB, null, resourceToString("/lcc-converter-stations-map-data.json"));
        succeedingTestForElementsInfos(NETWORK_UUID, VARIANT_ID, ElementType.LCC_CONVERTER_STATION, ElementInfos.InfoType.TAB, null, resourceToString("/lcc-converter-stations-map-data.json"));
    }

    @Test
    public void shouldReturnVscConverterStationTabData() throws Exception {
        succeedingTestForElementsInfos(NETWORK_UUID, null, ElementType.VSC_CONVERTER_STATION, ElementInfos.InfoType.TAB, null, resourceToString("/vsc-converter-stations-map-data.json"));
        succeedingTestForElementsInfos(NETWORK_UUID, VARIANT_ID, ElementType.VSC_CONVERTER_STATION, ElementInfos.InfoType.TAB, null, resourceToString("/vsc-converter-stations-map-data.json"));
    }

    @Test
    public void shouldReturnTwoWindingsTransformerTabData() throws Exception {
        succeedingTestForElementsInfos(NETWORK_UUID, null, ElementType.TWO_WINDINGS_TRANSFORMER, ElementInfos.InfoType.TAB, null, resourceToString("/2-windings-transformers-map-data.json"));
        succeedingTestForElementsInfos(NETWORK_UUID, VARIANT_ID, ElementType.TWO_WINDINGS_TRANSFORMER, ElementInfos.InfoType.TAB, null, resourceToString("/2-windings-transformers-map-data.json"));
    }

    @Test
    public void shouldReturnTwoWindingsTransformerFormData() throws Exception {
        succeedingTestForElementInfos(NETWORK_UUID, null, ElementType.TWO_WINDINGS_TRANSFORMER, ElementInfos.InfoType.FORM, "NGEN_NHV1", resourceToString("/2-windings-transformer-map-data.json"));
        succeedingTestForElementInfos(NETWORK_UUID, VARIANT_ID, ElementType.TWO_WINDINGS_TRANSFORMER, ElementInfos.InfoType.FORM, "NGEN_NHV1", resourceToString("/2-windings-transformer-map-data.json"));
    }

    @Test
    public void shouldReturnThreeWindingsTransformerTabData() throws Exception {
        succeedingTestForElementsInfos(NETWORK_UUID, null, ElementType.THREE_WINDINGS_TRANSFORMER, ElementInfos.InfoType.TAB, null, resourceToString("/3-windings-transformers-map-data.json"));
        succeedingTestForElementsInfos(NETWORK_UUID, VARIANT_ID, ElementType.THREE_WINDINGS_TRANSFORMER, ElementInfos.InfoType.TAB, null, resourceToString("/3-windings-transformers-map-data.json"));
        succeedingTestForElementsInfos(NETWORK_UUID, null, ElementType.THREE_WINDINGS_TRANSFORMER, ElementInfos.InfoType.TAB, List.of("P3"), "[]");
        succeedingTestForElementsInfos(NETWORK_UUID, VARIANT_ID, ElementType.THREE_WINDINGS_TRANSFORMER, ElementInfos.InfoType.TAB, List.of("P3"), "[]");
    }
}
