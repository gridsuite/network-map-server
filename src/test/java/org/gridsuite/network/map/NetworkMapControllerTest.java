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
import com.powsybl.network.store.iidm.impl.HvdcLineImpl;
import com.powsybl.network.store.iidm.impl.NetworkFactoryImpl;
import com.powsybl.network.store.iidm.impl.extensions.HvdcAngleDroopActivePowerControlImpl;
import com.powsybl.network.store.iidm.impl.extensions.HvdcOperatorActivePowerRangeImpl;
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

        Network network = EurostagTutorialExample1Factory.createWithMoreGenerators(new NetworkFactoryImpl());
        Line l1 = network.getLine("NHV1_NHV2_1");
        l1.getTerminal1().setP(1.1)
                .setQ(2.2);
        l1.getTerminal2().setP(3.33)
                .setQ(4.44);
        l1.setB1(5).setB2(6).setG1(7).setG2(8);
        l1.setR(9).setX(10);
        l1.newCurrentLimits1().setPermanentLimit(700.4).add();
        l1.newCurrentLimits2().setPermanentLimit(800.8).add();
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
        t1.newCurrentLimits1().setPermanentLimit(900.5).add();
        t1.newCurrentLimits2().setPermanentLimit(950.5).add();
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

        hvdcLineWithExtension.addExtension(HvdcOperatorActivePowerRange.class, new HvdcOperatorActivePowerRangeImpl((HvdcLineImpl) hvdcLineWithExtension, 1000F, 900F));
        hvdcLineWithExtension.addExtension(HvdcAngleDroopActivePowerControl.class, new HvdcAngleDroopActivePowerControlImpl((HvdcLineImpl) hvdcLineWithExtension, 190F, 180F, true));

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

        // Create a connected shunt compensator in variant VARIANT_ID on a NODE_BREAKER voltage level
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

        network.getLoad("LOAD")
                .newExtension(ConnectablePositionAdder.class)
                .newFeeder()
                .withName("feederName")
                .withOrder(0)
                .withDirection(ConnectablePosition.Direction.TOP).add()
                .add();

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

    private String buildUrlForList(String equipments, String variantId, List<String> immutableListSubstationIds) {
        List<String> substationsIds = immutableListSubstationIds == null ? List.of() : immutableListSubstationIds.stream().collect(Collectors.toList());
        StringBuffer url = new StringBuffer("/v1/networks/{networkUuid}/" + equipments);
        if (variantId == null && substationsIds.isEmpty()) {
            return url.toString();
        }

        url.append("?");
        url.append(variantId != null ? "variantId=" + variantId : "substationId=" + substationsIds.remove(0));
        url.append(String.join("", substationsIds.stream().map(id -> String.format("&substationId=%s", id)).collect(Collectors.toList())));

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

    private void failingTestForList(String equipments, UUID networkUuid, String variantId, List<String> substationsIds) throws Exception {
        mvc.perform(get(buildUrlForList(equipments, variantId, substationsIds), networkUuid))
            .andExpect(status().isNotFound());
    }

    private void succeedingTestForList(String equipments, UUID networkUuid, String variantId, List<String> substationsIds, String expectedJson) throws Exception {
        MvcResult res = mvc.perform(get(buildUrlForList(equipments, variantId, substationsIds), networkUuid))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andReturn();
        JSONAssert.assertEquals(res.getResponse().getContentAsString(), expectedJson, JSONCompareMode.NON_EXTENSIBLE);
    }

    private void succeedingTestForElement(String equipments, UUID networkUuid, String variantId, List<String> substationsIds, String elementId, String expectedJson) throws Exception {
        MvcResult res = mvc.perform(get(buildUrlForElement(equipments, variantId, substationsIds), networkUuid, elementId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn();
        JSONAssert.assertEquals(res.getResponse().getContentAsString(), expectedJson, JSONCompareMode.NON_EXTENSIBLE);
    }

    private void succeedingTestForElementNameOrEmpty(String equipments, UUID networkUuid, String variantId, List<String> substationsIds, String elementId, String expectedJson) throws Exception {
        MvcResult res = mvc.perform(get(buildUrlForElement(equipments, variantId, substationsIds), networkUuid, elementId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn();

        JSONAssert.assertEquals(res.getResponse().getContentAsString(), expectedJson, JSONCompareMode.NON_EXTENSIBLE);
    }

    private void failingTestForElement(String equipments, UUID networkUuid, String variantId, List<String> substationsIds, String elementId) throws Exception {
        mvc.perform(get(buildUrlForElement(equipments, variantId, substationsIds), networkUuid, elementId))
                .andExpect(status().isNotFound());
    }

    private String buildUrlVoltageLevels(String variantId, List<String> immutableListSubstationIds) {
        List<String> substationsIds = immutableListSubstationIds == null ? List.of() : immutableListSubstationIds.stream().collect(Collectors.toList());
        StringBuffer url = new StringBuffer("/v1/networks/{networkUuid}/voltage-levels");
        if (variantId == null && substationsIds.isEmpty()) {
            return url.toString();
        }

        url.append("?");
        url.append(variantId != null ? "variantId=" + variantId : "substationId=" + substationsIds.remove(0));
        url.append(String.join("", substationsIds.stream().map(id -> String.format("&substationId=%s", id)).collect(Collectors.toList())));

        return url.toString();
    }

    private void failingVoltageLevelsTest(UUID networkUuid, String variantId, List<String> substationsIds) throws Exception {
        mvc.perform(get(buildUrlVoltageLevels(variantId, substationsIds), networkUuid))
            .andExpect(status().isNotFound());
    }

    private void succeedingVoltageLevelsTest(UUID networkUuid, String variantId, List<String> substationsIds, String expectedJson) throws Exception {
        MvcResult res = mvc.perform(get(buildUrlVoltageLevels(variantId, substationsIds), networkUuid))
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
    public void shouldReturnSubstationsMapData() throws Exception {
        succeedingTestForList("substations", NETWORK_UUID, null, null, resourceToString("/substations-data.json"));
        succeedingTestForList("substations", NETWORK_UUID, VARIANT_ID, null, resourceToString("/substations-data.json"));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfSubstationsMapData() throws Exception {
        failingTestForList("substations", NOT_FOUND_NETWORK_ID, null, null);
        failingTestForList("substations", NETWORK_UUID, VARIANT_ID_NOT_FOUND, null);
    }

    @Test
    public void shouldReturnSubstationsMapDataFromIds() throws Exception {
        succeedingTestForList("substations", NETWORK_UUID, null, List.of("P1"), resourceToString("/partial-substations-map-data.json"));
        succeedingTestForList("substations", NETWORK_UUID, VARIANT_ID, List.of("P1"), resourceToString("/partial-substations-map-data.json"));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfSubstationsMapDataFromIds() throws Exception {
        failingTestForList("substations", NOT_FOUND_NETWORK_ID, null, List.of("P1", "P2"));
        failingTestForList("substations", NETWORK_UUID, VARIANT_ID_NOT_FOUND, List.of("P1", "P2"));
    }

    @Test
    public void shouldReturnSubstationMapData() throws Exception {
        succeedingTestForElement("substations", NETWORK_UUID, null, null, "P4", resourceToString("/substation-data.json"));
        succeedingTestForElement("substations", NETWORK_UUID, VARIANT_ID, null, "P4", resourceToString("/substation-data.json"));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfSubstationMapData() throws Exception {
        failingTestForElement("substations", NOT_FOUND_NETWORK_ID, null, null, "NOT_EXISTING_SUBSTATION");
        failingTestForElement("substations", NETWORK_UUID, VARIANT_ID_NOT_FOUND, null, "NOT_EXISTING_SUBSTATION");
    }

    @Test
    public void shouldReturnLinesMapData() throws Exception {
        succeedingTestForList("lines", NETWORK_UUID, null, null, resourceToString("/lines-map-data.json"));
        succeedingTestForList("lines", NETWORK_UUID, VARIANT_ID, null, resourceToString("/lines-map-data.json"));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfLinesMapData() throws Exception {
        failingTestForList("lines", NOT_FOUND_NETWORK_ID, null, null);
        failingTestForList("lines", NETWORK_UUID, VARIANT_ID_NOT_FOUND, null);
    }

    @Test
    public void shouldReturnLinesMapDataFromIds() throws Exception {
        succeedingTestForList("lines", NETWORK_UUID, null, List.of("P3"), resourceToString("/partial-lines-map-data.json"));
        succeedingTestForList("lines", NETWORK_UUID, VARIANT_ID, List.of("P3"), resourceToString("/partial-lines-map-data.json"));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfLinesMapDataFromIds() throws Exception {
        failingTestForList("lines", NOT_FOUND_NETWORK_ID, null, List.of("P1"));
        failingTestForList("lines", NETWORK_UUID, VARIANT_ID_NOT_FOUND, List.of("P1"));
    }

    @Test
    public void shouldReturnLineMapData() throws Exception {
        succeedingTestForElement("lines", NETWORK_UUID, null, null, "NHV1_NHV2_1", resourceToString("/line-map-data.json"));
        succeedingTestForElement("lines", NETWORK_UUID, VARIANT_ID, null, "NHV1_NHV2_1", resourceToString("/line-map-data.json"));
    }

    @Test
    public void shouldReturnAnErrorinsteadOfLineMapData() throws Exception {
        failingTestForElement("lines", NETWORK_UUID, null, null, "NOT_EXISTING_LINE");
        failingTestForElement("lines", NETWORK_UUID, VARIANT_ID, null, "NOT_EXISTING_LINE");
    }

    @Test
    public void shouldReturnGeneratorsMapData() throws Exception {
        succeedingTestForList("generators", NETWORK_UUID, null, null, resourceToString("/generators-map-data.json"));
        succeedingTestForList("generators", NETWORK_UUID, VARIANT_ID, null, resourceToString("/generators-map-data.json"));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfGeneratorsMapData() throws Exception {
        failingTestForList("generators", NOT_FOUND_NETWORK_ID, null, null);
        failingTestForList("generators", NETWORK_UUID, VARIANT_ID_NOT_FOUND, null);
    }

    @Test
    public void shouldReturnGeneratorsMapDataFromIds() throws Exception {
        succeedingTestForList("generators", NETWORK_UUID, null, List.of("P2"), "[]");
        succeedingTestForList("generators", NETWORK_UUID, VARIANT_ID, List.of("P2"), "[]");
    }

    @Test
    public void shouldReturnAnErrorInsteadOfGeneratorsMapDataFromIds() throws Exception {
        failingTestForList("generators", NOT_FOUND_NETWORK_ID, null, List.of("P1"));
        failingTestForList("generators", NETWORK_UUID, VARIANT_ID_NOT_FOUND, List.of("P1"));
    }

    @Test
    public void shouldReturnGeneratorMapData() throws Exception {
        succeedingTestForElement("generators", NETWORK_UUID, null, null, "GEN", resourceToString("/generator-map-data.json"));
        succeedingTestForElement("generators", NETWORK_UUID, VARIANT_ID, null, "GEN", resourceToString("/generator-map-data.json"));
    }

    @Test
    public void shouldReturnAnErrorinsteadOfGeneratorMapData() throws Exception {
        failingTestForElement("generators", NETWORK_UUID, null, null, "NOT_EXISTING_GEN");
        failingTestForElement("generators", NETWORK_UUID, VARIANT_ID, null, "NOT_EXISTING_GEN");
    }

    @Test
    public void shouldReturnTwoWindingsTransformersMapData() throws Exception {
        succeedingTestForList("2-windings-transformers", NETWORK_UUID, null, null, resourceToString("/2-windings-transformers-map-data.json"));
        succeedingTestForList("2-windings-transformers", NETWORK_UUID, VARIANT_ID, null, resourceToString("/2-windings-transformers-map-data.json"));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfTwoWindingsTransformersMapData() throws Exception {
        failingTestForList("2-windings-transformers", NOT_FOUND_NETWORK_ID, null, null);
        failingTestForList("2-windings-transformers", NETWORK_UUID, VARIANT_ID_NOT_FOUND, null);
    }

    @Test
    public void shouldReturnTwoWindingsTransformersMapDataFromIds() throws Exception {
        succeedingTestForList("2-windings-transformers", NETWORK_UUID, null, List.of("P1"), resourceToString("/partial-2-windings-transformers-map-data.json"));
        succeedingTestForList("2-windings-transformers", NETWORK_UUID, VARIANT_ID, List.of("P1"), resourceToString("/partial-2-windings-transformers-map-data.json"));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfTwoWindingsTransformersMapDataFromIds() throws Exception {
        failingTestForList("2-windings-transformers", NOT_FOUND_NETWORK_ID, null, List.of("P1"));
        failingTestForList("2-windings-transformers", NETWORK_UUID, VARIANT_ID_NOT_FOUND, List.of("P1"));
    }

    @Test
    public void shouldReturnTwoWindingsTransformerMapData() throws Exception {
        succeedingTestForElement("2-windings-transformers", NETWORK_UUID, null, null, "NGEN_NHV1", resourceToString("/2-windings-transformer-map-data.json"));
        succeedingTestForElement("2-windings-transformers", NETWORK_UUID, VARIANT_ID, null, "NGEN_NHV1", resourceToString("/2-windings-transformer-map-data.json"));
    }

    @Test
    public void shouldReturnAnErrorinsteadOfTwoWindingsTransformerMapData() throws Exception {
        failingTestForElement("2-windings-transformers", NETWORK_UUID, null, null, "NOT_EXISTING_2WT");
        failingTestForElement("2-windings-transformers", NETWORK_UUID, VARIANT_ID, null, "NOT_EXISTING_2WT");
    }

    @Test
    public void shouldReturnThreeWindingsTransformersMapData() throws Exception {
        succeedingTestForList("3-windings-transformers", NETWORK_UUID, null, null, resourceToString("/3-windings-transformers-map-data.json"));
        succeedingTestForList("3-windings-transformers", NETWORK_UUID, VARIANT_ID, null, resourceToString("/3-windings-transformers-map-data.json"));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfThreeWindingsTransformersMapData() throws Exception {
        failingTestForList("3-windings-transformers", NOT_FOUND_NETWORK_ID, null, null);
        failingTestForList("3-windings-transformers", NETWORK_UUID, VARIANT_ID_NOT_FOUND, null);
    }

    @Test
    public void shouldReturnThreeWindingsTransformersMapDataFromIds() throws Exception {
        succeedingTestForList("3-windings-transformers", NETWORK_UUID, null, List.of("P3"), "[]");
        succeedingTestForList("3-windings-transformers", NETWORK_UUID, VARIANT_ID, List.of("P3"), "[]");
    }

    @Test
    public void shouldReturnAnErrorInsteadOfThreeWindingsTransformersMapDataFromIds() throws Exception {
        failingTestForList("3-windings-transformers", NOT_FOUND_NETWORK_ID, null, List.of("P1"));
        failingTestForList("3-windings-transformers", NETWORK_UUID, VARIANT_ID_NOT_FOUND, List.of("P1"));
    }

    @Test
    public void shouldReturnAllMapData() throws Exception {
        succeedingTestForList("all", NETWORK_UUID, null, null, resourceToString("/all-map-data.json"));
        succeedingTestForList("all", NETWORK_UUID, VARIANT_ID, null, resourceToString("/all-map-data-in-variant.json"));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfAllMapData() throws Exception {
        failingTestForList("all", NOT_FOUND_NETWORK_ID, null, null);
        failingTestForList("all", NETWORK_UUID, VARIANT_ID_NOT_FOUND, null);
    }

    @Test
    public void shouldReturnAllMapDataFromIds() throws Exception {
        succeedingTestForList("all", NETWORK_UUID, null, List.of("P3"), resourceToString("/partial-all-map-data.json"));
        succeedingTestForList("all", NETWORK_UUID, VARIANT_ID, List.of("P3"), resourceToString("/partial-all-map-data-in-variant.json"));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfAllMapDataFromIds() throws Exception {
        failingTestForList("all", NOT_FOUND_NETWORK_ID, null, List.of("P1"));
        failingTestForList("all", NETWORK_UUID, VARIANT_ID_NOT_FOUND, List.of("P1"));
    }

    @Test
    public void shouldReturnBatteriesMapData() throws Exception {
        succeedingTestForList("batteries", NETWORK_UUID, null, null, resourceToString("/batteries-map-data.json"));
        succeedingTestForList("batteries", NETWORK_UUID, VARIANT_ID, null, resourceToString("/batteries-map-data.json"));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfBatteriesMapData() throws Exception {
        failingTestForList("batteries", NOT_FOUND_NETWORK_ID, null, null);
        failingTestForList("batteries", NETWORK_UUID, VARIANT_ID_NOT_FOUND, null);
    }

    @Test
    public void shouldReturnBatteriesMapDataFromIds() throws Exception {
        succeedingTestForList("batteries", NETWORK_UUID, null, List.of("P1"), resourceToString("/partial-batteries-map-data.json"));
        succeedingTestForList("batteries", NETWORK_UUID, VARIANT_ID, List.of("P1"), resourceToString("/partial-batteries-map-data.json"));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfBatteriesMapDataFromIds() throws Exception {
        failingTestForList("batteries", NOT_FOUND_NETWORK_ID, null, List.of("P1", "P2"));
        failingTestForList("batteries", NETWORK_UUID, VARIANT_ID_NOT_FOUND, List.of("P1", "P2"));
    }

    @Test
    public void shouldReturnDanglingLinesMapData() throws Exception {
        succeedingTestForList("dangling-lines", NETWORK_UUID, null, null, resourceToString("/dangling-lines-map-data.json"));
        succeedingTestForList("dangling-lines", NETWORK_UUID, VARIANT_ID, null, resourceToString("/dangling-lines-map-data.json"));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfDanglingLinesMapData() throws Exception {
        failingTestForList("dangling-lines", NOT_FOUND_NETWORK_ID, null, null);
        failingTestForList("dangling-lines", NETWORK_UUID, VARIANT_ID_NOT_FOUND, null);
    }

    @Test
    public void shouldReturnDanglingLinesMapDataFromIds() throws Exception {
        succeedingTestForList("dangling-lines", NETWORK_UUID, null, List.of("P2"), "[]");
        succeedingTestForList("dangling-lines", NETWORK_UUID, VARIANT_ID, List.of("P2"), "[]");
    }

    @Test
    public void shouldReturnAnErrorInsteadOfDanglingLinesMapDataFromIds() throws Exception {
        failingTestForList("dangling-lines", NOT_FOUND_NETWORK_ID, null, List.of("P1", "P2"));
        failingTestForList("dangling-lines", NETWORK_UUID, VARIANT_ID_NOT_FOUND, List.of("P1", "P2"));
    }

    @Test
    public void shouldReturnLoadsMapData() throws Exception {
        succeedingTestForList("loads", NETWORK_UUID, null, null, resourceToString("/loads-map-data.json"));
        succeedingTestForList("loads", NETWORK_UUID, VARIANT_ID, null, resourceToString("/loads-map-data.json"));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfLoadsMapData() throws Exception {
        failingTestForList("loads", NOT_FOUND_NETWORK_ID, null, null);
        failingTestForList("loads", NETWORK_UUID, VARIANT_ID_NOT_FOUND, null);
    }

    @Test
    public void shouldReturnLoadsMapDataFromIds() throws Exception {
        succeedingTestForList("loads", NETWORK_UUID, null, List.of("P2"), resourceToString("/partial-loads-map-data.json"));
        succeedingTestForList("loads", NETWORK_UUID, VARIANT_ID, List.of("P2"), resourceToString("/partial-loads-map-data.json"));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfLoadsMapDataFromIds() throws Exception {
        failingTestForList("loads", NOT_FOUND_NETWORK_ID, null, List.of("P1"));
        failingTestForList("loads", NETWORK_UUID, VARIANT_ID_NOT_FOUND, List.of("P1"));
    }

    @Test
    public void shouldReturnLoadMapData() throws Exception {
        succeedingTestForElement("loads", NETWORK_UUID, null, null, "LOAD", resourceToString("/load-map-data.json"));
        succeedingTestForElement("loads", NETWORK_UUID, VARIANT_ID, null, "LOAD", resourceToString("/load-map-data.json"));
    }

    @Test
    public void shouldReturnNameOrEmpty() throws Exception {
        succeedingTestForElementNameOrEmpty("loads", NETWORK_UUID, null, null, "LOAD_WITH_NULL_NAME", resourceToString("/load-map-data-test-null-name.json"));
        succeedingTestForElementNameOrEmpty("loads", NETWORK_UUID, null, null, "LOAD_ID", resourceToString("/load-map-data-not-null-name.json"));
    }

    @Test
    public void shouldReturnAnErrorinsteadOfLoadMapData() throws Exception {
        failingTestForElement("loads", NETWORK_UUID, null, null, "LOAD2ef");
        failingTestForElement("loads", NETWORK_UUID, VARIANT_ID, null, "LOAD2dqs");
    }

    @Test
    public void shouldReturnShuntCompensatorsMapData() throws Exception {
        succeedingTestForList("shunt-compensators", NETWORK_UUID, null, null, resourceToString("/shunt-compensators-map-data.json"));
        succeedingTestForList("shunt-compensators", NETWORK_UUID, VARIANT_ID, null, resourceToString("/shunt-compensators-map-data-in-variant.json"));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfShuntCompensatorsMapData() throws Exception {
        failingTestForList("shunt-compensators", NOT_FOUND_NETWORK_ID, null, null);
        failingTestForList("shunt-compensators", NETWORK_UUID, VARIANT_ID_NOT_FOUND, null);
    }

    @Test
    public void shouldReturnShuntCompensatorsMapDataFromIds() throws Exception {
        succeedingTestForList("shunt-compensators", NETWORK_UUID, null, List.of("P1"), resourceToString("/partial-shunt-compensators-map-data.json"));
        succeedingTestForList("shunt-compensators", NETWORK_UUID, VARIANT_ID, List.of("P1", "P3"), resourceToString("/partial-shunt-compensators-map-data-in-variant.json"));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfShuntCompensatorsMapDataFromIds() throws Exception {
        failingTestForList("shunt-compensators", NOT_FOUND_NETWORK_ID, null, List.of("P1"));
        failingTestForList("shunt-compensators", NETWORK_UUID, VARIANT_ID_NOT_FOUND, List.of("P1"));
    }

    @Test
    public void shouldReturnShuntCompensatorMapData() throws Exception {
        succeedingTestForElement("shunt-compensators", NETWORK_UUID, null, null, "SHUNT1", resourceToString("/shunt-compensator-map-data.json"));
        succeedingTestForElement("shunt-compensators", NETWORK_UUID, VARIANT_ID, null, "SHUNT1", resourceToString("/shunt-compensator-map-data.json"));
    }

    @Test
    public void shouldReturnAnErrorinsteadOfShuntCompensatorMapData() throws Exception {
        failingTestForElement("shunt-compensators", NETWORK_UUID, null, null, "NOT_EXISTING_SC");
        failingTestForElement("shunt-compensators", NETWORK_UUID, VARIANT_ID, null, "NOT_EXISTING_SC");
    }

    @Test
    public void shouldReturnStaticVarCompensatorsMapData() throws Exception {
        succeedingTestForList("static-var-compensators", NETWORK_UUID, null, null, resourceToString("/static-var-compensators-map-data.json"));
        succeedingTestForList("static-var-compensators", NETWORK_UUID, VARIANT_ID, null, resourceToString("/static-var-compensators-map-data.json"));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfStaticVarCompensatorsMapData() throws Exception {
        failingTestForList("static-var-compensators", NOT_FOUND_NETWORK_ID, null, null);
        failingTestForList("static-var-compensators", NETWORK_UUID, VARIANT_ID_NOT_FOUND, null);
    }

    @Test
    public void shouldReturnStaticVarCompensatorsMapDataFromIds() throws Exception {
        succeedingTestForList("static-var-compensators", NETWORK_UUID, null, List.of("P1"), resourceToString("/partial-static-var-compensators-map-data.json"));
        succeedingTestForList("static-var-compensators", NETWORK_UUID, VARIANT_ID, List.of("P1"), resourceToString("/partial-static-var-compensators-map-data.json"));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfStaticVarCompensatorsMapDataFromIds() throws Exception {
        failingTestForList("static-var-compensators", NOT_FOUND_NETWORK_ID, null, List.of("P1"));
        failingTestForList("static-var-compensators", NETWORK_UUID, VARIANT_ID_NOT_FOUND, List.of("P1"));
    }

    @Test
    public void shouldReturnLccConverterStationsMapData() throws Exception {
        succeedingTestForList("lcc-converter-stations", NETWORK_UUID, null, null, resourceToString("/lcc-converter-stations-map-data.json"));
        succeedingTestForList("lcc-converter-stations", NETWORK_UUID, VARIANT_ID, null, resourceToString("/lcc-converter-stations-map-data.json"));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfLccConverterStationsMapData() throws Exception {
        failingTestForList("lcc-converter-stations", NOT_FOUND_NETWORK_ID, null, null);
        failingTestForList("lcc-converter-stations", NETWORK_UUID, VARIANT_ID_NOT_FOUND, null);
    }

    @Test
    public void shouldReturnLccConverterStationsMapDataFromIds() throws Exception {
        succeedingTestForList("lcc-converter-stations", NETWORK_UUID, null, List.of("P1"), resourceToString("/partial-lcc-converter-stations-map-data.json"));
        succeedingTestForList("lcc-converter-stations", NETWORK_UUID, VARIANT_ID, List.of("P1"), resourceToString("/partial-lcc-converter-stations-map-data.json"));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfLccConverterStationsMapDataFromIds() throws Exception {
        failingTestForList("lcc-converter-stations", NOT_FOUND_NETWORK_ID, null, List.of("P1"));
        failingTestForList("lcc-converter-stations", NETWORK_UUID, VARIANT_ID_NOT_FOUND, List.of("P1"));
    }

    @Test
    public void shouldReturnVscConverterStationsMapData() throws Exception {
        succeedingTestForList("vsc-converter-stations", NETWORK_UUID, null, null, resourceToString("/vsc-converter-stations-map-data.json"));
        succeedingTestForList("vsc-converter-stations", NETWORK_UUID, VARIANT_ID, null, resourceToString("/vsc-converter-stations-map-data.json"));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfVscConverterStationsMapData() throws Exception {
        failingTestForList("vsc-converter-stations", NOT_FOUND_NETWORK_ID, null, null);
        failingTestForList("vsc-converter-stations", NETWORK_UUID, VARIANT_ID_NOT_FOUND, null);
    }

    @Test
    public void shouldReturnVscConverterStationsMapDataFromIds() throws Exception {
        succeedingTestForList("vsc-converter-stations", NETWORK_UUID, null, List.of("P1"), resourceToString("/partial-vsc-converter-stations-map-data.json"));
        succeedingTestForList("vsc-converter-stations", NETWORK_UUID, VARIANT_ID, List.of("P1"), resourceToString("/partial-vsc-converter-stations-map-data.json"));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfVscConverterStationsMapDataFromIds() throws Exception {
        failingTestForList("vsc-converter-stations", NOT_FOUND_NETWORK_ID, null, List.of("P1"));
        failingTestForList("vsc-converter-stations", NETWORK_UUID, VARIANT_ID_NOT_FOUND, List.of("P1"));
    }

    @Test
    public void shouldReturnHvdcLinesMapData() throws Exception {
        succeedingTestForList("hvdc-lines", NETWORK_UUID, null, null, resourceToString("/hvdc-lines-map-data.json"));
        succeedingTestForList("hvdc-lines", NETWORK_UUID, VARIANT_ID, null, resourceToString("/hvdc-lines-map-data.json"));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfHvdcLinesMapData() throws Exception {
        failingTestForList("hvdc-lines", NOT_FOUND_NETWORK_ID, null, null);
        failingTestForList("hvdc-lines", NETWORK_UUID, VARIANT_ID_NOT_FOUND, null);
    }

    @Test
    public void shouldReturnHvdcLinesMapDataFromIds() throws Exception {
        succeedingTestForList("hvdc-lines", NETWORK_UUID, null, List.of("P3"), "[]");
        succeedingTestForList("hvdc-lines", NETWORK_UUID, VARIANT_ID, List.of("P3"), "[]");
    }

    @Test
    public void shouldReturnAnErrorInsteadOfHvdcLinesMapDataFromIds() throws Exception {
        failingTestForList("hvdc-lines", NOT_FOUND_NETWORK_ID, null, List.of("P1", "P2"));
        failingTestForList("hvdc-lines", NETWORK_UUID, VARIANT_ID_NOT_FOUND, List.of("P1", "P2"));
    }

    @Test
    public void shouldReturnVoltageLevelsMapData() throws Exception {
        succeedingVoltageLevelsTest(NETWORK_UUID, null, null, resourceToString("/voltage-levels-map-data.json"));
        succeedingVoltageLevelsTest(NETWORK_UUID, VARIANT_ID, null, resourceToString("/voltage-levels-map-data.json"));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfVoltageLevelsMapData() throws Exception {
        failingVoltageLevelsTest(NOT_FOUND_NETWORK_ID, null, null);
        failingVoltageLevelsTest(NETWORK_UUID, VARIANT_ID_NOT_FOUND, null);
    }

    @Test
    public void shouldReturnVoltageLevelsMapDataFromIds() throws Exception {
        succeedingVoltageLevelsTest(NETWORK_UUID, null, List.of("P3"), resourceToString("/partial-voltage-levels-map-data.json"));
        succeedingVoltageLevelsTest(NETWORK_UUID, VARIANT_ID, List.of("P3"), resourceToString("/partial-voltage-levels-map-data.json"));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfVoltageLevelsMapDataFromIds() throws Exception {
        failingVoltageLevelsTest(NOT_FOUND_NETWORK_ID, null, List.of("P1", "P2"));
        failingVoltageLevelsTest(NETWORK_UUID, VARIANT_ID_NOT_FOUND, List.of("P1", "P2"));
    }

    @Test
    public void shouldReturnVotlageLevelMapData() throws Exception {
        succeedingTestForElement("voltage-levels", NETWORK_UUID, null, null, "VLGEN4", resourceToString("/voltage-level-map-data.json"));
        succeedingTestForElement("voltage-levels", NETWORK_UUID, VARIANT_ID, null, "VLGEN4", resourceToString("/voltage-level-map-data.json"));
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
        succeedingTestForList("voltage-levels-equipments", NETWORK_UUID, null, null, resourceToString("/voltage-levels-equipments-map-data.json"));
        succeedingTestForList("voltage-levels-equipments", NETWORK_UUID, VARIANT_ID, null, resourceToString("/voltage-levels-equipments-map-data-in-variant.json"));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfVoltageLevelsEquipmentsMapData() throws Exception {
        failingTestForList("voltage-levels-equipments", NOT_FOUND_NETWORK_ID, null, null);
        failingTestForList("voltage-levels-equipments", NETWORK_UUID, VARIANT_ID_NOT_FOUND, null);
    }

    @Test
    public void shouldReturnMapSubstationsData() throws Exception {
        succeedingTestForList("map-substations", NETWORK_UUID, null, null, resourceToString("/map-substations-data.json"));
        succeedingTestForList("map-substations", NETWORK_UUID, VARIANT_ID, null, resourceToString("/map-substations-data.json"));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfMapSubstationsData() throws Exception {
        failingTestForList("map-substations", NOT_FOUND_NETWORK_ID, null, null);
        failingTestForList("map-substations", NETWORK_UUID, VARIANT_ID_NOT_FOUND, null);
    }

    @Test
    public void shouldReturnMapSubstationsDataFromSubstationId() throws Exception {
        succeedingTestForList("map-substations", NETWORK_UUID, null, List.of("P1"), resourceToString("/partial-map-substations-data.json"));
        succeedingTestForList("map-substations", NETWORK_UUID, VARIANT_ID, List.of("P1"), resourceToString("/partial-map-substations-data.json"));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfMapSubstationsDataFromSubstationId() throws Exception {
        failingTestForList("map-substations", NOT_FOUND_NETWORK_ID, null, List.of("P1"));
        failingTestForList("map-substations", NETWORK_UUID, VARIANT_ID_NOT_FOUND, List.of("P1"));
    }

    @Test
    public void shouldReturnMapLinesData() throws Exception {
        succeedingTestForList("map-lines", NETWORK_UUID, null, null, resourceToString("/map-lines-data.json"));
        succeedingTestForList("map-lines", NETWORK_UUID, VARIANT_ID, null, resourceToString("/map-lines-data.json"));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfMapLinesData() throws Exception {
        failingTestForList("map-lines", NOT_FOUND_NETWORK_ID, null, null);
        failingTestForList("map-lines", NETWORK_UUID, VARIANT_ID_NOT_FOUND, null);
    }

    @Test
    public void shouldReturnMapLinesDataFromSubstationId() throws Exception {
        succeedingTestForList("map-lines", NETWORK_UUID, null, List.of("P1"), resourceToString("/partial-map-lines-data.json"));
        succeedingTestForList("map-lines", NETWORK_UUID, VARIANT_ID, List.of("P1"), resourceToString("/partial-map-lines-data.json"));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfMapLinesDataFromSubstationId() throws Exception {
        failingTestForList("map-lines", NOT_FOUND_NETWORK_ID, null, List.of("P1"));
        failingTestForList("map-lines", NETWORK_UUID, VARIANT_ID_NOT_FOUND, List.of("P1"));
    }
}
