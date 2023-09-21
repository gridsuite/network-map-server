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
import org.gridsuite.network.map.dto.ElementInfos.InfoType;
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
    public static final String QUERY_PARAM_DC_POWER_FACTOR = "dcPowerFactor";

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
        gen.newExtension(GeneratorStartupAdder.class)
                .withPlannedActivePowerSetpoint(0.3)
                .withMarginalCost(3)
                .withPlannedOutageRate(0.4)
                .withForcedOutageRate(2)
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
        line3.getTerminal1().setP(200.);
        line3.getTerminal2().setP(100.);

        Substation p6 = network.newSubstation()
                .setId("P6")
                .setCountry(Country.FR)
                .setTso("RTE")
                .setGeographicalTags("A")
                .add();
        VoltageLevel vlgen6 = p6.newVoltageLevel()
                .setId("VLGEN6")
                .setNominalV(24.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vlgen6.getBusBreakerView().newBus()
                .setId("NGEN6")
                .add();
        network.newLine()
                .setId("LINE4")
                .setVoltageLevel1("VLGEN6")
                .setBus1("NGEN6")
                .setConnectableBus1("NGEN6")
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

        Battery b1 = vlnew2.newBattery()
                .setId("BATTERY1")
                .setName("BATTERY1")
                .setMinP(50)
                .setMaxP(70)
                .setTargetP(1)
                .setTargetQ(1)
                .setConnectableBus("NNEW2")
                .add();
        b1.newExtension(ConnectablePositionAdder.class)
                .newFeeder()
                .withName("feederName")
                .withOrder(0)
                .withDirection(ConnectablePosition.Direction.TOP).add()
                .add();
        b1.newMinMaxReactiveLimits().setMinQ(-500)
                .setMaxQ(500)
                .add();

        Battery b2 = vlgen3.newBattery()
                .setId("BATTERY2")
                .setName("BATTERY2")
                .setMinP(50)
                .setMaxP(70)
                .setTargetP(1)
                .setTargetQ(1)
                .setConnectableBus("NNEW2")
                .add();
        b2.newExtension(ConnectablePositionAdder.class)
                .newFeeder()
                .withName("feederName")
                .withOrder(0)
                .withDirection(ConnectablePosition.Direction.TOP).add()
                .add();
        b2.newExtension(ActivePowerControlAdder.class).withParticipate(true).withDroop(3).add();
        b2.newReactiveCapabilityCurve().beginPoint()
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
                .setConverterStationId1("LCC1")
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
                .setConverterStationId1("LCC1")
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

    private void succeedingTestForElement(String equipments, UUID networkUuid, String variantId, List<String> substationsIds, String elementId, String expectedJson) throws Exception {
        MvcResult res = mvc.perform(get(buildUrlForElement(equipments, variantId, substationsIds), networkUuid, elementId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn();

        JSONAssert.assertEquals(expectedJson, res.getResponse().getContentAsString(), JSONCompareMode.NON_EXTENSIBLE);
    }

    @SneakyThrows
    private void succeedingTestForElementInfos(UUID networkUuid, String variantId, ElementType elementType, InfoType infoType, String elementId, String expectedJson) {
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
    private void succeedingTestForElementInfosInDc(UUID networkUuid, String variantId, ElementType elementType, InfoType infoType, String elementId, double dcPowerFactor, String expectedJson) {
        MvcResult mvcResult = mvc.perform(get("/v1/networks/{networkUuid}/elements/{elementId}", networkUuid, elementId)
                .queryParam(QUERY_PARAM_VARIANT_ID, variantId)
                .queryParam(QUERY_PARAM_ELEMENT_TYPE, elementType.name())
                .queryParam(QUERY_PARAM_INFO_TYPE, infoType.name())
                .queryParam(QUERY_PARAM_DC_POWER_FACTOR, Double.toString(dcPowerFactor))
            )
            .andExpect(status().isOk())
            .andReturn();

        JSONAssert.assertEquals(expectedJson, mvcResult.getResponse().getContentAsString(), JSONCompareMode.NON_EXTENSIBLE);
    }

    @SneakyThrows
    private void notFoundTestForElementInfos(UUID networkUuid, String variantId, ElementType elementType, InfoType infoType, String elementId) {
        mvc.perform(get("/v1/networks/{networkUuid}/elements/{elementId}", networkUuid, elementId)
                        .queryParam(QUERY_PARAM_VARIANT_ID, variantId)
                        .queryParam(QUERY_PARAM_ELEMENT_TYPE, elementType.name())
                        .queryParam(QUERY_PARAM_INFO_TYPE, infoType.name())
                )
                .andExpect(status().isNotFound());
    }

    @SneakyThrows
    private void succeedingTestForElementsInfos(UUID networkUuid, String variantId, ElementType elementType, InfoType infoType, List<String> substationsIds, String expectedJson) {
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
    private void notFoundTestForElementsInfos(UUID networkUuid, String variantId, ElementType elementType, InfoType infoType, List<String> substationsIds) {
        LinkedMultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.add(QUERY_PARAM_VARIANT_ID, variantId);
        queryParams.add(QUERY_PARAM_ELEMENT_TYPE, elementType.name());
        queryParams.add(QUERY_PARAM_INFO_TYPE, infoType.name());
        if (!substationsIds.isEmpty()) {
            queryParams.addAll(QUERY_PARAM_SUBSTATIONS_IDS, substationsIds);
        }
        mvc.perform(get("/v1/networks/{networkUuid}/elements", networkUuid).queryParams(queryParams))
                .andExpect(status().isNotFound());
    }

    @SneakyThrows
    private void succeedingTestForElementsIds(UUID networkUuid, String variantId, ElementType elementType, List<String> substationsIds, String expectedJson) {
        LinkedMultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.add(QUERY_PARAM_VARIANT_ID, variantId);
        queryParams.add(QUERY_PARAM_ELEMENT_TYPE, elementType.name());
        if (!substationsIds.isEmpty()) {
            queryParams.addAll(QUERY_PARAM_SUBSTATIONS_IDS, substationsIds);
        }
        MvcResult mvcResult = mvc.perform(get("/v1/networks/{networkUuid}/elements-ids", networkUuid).queryParams(queryParams))
                .andExpect(status().isOk())
                .andReturn();

        JSONAssert.assertEquals(expectedJson, mvcResult.getResponse().getContentAsString(), JSONCompareMode.NON_EXTENSIBLE);
    }

    @SneakyThrows
    private void notFoundTestForElementsIds(UUID networkUuid, String variantId, ElementType elementType, List<String> substationsIds) {
        LinkedMultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.add(QUERY_PARAM_VARIANT_ID, variantId);
        queryParams.add(QUERY_PARAM_ELEMENT_TYPE, elementType.name());
        if (!substationsIds.isEmpty()) {
            queryParams.addAll(QUERY_PARAM_SUBSTATIONS_IDS, substationsIds);
        }
        mvc.perform(get("/v1/networks/{networkUuid}/elements-ids", networkUuid).queryParams(queryParams))
                .andExpect(status().isNotFound());
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

    @SneakyThrows
    private void notFoundTestForEquipmentsInfos(UUID networkUuid, String variantId, String infosPath, List<String> substationsIds) {
        LinkedMultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.add(QUERY_PARAM_VARIANT_ID, variantId);
        if (!substationsIds.isEmpty()) {
            queryParams.addAll(QUERY_PARAM_SUBSTATION_ID, substationsIds);
        }
        mvc.perform(get("/v1/networks/{networkUuid}/{infosPath}", networkUuid, infosPath).queryParams(queryParams))
                .andExpect(status().isNotFound());
    }

    private void shouldNotExistElement(String equipments, UUID networkUuid, String variantId, List<String> substationsIds, String elementId) throws Exception {
        mvc.perform(get(buildUrlForElement(equipments, variantId, substationsIds), networkUuid, elementId))
                .andExpect(status().isNoContent());
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

    private String buildUrlHvdcWithShuntCompensators(String variantId) {
        StringBuffer url = new StringBuffer("/v1/networks/{networkUuid}/hvdc-lines/{hvdcId}/shunt-compensators");
        if (variantId != null) {
            url.append("?variantId=" + variantId);
        }
        return url.toString();
    }

    private void succeedingHvdcWithShuntCompensatorsTest(UUID networkUuid, String hvdcId, String variantId, String expectedJson) throws Exception {
        MvcResult res = mvc.perform(get(buildUrlHvdcWithShuntCompensators(variantId), networkUuid, hvdcId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn();
        JSONAssert.assertEquals(res.getResponse().getContentAsString(), expectedJson, JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    public void shouldReturnSubstationsTabData() throws Exception {
        succeedingTestForElementsInfos(NETWORK_UUID, null, ElementType.SUBSTATION, InfoType.TAB, null, resourceToString("/substations-tab-data.json"));
        succeedingTestForElementsInfos(NETWORK_UUID, VARIANT_ID, ElementType.SUBSTATION, InfoType.TAB, null, resourceToString("/substations-tab-data.json"));
    }

    @Test
    public void shouldReturnSubstationsIds() {
        succeedingTestForElementsIds(NETWORK_UUID, null, ElementType.SUBSTATION, List.of(), List.of("P1", "P2", "P3", "P4", "P5", "P6").toString());
        succeedingTestForElementsIds(NETWORK_UUID, VARIANT_ID, ElementType.SUBSTATION, List.of(), List.of("P1", "P2", "P3", "P4", "P5", "P6").toString());
    }

    @Test
    public void shouldReturnNotFoundInsteadOfSubstationsMapData() {
        notFoundTestForElementsInfos(NOT_FOUND_NETWORK_ID, null, ElementType.SUBSTATION, InfoType.LIST, List.of());
        notFoundTestForElementsInfos(NETWORK_UUID, VARIANT_ID_NOT_FOUND, ElementType.SUBSTATION, InfoType.LIST, List.of());
        notFoundTestForElementsInfos(NOT_FOUND_NETWORK_ID, null, ElementType.SUBSTATION, InfoType.LIST, List.of("P1", "P2"));
        notFoundTestForElementsInfos(NETWORK_UUID, VARIANT_ID_NOT_FOUND, ElementType.SUBSTATION, InfoType.LIST, List.of("P1", "P2"));
    }

    @Test
    public void shouldReturnSubstationMapData() throws Exception {
        succeedingTestForElementInfos(NETWORK_UUID, null, ElementType.SUBSTATION, InfoType.MAP, "P4", resourceToString("/substation-map-data.json"));
        succeedingTestForElementInfos(NETWORK_UUID, VARIANT_ID, ElementType.SUBSTATION, InfoType.MAP, "P4", resourceToString("/substation-map-data.json"));
    }

    @Test
    public void shouldReturnNotFoundInsteadOfSubstationMapData() {
        notFoundTestForElementInfos(NETWORK_UUID, null, ElementType.SUBSTATION, InfoType.LIST, "NOT_EXISTING_SUBSTATION");
        notFoundTestForElementInfos(NETWORK_UUID, VARIANT_ID, ElementType.SUBSTATION, InfoType.LIST, "NOT_EXISTING_SUBSTATION");
    }

    @Test
    public void shouldReturnLinesFormData() throws Exception {
        succeedingTestForElementsInfos(NETWORK_UUID, null, ElementType.LINE, InfoType.FORM, null, resourceToString("/lines-form-data.json"));
        succeedingTestForElementsInfos(NETWORK_UUID, VARIANT_ID, ElementType.LINE, InfoType.FORM, null, resourceToString("/lines-form-data.json"));

        succeedingTestForElementsInfos(NETWORK_UUID, null, ElementType.LINE, InfoType.FORM, List.of("P3"), resourceToString("/partial-lines-form-data.json"));
        succeedingTestForElementsInfos(NETWORK_UUID, VARIANT_ID, ElementType.LINE, InfoType.FORM, List.of("P3"), resourceToString("/partial-lines-form-data.json"));
    }

    @Test
    public void shouldReturnLinesListData() throws Exception {
        succeedingTestForElementsInfos(NETWORK_UUID, null, ElementType.LINE, InfoType.LIST, null, resourceToString("/lines-list-data.json"));
        succeedingTestForElementsInfos(NETWORK_UUID, VARIANT_ID, ElementType.LINE, InfoType.LIST, null, resourceToString("/lines-list-data.json"));

        succeedingTestForElementsInfos(NETWORK_UUID, null, ElementType.LINE, InfoType.LIST, List.of("P3"), resourceToString("/partial-lines-list-data.json"));
        succeedingTestForElementsInfos(NETWORK_UUID, VARIANT_ID, ElementType.LINE, InfoType.LIST, List.of("P3"), resourceToString("/partial-lines-list-data.json"));
    }

    @Test
    public void shouldReturnLinesIds() {
        succeedingTestForElementsIds(NETWORK_UUID, null, ElementType.LINE, List.of(), List.of("NHV1_NHV2_1", "NHV1_NHV2_2", "LINE3", "LINE4").toString());
        succeedingTestForElementsIds(NETWORK_UUID, VARIANT_ID, ElementType.LINE, List.of(), List.of("NHV1_NHV2_1", "NHV1_NHV2_2", "LINE3", "LINE4").toString());
        succeedingTestForElementsIds(NETWORK_UUID, VARIANT_ID, ElementType.LINE, List.of("P1"), List.of("NHV1_NHV2_1", "NHV1_NHV2_2", "LINE3").toString());
    }

    @Test
    public void shouldReturnNotFoundInsteadOfLinesMapData() {
        notFoundTestForElementsInfos(NOT_FOUND_NETWORK_ID, null, ElementType.LINE, InfoType.LIST, List.of());
        notFoundTestForElementsInfos(NETWORK_UUID, VARIANT_ID_NOT_FOUND, ElementType.LINE, InfoType.LIST, List.of());
        notFoundTestForElementsInfos(NOT_FOUND_NETWORK_ID, null, ElementType.LINE, InfoType.LIST, List.of("P1", "P2"));
        notFoundTestForElementsInfos(NETWORK_UUID, VARIANT_ID_NOT_FOUND, ElementType.LINE, InfoType.LIST, List.of("P1", "P2"));
    }

    @Test
    public void shouldReturnNotFoundInsteadOfLinesIds() {
        notFoundTestForElementsIds(NOT_FOUND_NETWORK_ID, null, ElementType.LINE, List.of());
        notFoundTestForElementsIds(NETWORK_UUID, VARIANT_ID_NOT_FOUND, ElementType.LINE, List.of());
    }

    @Test
    public void shouldReturnLineFormData() throws Exception {
        succeedingTestForElementInfos(NETWORK_UUID, null, ElementType.LINE, InfoType.FORM, "NHV1_NHV2_1", resourceToString("/line-form-data.json"));
        succeedingTestForElementInfos(NETWORK_UUID, VARIANT_ID, ElementType.LINE, InfoType.FORM, "NHV1_NHV2_1", resourceToString("/line-form-data.json"));
    }

    @Test
    public void shouldReturnLineTooltipData() throws Exception {
        succeedingTestForElementInfos(NETWORK_UUID, null, ElementType.LINE, InfoType.TOOLTIP, "NHV1_NHV2_1", resourceToString("/line-tooltip-data.json"));
        succeedingTestForElementInfos(NETWORK_UUID, VARIANT_ID, ElementType.LINE, InfoType.TOOLTIP, "NHV1_NHV2_1", resourceToString("/line-tooltip-data.json"));

        succeedingTestForElementInfosInDc(NETWORK_UUID, VARIANT_ID, ElementType.LINE, InfoType.TOOLTIP, "LINE3", 0.80, resourceToString("/line-tooltip-data-dc.json"));
    }

    @Test
    public void shouldReturnHvdcLineMapData() throws Exception {
        succeedingTestForElementInfos(NETWORK_UUID, null, ElementType.HVDC_LINE, InfoType.MAP, "HVDC1", resourceToString("/hvdc-line-map-data.json"));
        succeedingTestForElementInfos(NETWORK_UUID, VARIANT_ID, ElementType.HVDC_LINE, InfoType.MAP, "HVDC1", resourceToString("/hvdc-line-map-data.json"));
    }

    @Test
    public void shouldReturnNotFoundinsteadOfLineMapData() {
        notFoundTestForElementInfos(NETWORK_UUID, null, ElementType.LINE, InfoType.LIST, "NOT_EXISTING_LINE");
        notFoundTestForElementInfos(NETWORK_UUID, VARIANT_ID, ElementType.LINE, InfoType.LIST, "NOT_EXISTING_LINE");
    }

    @Test
    public void shouldReturnGeneratorsIds() {
        succeedingTestForElementsIds(NETWORK_UUID, null, ElementType.GENERATOR, List.of(), List.of("GEN", "GEN2").toString());
        succeedingTestForElementsIds(NETWORK_UUID, VARIANT_ID, ElementType.GENERATOR, List.of(), List.of("GEN", "GEN2").toString());
        succeedingTestForElementsIds(NETWORK_UUID, VARIANT_ID, ElementType.GENERATOR, List.of("P1"), List.of("GEN", "GEN2").toString());
    }

    @Test
    public void shouldReturnNotFoundInsteadOfGeneratorsMapData() {
        notFoundTestForElementsInfos(NOT_FOUND_NETWORK_ID, null, ElementType.GENERATOR, InfoType.LIST, List.of());
        notFoundTestForElementsInfos(NETWORK_UUID, VARIANT_ID_NOT_FOUND, ElementType.GENERATOR, InfoType.LIST, List.of());
        notFoundTestForElementsInfos(NOT_FOUND_NETWORK_ID, null, ElementType.GENERATOR, InfoType.LIST, List.of("P1", "P2"));
        notFoundTestForElementsInfos(NETWORK_UUID, VARIANT_ID_NOT_FOUND, ElementType.GENERATOR, InfoType.LIST, List.of("P1", "P2"));
    }

    @Test
    public void shouldReturnNotFoundInsteadOfGeneratorsIds() {
        notFoundTestForElementsIds(NOT_FOUND_NETWORK_ID, null, ElementType.GENERATOR, List.of());
        notFoundTestForElementsIds(NETWORK_UUID, VARIANT_ID_NOT_FOUND, ElementType.GENERATOR, List.of());
    }

    @Test
    public void shouldReturnNotFoundInsteadOfGeneratorMapData() {
        notFoundTestForElementInfos(NETWORK_UUID, null, ElementType.GENERATOR, InfoType.LIST, "NOT_EXISTING_GEN");
        notFoundTestForElementInfos(NETWORK_UUID, VARIANT_ID, ElementType.GENERATOR, InfoType.LIST, "NOT_EXISTING_GEN");
    }

    @Test
    public void shouldReturnTwoWindingsTransformersIds() {
        succeedingTestForElementsIds(NETWORK_UUID, null, ElementType.TWO_WINDINGS_TRANSFORMER, List.of(), List.of("NGEN_NHV1", "NGEN_NHV2", "NHV2_NLOAD").toString());
        succeedingTestForElementsIds(NETWORK_UUID, VARIANT_ID, ElementType.TWO_WINDINGS_TRANSFORMER, List.of(), List.of("NGEN_NHV1", "NGEN_NHV2", "NHV2_NLOAD").toString());
        succeedingTestForElementsIds(NETWORK_UUID, VARIANT_ID, ElementType.TWO_WINDINGS_TRANSFORMER, List.of("P1"), List.of("NGEN_NHV1", "NGEN_NHV2").toString());
    }

    @Test
    public void shouldReturnNotFoundInsteadOfTwoWindingsTransformersMapData() {
        notFoundTestForElementsInfos(NOT_FOUND_NETWORK_ID, null, ElementType.TWO_WINDINGS_TRANSFORMER, InfoType.LIST, List.of());
        notFoundTestForElementsInfos(NETWORK_UUID, VARIANT_ID_NOT_FOUND, ElementType.TWO_WINDINGS_TRANSFORMER, InfoType.LIST, List.of());
        notFoundTestForElementsInfos(NOT_FOUND_NETWORK_ID, null, ElementType.TWO_WINDINGS_TRANSFORMER, InfoType.LIST, List.of("P1", "P2"));
        notFoundTestForElementsInfos(NETWORK_UUID, VARIANT_ID_NOT_FOUND, ElementType.TWO_WINDINGS_TRANSFORMER, InfoType.LIST, List.of("P1", "P2"));
    }

    @Test
    public void shouldReturnNotFoundInsteadOfTwoWindingsTransformersIds() {
        notFoundTestForElementsIds(NOT_FOUND_NETWORK_ID, null, ElementType.TWO_WINDINGS_TRANSFORMER, List.of());
        notFoundTestForElementsIds(NETWORK_UUID, VARIANT_ID_NOT_FOUND, ElementType.TWO_WINDINGS_TRANSFORMER, List.of());
    }

    @Test
    public void shouldReturnNotFoundInsteadOfTwoWindingsTransformerMapData() {
        notFoundTestForElementInfos(NETWORK_UUID, null, ElementType.TWO_WINDINGS_TRANSFORMER, InfoType.LIST, "NOT_EXISTING_2WT");
        notFoundTestForElementInfos(NETWORK_UUID, VARIANT_ID, ElementType.TWO_WINDINGS_TRANSFORMER, InfoType.LIST, "NOT_EXISTING_2WT");
    }

    @Test
    public void shouldReturnThreeWindingsTransformersIds() {
        succeedingTestForElementsIds(NETWORK_UUID, null, ElementType.THREE_WINDINGS_TRANSFORMER, List.of(), List.of("TWT", "TWT21", "TWT32").toString());
        succeedingTestForElementsIds(NETWORK_UUID, VARIANT_ID, ElementType.THREE_WINDINGS_TRANSFORMER, List.of(), List.of("TWT", "TWT21", "TWT32").toString());
    }

    @Test
    public void shouldReturnNotFoundInsteadOfThreeWindingsTransformersMapData() {
        notFoundTestForElementsInfos(NOT_FOUND_NETWORK_ID, null, ElementType.THREE_WINDINGS_TRANSFORMER, InfoType.LIST, List.of());
        notFoundTestForElementsInfos(NETWORK_UUID, VARIANT_ID_NOT_FOUND, ElementType.THREE_WINDINGS_TRANSFORMER, InfoType.LIST, List.of());
        notFoundTestForElementsInfos(NOT_FOUND_NETWORK_ID, null, ElementType.THREE_WINDINGS_TRANSFORMER, InfoType.LIST, List.of("P1", "P2"));
        notFoundTestForElementsInfos(NETWORK_UUID, VARIANT_ID_NOT_FOUND, ElementType.THREE_WINDINGS_TRANSFORMER, InfoType.LIST, List.of("P1", "P2"));
    }

    @Test
    public void shouldReturnNotFoundInsteadOfThreeWindingsTransformersIds() {
        notFoundTestForElementsIds(NOT_FOUND_NETWORK_ID, null, ElementType.THREE_WINDINGS_TRANSFORMER, List.of());
        notFoundTestForElementsIds(NETWORK_UUID, VARIANT_ID_NOT_FOUND, ElementType.THREE_WINDINGS_TRANSFORMER, List.of());
    }

    @Test
    public void shouldReturnAllData() throws Exception {
        succeedingTestForEquipmentsInfos(NETWORK_UUID, null, "all", null, resourceToString("/all-data.json"));
        succeedingTestForEquipmentsInfos(NETWORK_UUID, null, "all", List.of("P3"), resourceToString("/partial-all-data.json"));
        succeedingTestForEquipmentsInfos(NETWORK_UUID, VARIANT_ID, "all", null, resourceToString("/all-data-in-variant.json"));
        succeedingTestForEquipmentsInfos(NETWORK_UUID, VARIANT_ID, "all", List.of("P3"), resourceToString("/partial-all-data-in-variant.json"));
        succeedingTestForEquipmentsInfos(NETWORK_UUID, null, "all", List.of("P3", "P6"), resourceToString("/partial-all-map-data-no-redundant-lines.json"));
    }

    @Test
    public void shouldReturnNotFoundInsteadOfAllData() {
        notFoundTestForEquipmentsInfos(NOT_FOUND_NETWORK_ID, null, "all", List.of());
        notFoundTestForEquipmentsInfos(NETWORK_UUID, VARIANT_ID_NOT_FOUND, "all", List.of());
        notFoundTestForEquipmentsInfos(NOT_FOUND_NETWORK_ID, null, "all", List.of("P1"));
        notFoundTestForEquipmentsInfos(NETWORK_UUID, VARIANT_ID_NOT_FOUND, "all", List.of("P1"));
    }

    @Test
    public void shouldReturnBatteriesIds() {
        succeedingTestForElementsIds(NETWORK_UUID, null, ElementType.BATTERY, List.of(), List.of("BATTERY1", "BATTERY2").toString());
        succeedingTestForElementsIds(NETWORK_UUID, VARIANT_ID, ElementType.BATTERY, List.of(), List.of("BATTERY1", "BATTERY2").toString());
        succeedingTestForElementsIds(NETWORK_UUID, VARIANT_ID, ElementType.BATTERY, List.of("P1"), List.of("BATTERY1").toString());
    }

    @Test
    public void shouldReturnNotFoundInsteadOfBatteriesMapData() {
        notFoundTestForElementsInfos(NOT_FOUND_NETWORK_ID, null, ElementType.BATTERY, InfoType.LIST, List.of());
        notFoundTestForElementsInfos(NETWORK_UUID, VARIANT_ID_NOT_FOUND, ElementType.BATTERY, InfoType.LIST, List.of());
        notFoundTestForElementsInfos(NOT_FOUND_NETWORK_ID, null, ElementType.BATTERY, InfoType.LIST, List.of("P1", "P2"));
        notFoundTestForElementsInfos(NETWORK_UUID, VARIANT_ID_NOT_FOUND, ElementType.BATTERY, InfoType.LIST, List.of("P1", "P2"));
    }

    @Test
    public void shouldReturnNotFoundInsteadOfBatteriesIds() {
        notFoundTestForElementsIds(NOT_FOUND_NETWORK_ID, null, ElementType.BATTERY, List.of());
        notFoundTestForElementsIds(NETWORK_UUID, VARIANT_ID_NOT_FOUND, ElementType.BATTERY, List.of());
    }

    @Test
    public void shouldReturnNotFoundInsteadOfBatteryMapData() {
        notFoundTestForElementInfos(NETWORK_UUID, null, ElementType.BATTERY, InfoType.LIST, "NOT_EXISTING_BAT");
        notFoundTestForElementInfos(NETWORK_UUID, VARIANT_ID, ElementType.BATTERY, InfoType.LIST, "NOT_EXISTING_BAT");
    }

    @Test
    public void shouldReturnBatteriesFormData() throws Exception {

        succeedingTestForElementInfos(NETWORK_UUID, null, ElementType.BATTERY, InfoType.FORM, null, resourceToString("/batteries-map-data.json"));
        succeedingTestForElementInfos(NETWORK_UUID, VARIANT_ID, ElementType.BATTERY, InfoType.FORM, null, resourceToString("/batteries-map-data.json"));
    }

    @Test
    public void shouldReturnBatteriesTabData() throws Exception {
        succeedingTestForElementsInfos(NETWORK_UUID, null, ElementType.BATTERY, InfoType.TAB, null, resourceToString("/batteries-tab-data.json"));
        succeedingTestForElementsInfos(NETWORK_UUID, VARIANT_ID, ElementType.BATTERY, InfoType.TAB, null, resourceToString("/batteries-tab-data.json"));
    }

    @Test
    public void shouldReturnDanglingIds() {
        succeedingTestForElementsIds(NETWORK_UUID, null, ElementType.DANGLING_LINE, List.of(), List.of("DL1", "DL2").toString());
        succeedingTestForElementsIds(NETWORK_UUID, VARIANT_ID, ElementType.DANGLING_LINE, List.of(), List.of("DL1", "DL2").toString());
        succeedingTestForElementsIds(NETWORK_UUID, VARIANT_ID, ElementType.DANGLING_LINE, List.of("P1", "P3"), List.of("DL1", "DL2").toString());
    }

    @Test
    public void shouldReturnNotFoundInsteadOfDanglingLinesMapData() {
        notFoundTestForElementsInfos(NOT_FOUND_NETWORK_ID, null, ElementType.DANGLING_LINE, InfoType.LIST, List.of());
        notFoundTestForElementsInfos(NETWORK_UUID, VARIANT_ID_NOT_FOUND, ElementType.DANGLING_LINE, InfoType.LIST, List.of());
        notFoundTestForElementsInfos(NOT_FOUND_NETWORK_ID, null, ElementType.DANGLING_LINE, InfoType.LIST, List.of("P1", "P2"));
        notFoundTestForElementsInfos(NETWORK_UUID, VARIANT_ID_NOT_FOUND, ElementType.DANGLING_LINE, InfoType.LIST, List.of("P1", "P2"));
    }

    @Test
    public void shouldReturnNotFoundInsteadOfDanglingLinesIds() {
        notFoundTestForElementsIds(NOT_FOUND_NETWORK_ID, null, ElementType.DANGLING_LINE, List.of());
        notFoundTestForElementsIds(NETWORK_UUID, VARIANT_ID_NOT_FOUND, ElementType.DANGLING_LINE, List.of());
    }

    @Test
    public void shouldReturnLoadsFormData() throws Exception {
        succeedingTestForElementsInfos(NETWORK_UUID, null, ElementType.LOAD, InfoType.FORM, null, resourceToString("/loads-form-data.json"));
        succeedingTestForElementsInfos(NETWORK_UUID, VARIANT_ID, ElementType.LOAD, InfoType.FORM, null, resourceToString("/loads-form-data.json"));
        succeedingTestForElementsInfos(NETWORK_UUID, null, ElementType.LOAD, InfoType.FORM, List.of("P2"), resourceToString("/partial-loads-form-data.json"));
        succeedingTestForElementsInfos(NETWORK_UUID, VARIANT_ID, ElementType.LOAD, InfoType.FORM, List.of("P2"), resourceToString("/partial-loads-form-data.json"));
    }

    @Test
    public void shouldReturnLoadsIds() {
        succeedingTestForElementsIds(NETWORK_UUID, null, ElementType.LOAD, List.of(), List.of("LOAD", "LOAD_WITH_NULL_NAME", "LOAD_ID").toString());
        succeedingTestForElementsIds(NETWORK_UUID, VARIANT_ID, ElementType.LOAD, List.of(), List.of("LOAD", "LOAD_WITH_NULL_NAME", "LOAD_ID").toString());
        succeedingTestForElementsIds(NETWORK_UUID, VARIANT_ID, ElementType.LOAD, List.of("P1", "P2"), List.of("LOAD", "LOAD_WITH_NULL_NAME", "LOAD_ID").toString());
    }

    @Test
    public void shouldReturnNotFoundInsteadOfLoadsMapData() {
        notFoundTestForElementsInfos(NOT_FOUND_NETWORK_ID, null, ElementType.LOAD, InfoType.LIST, List.of());
        notFoundTestForElementsInfos(NETWORK_UUID, VARIANT_ID_NOT_FOUND, ElementType.LOAD, InfoType.LIST, List.of());
        notFoundTestForElementsInfos(NOT_FOUND_NETWORK_ID, null, ElementType.LOAD, InfoType.LIST, List.of("P1", "P2"));
        notFoundTestForElementsInfos(NETWORK_UUID, VARIANT_ID_NOT_FOUND, ElementType.LOAD, InfoType.LIST, List.of("P1", "P2"));
    }

    @Test
    public void shouldReturnNotFoundInsteadOfLoadsIds() {
        notFoundTestForElementsIds(NOT_FOUND_NETWORK_ID, null, ElementType.LOAD, List.of());
        notFoundTestForElementsIds(NETWORK_UUID, VARIANT_ID_NOT_FOUND, ElementType.LOAD, List.of());
    }

    @Test
    public void shouldReturnLoadFormData() throws Exception {
        succeedingTestForElementInfos(NETWORK_UUID, null, ElementType.LOAD, InfoType.FORM, "LOAD", resourceToString("/load-form-data.json"));
        succeedingTestForElementInfos(NETWORK_UUID, VARIANT_ID, ElementType.LOAD, InfoType.FORM, "LOAD", resourceToString("/load-form-data.json"));
        succeedingTestForElementInfos(NETWORK_UUID, null, ElementType.LOAD, InfoType.FORM, "LOAD_WITH_NULL_NAME", resourceToString("/load-form-data-test-null-name.json"));
        succeedingTestForElementInfos(NETWORK_UUID, null, ElementType.LOAD, InfoType.FORM, "LOAD_ID", resourceToString("/load-form-data-not-null-name.json"));
    }

    @Test
    public void shouldReturnNotFoundInsteadOfLoadMapData() {
        notFoundTestForElementInfos(NETWORK_UUID, null, ElementType.LOAD, InfoType.LIST, "LOAD2ef");
        notFoundTestForElementInfos(NETWORK_UUID, VARIANT_ID, ElementType.LOAD, InfoType.LIST, "LOAD2dqs");
    }

    @Test
    public void shouldReturnShuntCompensatorsIds() {
        succeedingTestForElementsIds(NETWORK_UUID, null, ElementType.SHUNT_COMPENSATOR, List.of(), List.of("SHUNT1", "SHUNT2", "SHUNT_VLNB").toString());
        succeedingTestForElementsIds(NETWORK_UUID, VARIANT_ID, ElementType.SHUNT_COMPENSATOR, List.of(), List.of("SHUNT1", "SHUNT2", "SHUNT3", "SHUNT_VLNB").toString());
        succeedingTestForElementsIds(NETWORK_UUID, VARIANT_ID, ElementType.SHUNT_COMPENSATOR, List.of("P1", "P2", "P3"), List.of("SHUNT1", "SHUNT2", "SHUNT3").toString());
    }

    @Test
    public void shouldReturnNotFoundInsteadOfShuntCompensatorsMapData() {
        notFoundTestForElementsInfos(NOT_FOUND_NETWORK_ID, null, ElementType.SHUNT_COMPENSATOR, InfoType.LIST, List.of());
        notFoundTestForElementsInfos(NETWORK_UUID, VARIANT_ID_NOT_FOUND, ElementType.SHUNT_COMPENSATOR, InfoType.LIST, List.of());
        notFoundTestForElementsInfos(NOT_FOUND_NETWORK_ID, null, ElementType.SHUNT_COMPENSATOR, InfoType.LIST, List.of("P1", "P2"));
        notFoundTestForElementsInfos(NETWORK_UUID, VARIANT_ID_NOT_FOUND, ElementType.SHUNT_COMPENSATOR, InfoType.LIST, List.of("P1", "P2"));
    }

    @Test
    public void shouldReturnNotFoundInsteadOfShuntCompensatorsIds() {
        notFoundTestForElementsIds(NOT_FOUND_NETWORK_ID, null, ElementType.SHUNT_COMPENSATOR, List.of());
        notFoundTestForElementsIds(NETWORK_UUID, VARIANT_ID_NOT_FOUND, ElementType.SHUNT_COMPENSATOR, List.of());
    }

    @Test
    public void shouldReturnNotFoundInsteadOfShuntCompensatorMapData() {
        notFoundTestForElementInfos(NETWORK_UUID, null, ElementType.SHUNT_COMPENSATOR, InfoType.LIST, "NOT_EXISTING_SC");
        notFoundTestForElementInfos(NETWORK_UUID, VARIANT_ID, ElementType.SHUNT_COMPENSATOR, InfoType.LIST, "NOT_EXISTING_SC");
    }

    @Test
    public void shouldReturnStaticVarCompensatorsIds() {
        succeedingTestForElementsIds(NETWORK_UUID, null, ElementType.STATIC_VAR_COMPENSATOR, List.of(), List.of("SVC1", "SVC2").toString());
        succeedingTestForElementsIds(NETWORK_UUID, VARIANT_ID, ElementType.STATIC_VAR_COMPENSATOR, List.of(), List.of("SVC1", "SVC2").toString());
        succeedingTestForElementsIds(NETWORK_UUID, VARIANT_ID, ElementType.STATIC_VAR_COMPENSATOR, List.of("P1", "P2"), List.of("SVC1", "SVC2").toString());
    }

    @Test
    public void shouldReturnNotFoundInsteadOfStaticVarCompensatorsMapData() {
        notFoundTestForElementsInfos(NOT_FOUND_NETWORK_ID, null, ElementType.STATIC_VAR_COMPENSATOR, InfoType.LIST, List.of());
        notFoundTestForElementsInfos(NETWORK_UUID, VARIANT_ID_NOT_FOUND, ElementType.STATIC_VAR_COMPENSATOR, InfoType.LIST, List.of());
        notFoundTestForElementsInfos(NOT_FOUND_NETWORK_ID, null, ElementType.STATIC_VAR_COMPENSATOR, InfoType.LIST, List.of("P1", "P2"));
        notFoundTestForElementsInfos(NETWORK_UUID, VARIANT_ID_NOT_FOUND, ElementType.STATIC_VAR_COMPENSATOR, InfoType.LIST, List.of("P1", "P2"));
    }

    @Test
    public void shouldReturnNotFoundInsteadOfStaticVarCompensatorsIds() {
        notFoundTestForElementsIds(NOT_FOUND_NETWORK_ID, null, ElementType.STATIC_VAR_COMPENSATOR, List.of());
        notFoundTestForElementsIds(NETWORK_UUID, VARIANT_ID_NOT_FOUND, ElementType.STATIC_VAR_COMPENSATOR, List.of());
    }

    @Test
    public void shouldReturnLccConverterStationsIds() {
        succeedingTestForElementsIds(NETWORK_UUID, null, ElementType.LCC_CONVERTER_STATION, List.of(), List.of("LCC1", "LCC2").toString());
        succeedingTestForElementsIds(NETWORK_UUID, VARIANT_ID, ElementType.LCC_CONVERTER_STATION, List.of(), List.of("LCC1", "LCC2").toString());
        succeedingTestForElementsIds(NETWORK_UUID, VARIANT_ID, ElementType.LCC_CONVERTER_STATION, List.of("P1", "P2"), List.of("LCC1", "LCC2").toString());
    }

    @Test
    public void shouldReturnNotFoundInsteadOfLccConverterStationsMapData() {
        notFoundTestForElementsInfos(NOT_FOUND_NETWORK_ID, null, ElementType.LCC_CONVERTER_STATION, InfoType.LIST, List.of());
        notFoundTestForElementsInfos(NETWORK_UUID, VARIANT_ID_NOT_FOUND, ElementType.LCC_CONVERTER_STATION, InfoType.LIST, List.of());
        notFoundTestForElementsInfos(NOT_FOUND_NETWORK_ID, null, ElementType.LCC_CONVERTER_STATION, InfoType.LIST, List.of("P1", "P2"));
        notFoundTestForElementsInfos(NETWORK_UUID, VARIANT_ID_NOT_FOUND, ElementType.LCC_CONVERTER_STATION, InfoType.LIST, List.of("P1", "P2"));
    }

    @Test
    public void shouldReturnNotFoundInsteadOfLccConverterStationsIds() {
        notFoundTestForElementsIds(NOT_FOUND_NETWORK_ID, null, ElementType.LCC_CONVERTER_STATION, List.of());
        notFoundTestForElementsIds(NETWORK_UUID, VARIANT_ID_NOT_FOUND, ElementType.LCC_CONVERTER_STATION, List.of());
    }

    @Test
    public void shouldReturnVscConverterStationsIds() {
        succeedingTestForElementsIds(NETWORK_UUID, null, ElementType.VSC_CONVERTER_STATION, List.of(), List.of("VSC1", "VSC3", "VSC4", "VSC5", "VSC6", "VSC2").toString());
        succeedingTestForElementsIds(NETWORK_UUID, VARIANT_ID, ElementType.VSC_CONVERTER_STATION, List.of(), List.of("VSC1", "VSC3", "VSC4", "VSC5", "VSC6", "VSC2").toString());
        succeedingTestForElementsIds(NETWORK_UUID, VARIANT_ID, ElementType.VSC_CONVERTER_STATION, List.of("P1", "P2"), List.of("VSC6", "VSC1", "VSC3", "VSC4", "VSC5").toString());
    }

    @Test
    public void shouldReturnNotFoundInsteadOfVscConverterStationsMapData() {
        notFoundTestForElementsInfos(NOT_FOUND_NETWORK_ID, null, ElementType.VSC_CONVERTER_STATION, InfoType.LIST, List.of());
        notFoundTestForElementsInfos(NETWORK_UUID, VARIANT_ID_NOT_FOUND, ElementType.VSC_CONVERTER_STATION, InfoType.LIST, List.of());
        notFoundTestForElementsInfos(NOT_FOUND_NETWORK_ID, null, ElementType.VSC_CONVERTER_STATION, InfoType.LIST, List.of("P1", "P2"));
        notFoundTestForElementsInfos(NETWORK_UUID, VARIANT_ID_NOT_FOUND, ElementType.VSC_CONVERTER_STATION, InfoType.LIST, List.of("P1", "P2"));
    }

    @Test
    public void shouldReturnNotFoundInsteadOfVscConverterStationsIds() {
        notFoundTestForElementsIds(NOT_FOUND_NETWORK_ID, null, ElementType.VSC_CONVERTER_STATION, List.of());
        notFoundTestForElementsIds(NETWORK_UUID, VARIANT_ID_NOT_FOUND, ElementType.VSC_CONVERTER_STATION, List.of());
    }

    @Test
    public void shouldReturnHvdcLinesTabData() throws Exception {
        succeedingTestForElementsInfos(NETWORK_UUID, null, ElementType.HVDC_LINE, InfoType.TAB, null, resourceToString("/hvdc-lines-tab-data.json"));
        succeedingTestForElementsInfos(NETWORK_UUID, VARIANT_ID, ElementType.HVDC_LINE, InfoType.TAB, null, resourceToString("/hvdc-lines-tab-data.json"));
    }

    @Test
    public void shouldReturnHvdcLinesIds() {
        succeedingTestForElementsIds(NETWORK_UUID, null, ElementType.HVDC_LINE, List.of(), List.of("HVDC1", "HVDC3", "HVDC4", "HVDC2").toString());
        succeedingTestForElementsIds(NETWORK_UUID, VARIANT_ID, ElementType.HVDC_LINE, List.of(), List.of("HVDC1", "HVDC3", "HVDC4", "HVDC2").toString());
        succeedingTestForElementsIds(NETWORK_UUID, VARIANT_ID, ElementType.HVDC_LINE, List.of("P1", "P3", "P4"), List.of("HVDC1", "HVDC3", "HVDC4").toString());
    }

    @Test
    public void shouldReturnNotFoundInsteadOfHvdcLinesMapData() {
        notFoundTestForElementsInfos(NOT_FOUND_NETWORK_ID, null, ElementType.HVDC_LINE, InfoType.LIST, List.of());
        notFoundTestForElementsInfos(NETWORK_UUID, VARIANT_ID_NOT_FOUND, ElementType.HVDC_LINE, InfoType.LIST, List.of());
        notFoundTestForElementsInfos(NOT_FOUND_NETWORK_ID, null, ElementType.HVDC_LINE, InfoType.LIST, List.of("P1", "P2"));
        notFoundTestForElementsInfos(NETWORK_UUID, VARIANT_ID_NOT_FOUND, ElementType.HVDC_LINE, InfoType.LIST, List.of("P1", "P2"));
    }

    @Test
    public void shouldReturnNotFoundInsteadOfHvdcLinesIds() {
        notFoundTestForElementsIds(NOT_FOUND_NETWORK_ID, null, ElementType.HVDC_LINE, List.of());
        notFoundTestForElementsIds(NETWORK_UUID, VARIANT_ID_NOT_FOUND, ElementType.HVDC_LINE, List.of());
    }

    @Test
    public void shouldReturnVoltageLevelsFormData() throws Exception {
        succeedingTestForElementsInfos(NETWORK_UUID, null, ElementType.VOLTAGE_LEVEL, InfoType.FORM, null, resourceToString("/voltage-levels-form-data.json"));
        succeedingTestForElementsInfos(NETWORK_UUID, VARIANT_ID, ElementType.VOLTAGE_LEVEL, InfoType.FORM, null, resourceToString("/voltage-levels-form-data.json"));

        succeedingTestForElementsInfos(NETWORK_UUID, null, ElementType.VOLTAGE_LEVEL, InfoType.FORM, List.of("P3"), resourceToString("/partial-voltage-levels-form-data.json"));
        succeedingTestForElementsInfos(NETWORK_UUID, VARIANT_ID, ElementType.VOLTAGE_LEVEL, InfoType.FORM, List.of("P3"), resourceToString("/partial-voltage-levels-form-data.json"));
    }

    @Test
    public void shouldReturnVotlageLevelsListData() throws Exception {
        succeedingTestForElementsInfos(NETWORK_UUID, null, ElementType.VOLTAGE_LEVEL, InfoType.LIST, null, resourceToString("/voltage-levels-list-data.json"));
        succeedingTestForElementsInfos(NETWORK_UUID, VARIANT_ID, ElementType.VOLTAGE_LEVEL, InfoType.LIST, null, resourceToString("/voltage-levels-list-data.json"));

        succeedingTestForElementsInfos(NETWORK_UUID, null, ElementType.VOLTAGE_LEVEL, InfoType.LIST, List.of("P3"), resourceToString("/partial-voltage-levels-list-data.json"));
        succeedingTestForElementsInfos(NETWORK_UUID, VARIANT_ID, ElementType.VOLTAGE_LEVEL, InfoType.LIST, List.of("P3"), resourceToString("/partial-voltage-levels-list-data.json"));
    }

    @Test
    public void shouldReturnVoltageLevelsIds() throws Exception {
        succeedingTestForElementsIds(NETWORK_UUID, null, ElementType.VOLTAGE_LEVEL, List.of(), List.of("VLGEN", "VLHV1", "VLHV2", "VLLOAD", "VLNEW2", "VLGEN3", "VLGEN4", "VLGEN5", "VLGEN6").toString());
        succeedingTestForElementsIds(NETWORK_UUID, VARIANT_ID, ElementType.VOLTAGE_LEVEL, List.of(), List.of("VLGEN", "VLHV1", "VLHV2", "VLLOAD", "VLNEW2", "VLGEN3", "VLGEN4", "VLGEN5", "VLGEN6").toString());
        succeedingTestForElementsIds(NETWORK_UUID, VARIANT_ID, ElementType.VOLTAGE_LEVEL, List.of("P1", "P2", "P3", "P4", "P5", "P6"), List.of("VLGEN", "VLHV1", "VLHV2", "VLLOAD", "VLNEW2", "VLGEN3", "VLGEN4", "VLGEN5", "VLGEN6").toString());
    }

    @Test
    public void shouldReturnNotFoundInsteadOfVoltageLevelsIds() {
        notFoundTestForElementsIds(NOT_FOUND_NETWORK_ID, null, ElementType.VOLTAGE_LEVEL, List.of());
        notFoundTestForElementsIds(NOT_FOUND_NETWORK_ID, VARIANT_ID_NOT_FOUND, ElementType.VOLTAGE_LEVEL, List.of());
    }

    @Test
    public void shouldReturnNotFoundInsteadOfVoltageLevelsMapData() {
        notFoundTestForElementsInfos(NOT_FOUND_NETWORK_ID, null, ElementType.VOLTAGE_LEVEL, InfoType.LIST, List.of());
        notFoundTestForElementsInfos(NETWORK_UUID, VARIANT_ID_NOT_FOUND, ElementType.VOLTAGE_LEVEL, InfoType.LIST, List.of());
        notFoundTestForElementsInfos(NOT_FOUND_NETWORK_ID, null, ElementType.VOLTAGE_LEVEL, InfoType.LIST, List.of("P1", "P2"));
        notFoundTestForElementsInfos(NETWORK_UUID, VARIANT_ID_NOT_FOUND, ElementType.VOLTAGE_LEVEL, InfoType.LIST, List.of("P1", "P2"));
    }

    @Test
    public void shouldReturnVotlageLevelFormData() throws Exception {
        succeedingTestForElementInfos(NETWORK_UUID, null, ElementType.VOLTAGE_LEVEL, InfoType.FORM, "VLGEN4", resourceToString("/voltage-level-form-data.json"));
        succeedingTestForElementInfos(NETWORK_UUID, VARIANT_ID, ElementType.VOLTAGE_LEVEL, InfoType.FORM, "VLGEN4", resourceToString("/voltage-level-form-data.json"));
    }

    @Test
    public void shouldReturnVotlageLevelTabData() throws Exception {
        succeedingTestForElementInfos(NETWORK_UUID, null, ElementType.VOLTAGE_LEVEL, InfoType.TAB, "VLGEN4", resourceToString("/voltage-level-tab-data.json"));
        succeedingTestForElementInfos(NETWORK_UUID, VARIANT_ID, ElementType.VOLTAGE_LEVEL, InfoType.TAB, "VLGEN4", resourceToString("/voltage-level-tab-data.json"));
    }

    @Test
    public void shouldReturnNotFoundInsteadOfVotlageLevelMapData() {
        notFoundTestForElementInfos(NETWORK_UUID, null, ElementType.VOLTAGE_LEVEL, InfoType.LIST, "NOT_EXISTING_VL");
        notFoundTestForElementInfos(NETWORK_UUID, VARIANT_ID, ElementType.VOLTAGE_LEVEL, InfoType.LIST, "NOT_EXISTING_VL");
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
    public void shouldReturnHvdcShuntCompensatorMapData() throws Exception {
        succeedingHvdcWithShuntCompensatorsTest(NETWORK_UUID, "HVDC1", null, resourceToString("/hvdc-line-with-shunt-compensators-map-data.json"));
    }

    @Test
    public void shouldReturnVoltageLevelsAndEquipments() throws Exception {
        succeedingTestForEquipmentsInfos(NETWORK_UUID, null, "voltage-levels-equipments", null, resourceToString("/voltage-levels-equipments-map-data.json"));
        succeedingTestForEquipmentsInfos(NETWORK_UUID, VARIANT_ID, "voltage-levels-equipments", null, resourceToString("/voltage-levels-equipments-map-data-in-variant.json"));
    }

    @Test
    public void shouldReturnVoltageLevelEquipments() throws Exception {
        succeedingTestForEquipmentsInfos(NETWORK_UUID, null, "voltage-level-equipments/VLGEN", List.of(), resourceToString("/voltage-level-VLGEN-equipments.json"));
        succeedingTestForEquipmentsInfos(NETWORK_UUID, VARIANT_ID, "voltage-level-equipments/VLGEN", List.of(), resourceToString("/voltage-level-VLGEN-equipments.json"));
    }

    @Test
    public void shouldReturnNotFoundInsteadOfVoltageLevelsEquipmentsMapData() {
        notFoundTestForEquipmentsInfos(NOT_FOUND_NETWORK_ID, null, "voltage-levels-equipments", List.of());
        notFoundTestForEquipmentsInfos(NETWORK_UUID, VARIANT_ID_NOT_FOUND, "voltage-levels-equipments", List.of());
        notFoundTestForEquipmentsInfos(NOT_FOUND_NETWORK_ID, null, "voltage-levels-equipments", List.of("P1"));
        notFoundTestForEquipmentsInfos(NETWORK_UUID, VARIANT_ID_NOT_FOUND, "voltage-levels-equipments", List.of("P1"));
    }

    @Test
    public void shouldReturnSubstationsMapData() throws Exception {
        succeedingTestForElementsInfos(NETWORK_UUID, null, ElementType.SUBSTATION, InfoType.MAP, null, resourceToString("/substations-map-data.json"));
        succeedingTestForElementsInfos(NETWORK_UUID, VARIANT_ID, ElementType.SUBSTATION, InfoType.MAP, null, resourceToString("/substations-map-data.json"));

        succeedingTestForElementsInfos(NETWORK_UUID, null, ElementType.SUBSTATION, InfoType.MAP, List.of("P1"), resourceToString("/partial-substations-map-data.json"));
        succeedingTestForElementsInfos(NETWORK_UUID, VARIANT_ID, ElementType.SUBSTATION, InfoType.MAP, List.of("P1"), resourceToString("/partial-substations-map-data.json"));
    }

    @Test
    public void shouldReturnSubstationsListData() throws Exception {
        succeedingTestForElementsInfos(NETWORK_UUID, null, ElementType.SUBSTATION, InfoType.LIST, null, resourceToString("/substations-list-data.json"));
        succeedingTestForElementsInfos(NETWORK_UUID, VARIANT_ID, ElementType.SUBSTATION, InfoType.LIST, null, resourceToString("/substations-list-data.json"));

        succeedingTestForElementsInfos(NETWORK_UUID, null, ElementType.SUBSTATION, InfoType.LIST, List.of("P1"), resourceToString("/partial-substations-list-data.json"));
        succeedingTestForElementsInfos(NETWORK_UUID, VARIANT_ID, ElementType.SUBSTATION, InfoType.LIST, List.of("P1"), resourceToString("/partial-substations-list-data.json"));
    }

    @Test
    public void shouldReturnNotFoundInsteadOfMapSubstationsData() {
        notFoundTestForElementsInfos(NOT_FOUND_NETWORK_ID, null, ElementType.SUBSTATION, InfoType.MAP, List.of());
        notFoundTestForElementsInfos(NETWORK_UUID, VARIANT_ID_NOT_FOUND, ElementType.SUBSTATION, InfoType.MAP, List.of());
        notFoundTestForElementsInfos(NOT_FOUND_NETWORK_ID, null, ElementType.SUBSTATION, InfoType.MAP, List.of("P1", "P2"));
        notFoundTestForElementsInfos(NETWORK_UUID, VARIANT_ID_NOT_FOUND, ElementType.SUBSTATION, InfoType.MAP, List.of("P1", "P2"));
    }

    @Test
    public void shouldReturnLinesMapData() throws Exception {
        succeedingTestForElementsInfos(NETWORK_UUID, null, ElementType.LINE, InfoType.MAP, null, resourceToString("/lines-map-data.json"));
        succeedingTestForElementsInfos(NETWORK_UUID, VARIANT_ID, ElementType.LINE, InfoType.MAP, null, resourceToString("/lines-map-data.json"));

        succeedingTestForElementsInfos(NETWORK_UUID, null, ElementType.LINE, InfoType.MAP, List.of("P1"), resourceToString("/partial-lines-map-data.json"));
        succeedingTestForElementsInfos(NETWORK_UUID, VARIANT_ID, ElementType.LINE, InfoType.MAP, List.of("P1"), resourceToString("/partial-lines-map-data.json"));
    }

    @Test
    public void shouldReturnNotFoundInsteadOfMapLinesData() {
        notFoundTestForElementsInfos(NOT_FOUND_NETWORK_ID, null, ElementType.LINE, InfoType.MAP, List.of());
        notFoundTestForElementsInfos(NETWORK_UUID, VARIANT_ID_NOT_FOUND, ElementType.LINE, InfoType.MAP, List.of());
        notFoundTestForElementsInfos(NOT_FOUND_NETWORK_ID, null, ElementType.LINE, InfoType.MAP, List.of("P1"));
        notFoundTestForElementsInfos(NETWORK_UUID, VARIANT_ID_NOT_FOUND, ElementType.LINE, InfoType.MAP, List.of("P1"));
    }

    @Test
    public void shouldReturnHvdcLinesMapData() throws Exception {
        succeedingTestForElementsInfos(NETWORK_UUID, null, ElementType.HVDC_LINE, InfoType.MAP, null, resourceToString("/hvdc-lines-map-data.json"));
        succeedingTestForElementsInfos(NETWORK_UUID, VARIANT_ID, ElementType.HVDC_LINE, InfoType.MAP, null, resourceToString("/hvdc-lines-map-data.json"));

        succeedingTestForElementsInfos(NETWORK_UUID, null, ElementType.HVDC_LINE, InfoType.MAP, List.of("P2"), "[]");
        succeedingTestForElementsInfos(NETWORK_UUID, VARIANT_ID, ElementType.HVDC_LINE, InfoType.MAP, List.of("P2"), "[]");

        succeedingTestForElementsInfos(NETWORK_UUID, null, ElementType.HVDC_LINE, InfoType.MAP, List.of("P1"), resourceToString("/partial-map-hvdc-lines-data.json"));
        succeedingTestForElementsInfos(NETWORK_UUID, VARIANT_ID, ElementType.HVDC_LINE, InfoType.MAP, List.of("P1"), resourceToString("/partial-map-hvdc-lines-data.json"));
    }

    @Test
    public void shouldReturnNotFoundInsteadOfMapHvdcLinesData() {
        notFoundTestForElementsInfos(NOT_FOUND_NETWORK_ID, null, ElementType.HVDC_LINE, InfoType.MAP, List.of());
        notFoundTestForElementsInfos(NETWORK_UUID, VARIANT_ID_NOT_FOUND, ElementType.HVDC_LINE, InfoType.MAP, List.of());
        notFoundTestForElementsInfos(NOT_FOUND_NETWORK_ID, null, ElementType.HVDC_LINE, InfoType.MAP, List.of("P1"));
        notFoundTestForElementsInfos(NETWORK_UUID, VARIANT_ID_NOT_FOUND, ElementType.HVDC_LINE, InfoType.MAP, List.of("P1"));
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
        succeedingTestForElementInfos(NETWORK_UUID, null, ElementType.GENERATOR, InfoType.FORM, "GEN", resourceToString("/generator-map-data.json"));
        succeedingTestForElementInfos(NETWORK_UUID, VARIANT_ID, ElementType.GENERATOR, InfoType.FORM, "GEN", resourceToString("/generator-map-data.json"));
    }

    @Test
    public void shouldReturnGeneratorsTabData() throws Exception {
        succeedingTestForElementsInfos(NETWORK_UUID, null, ElementType.GENERATOR, InfoType.TAB, null, resourceToString("/generators-tab-data.json"));
        succeedingTestForElementsInfos(NETWORK_UUID, VARIANT_ID, ElementType.GENERATOR, InfoType.TAB, null, resourceToString("/generators-tab-data.json"));
    }

    @Test
    public void shouldReturnBatteryTabData() throws Exception {
        succeedingTestForElementsInfos(NETWORK_UUID, null, ElementType.BATTERY, InfoType.TAB, null, resourceToString("/batteries-tab-data.json"));
        succeedingTestForElementsInfos(NETWORK_UUID, VARIANT_ID, ElementType.BATTERY, InfoType.TAB, null, resourceToString("/batteries-tab-data.json"));
        succeedingTestForElementsInfos(NETWORK_UUID, null, ElementType.BATTERY, InfoType.TAB, List.of("P1"), resourceToString("/partial-batteries-tab-data.json"));
        succeedingTestForElementsInfos(NETWORK_UUID, VARIANT_ID, ElementType.BATTERY, InfoType.TAB, List.of("P1"), resourceToString("/partial-batteries-tab-data.json"));
    }

    @Test
    public void shouldReturnShuntCompensatorTabData() throws Exception {
        succeedingTestForElementsInfos(NETWORK_UUID, null, ElementType.SHUNT_COMPENSATOR, InfoType.TAB, null, resourceToString("/shunt-compensators-tab-data.json"));
        succeedingTestForElementsInfos(NETWORK_UUID, VARIANT_ID, ElementType.SHUNT_COMPENSATOR, InfoType.TAB, null, resourceToString("/shunt-compensators-tab-data-in-variant.json"));
    }

    @Test
    public void shouldReturnShuntCompensatorFormData() throws Exception {
        succeedingTestForElementInfos(NETWORK_UUID, null, ElementType.SHUNT_COMPENSATOR, InfoType.FORM, "SHUNT_VLNB", resourceToString("/shunt-compensator-map-data.json"));
        succeedingTestForElementInfos(NETWORK_UUID, VARIANT_ID, ElementType.SHUNT_COMPENSATOR, InfoType.FORM, "SHUNT_VLNB", resourceToString("/shunt-compensator-map-data-in-variant.json"));
    }

    @Test
    public void shouldReturnDanglingLineTabData() throws Exception {
        succeedingTestForElementsInfos(NETWORK_UUID, null, ElementType.DANGLING_LINE, InfoType.TAB, null, resourceToString("/dangling-lines-tab-data.json"));
        succeedingTestForElementsInfos(NETWORK_UUID, VARIANT_ID, ElementType.DANGLING_LINE, InfoType.TAB, null, resourceToString("/dangling-lines-tab-data.json"));
    }

    @Test
    public void shouldReturnStaticVarCompensatorTabData() throws Exception {
        succeedingTestForElementsInfos(NETWORK_UUID, null, ElementType.STATIC_VAR_COMPENSATOR, InfoType.TAB, null, resourceToString("/static-var-compensators-tab-data.json"));
        succeedingTestForElementsInfos(NETWORK_UUID, VARIANT_ID, ElementType.STATIC_VAR_COMPENSATOR, InfoType.TAB, null, resourceToString("/static-var-compensators-tab-data.json"));
    }

    @Test
    public void shouldReturnLccConverterStationTabData() throws Exception {
        succeedingTestForElementsInfos(NETWORK_UUID, null, ElementType.LCC_CONVERTER_STATION, InfoType.TAB, null, resourceToString("/lcc-converter-stations-tab-data.json"));
        succeedingTestForElementsInfos(NETWORK_UUID, VARIANT_ID, ElementType.LCC_CONVERTER_STATION, InfoType.TAB, null, resourceToString("/lcc-converter-stations-tab-data.json"));
    }

    @Test
    public void shouldReturnVscConverterStationTabData() throws Exception {
        succeedingTestForElementsInfos(NETWORK_UUID, null, ElementType.VSC_CONVERTER_STATION, InfoType.TAB, null, resourceToString("/vsc-converter-stations-tab-data.json"));
        succeedingTestForElementsInfos(NETWORK_UUID, VARIANT_ID, ElementType.VSC_CONVERTER_STATION, InfoType.TAB, null, resourceToString("/vsc-converter-stations-tab-data.json"));
    }

    @Test
    public void shouldReturnTwoWindingsTransformerTabData() throws Exception {
        succeedingTestForElementsInfos(NETWORK_UUID, null, ElementType.TWO_WINDINGS_TRANSFORMER, InfoType.TAB, null, resourceToString("/2-windings-transformers-tab-data.json"));
        succeedingTestForElementsInfos(NETWORK_UUID, VARIANT_ID, ElementType.TWO_WINDINGS_TRANSFORMER, InfoType.TAB, null, resourceToString("/2-windings-transformers-tab-data.json"));
    }

    @Test
    public void shouldReturnTwoWindingsTransformerFormData() throws Exception {
        succeedingTestForElementInfos(NETWORK_UUID, null, ElementType.TWO_WINDINGS_TRANSFORMER, InfoType.FORM, "NGEN_NHV1", resourceToString("/2-windings-transformer-map-data.json"));
        succeedingTestForElementInfos(NETWORK_UUID, VARIANT_ID, ElementType.TWO_WINDINGS_TRANSFORMER, InfoType.FORM, "NGEN_NHV1", resourceToString("/2-windings-transformer-map-data.json"));
    }

    @Test
    public void shouldReturnTwoWindingsTransformerTooltipData() throws Exception {
        succeedingTestForElementInfos(NETWORK_UUID, null, ElementType.TWO_WINDINGS_TRANSFORMER, InfoType.TOOLTIP, "NGEN_NHV1", resourceToString("/2-windings-transformer-tooltip-data.json"));
        succeedingTestForElementInfos(NETWORK_UUID, VARIANT_ID, ElementType.TWO_WINDINGS_TRANSFORMER, InfoType.TOOLTIP, "NGEN_NHV1", resourceToString("/2-windings-transformer-tooltip-data.json"));
    }

    @Test
    public void shouldReturnThreeWindingsTransformerTabData() throws Exception {
        succeedingTestForElementsInfos(NETWORK_UUID, null, ElementType.THREE_WINDINGS_TRANSFORMER, InfoType.TAB, null, resourceToString("/3-windings-transformers-tab-data.json"));
        succeedingTestForElementsInfos(NETWORK_UUID, VARIANT_ID, ElementType.THREE_WINDINGS_TRANSFORMER, InfoType.TAB, null, resourceToString("/3-windings-transformers-tab-data.json"));
        succeedingTestForElementsInfos(NETWORK_UUID, null, ElementType.THREE_WINDINGS_TRANSFORMER, InfoType.TAB, List.of("P3"), "[]");
        succeedingTestForElementsInfos(NETWORK_UUID, VARIANT_ID, ElementType.THREE_WINDINGS_TRANSFORMER, InfoType.TAB, List.of("P3"), "[]");
    }
}
