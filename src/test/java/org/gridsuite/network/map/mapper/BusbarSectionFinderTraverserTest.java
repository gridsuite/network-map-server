package org.gridsuite.network.map.mapper;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.BusbarSectionPositionAdder;
import org.gridsuite.network.map.dto.definition.extension.BusbarSectionFinderTraverser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Properties;

import static org.gridsuite.network.map.NetworkMapControllerTest.createSwitch;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Ghazwa Rehili <ghazwa.rehili at rte-france.com>
 */

@SpringBootTest
class BusbarSectionFinderTraverserTest {

    private Network network;

    @BeforeEach
    void setUp() {
        network = NetworkFactory.findDefault().createNetwork("sim1", "test");
        Substation p4 = network.newSubstation()
                .setId("P4")
                .setCountry(Country.FR)
                .setTso("RTE")
                .setGeographicalTags("A")
                .add();
        VoltageLevel vlgen1 = p4.newVoltageLevel()
                .setId("VLGEN1")
                .setNominalV(24.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        vlgen1.getNodeBreakerView().newBusbarSection()
                .setId("BUS1")
                .setName("BUS1")
                .setNode(1)
                .add();

        VoltageLevel vlgen = p4.newVoltageLevel()
                .setId("VLGEN")
                .setNominalV(24.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        vlgen.getNodeBreakerView().newBusbarSection()
                .setId("BUS1_1")
                .setName("BUS1_1")
                .setNode(1)
                .add();
        vlgen.getNodeBreakerView().newBusbarSection()
                .setId("BUS2_1")
                .setName("BUS2_1")
                .setNode(2)
                .add();
        vlgen.getNodeBreakerView().newBusbarSection()
                .setId("BUS1_2")
                .setName("BUS1_2")
                .setNode(3)
                .add();
        vlgen.getNodeBreakerView().newBusbarSection()
                .setId("BUS2_2")
                .setName("BUS2_2")
                .setNode(4)
                .add();

        vlgen.getNodeBreakerView()
                .getBusbarSection("BUS1_1")
                .newExtension(BusbarSectionPositionAdder.class)
                .withBusbarIndex(1)
                .withSectionIndex(1)
                .add();
        vlgen.getNodeBreakerView()
                .getBusbarSection("BUS1_2")
                .newExtension(BusbarSectionPositionAdder.class)
                .withBusbarIndex(1)
                .withSectionIndex(2)
                .add();
        vlgen.getNodeBreakerView()
                .getBusbarSection("BUS2_1")
                .newExtension(BusbarSectionPositionAdder.class)
                .withBusbarIndex(2)
                .withSectionIndex(1)
                .add();
        vlgen.getNodeBreakerView()
                .getBusbarSection("BUS2_2")
                .newExtension(BusbarSectionPositionAdder.class)
                .withBusbarIndex(2)
                .withSectionIndex(2)
                .add();

        //Fork topology
        createSwitch(vlgen, "DISC_BUS1.1_BUS1.2", SwitchKind.DISCONNECTOR, false, 1, 3);
        createSwitch(vlgen, "DISC_BUS2.1_BUS2.2", SwitchKind.DISCONNECTOR, true, 2, 4);
        createSwitch(vlgen, "DISC_BUS1_2", SwitchKind.DISCONNECTOR, true, 3, 5);
        createSwitch(vlgen, "DISC_BUS2_2", SwitchKind.DISCONNECTOR, true, 4, 5);
        createSwitch(vlgen, "BRK_FORK", SwitchKind.BREAKER, true, 5, 6);
        createSwitch(vlgen, "DISC_LINE_1_2", SwitchKind.DISCONNECTOR, false, 6, 7);
        createSwitch(vlgen, "DISC_LINE_2_2", SwitchKind.DISCONNECTOR, false, 6, 8);
        createSwitch(vlgen, "BRK_LINE_1_2", SwitchKind.BREAKER, false, 7, 10);
        createSwitch(vlgen, "BRK_LINE_2_2", SwitchKind.BREAKER, false, 8, 9);
        network.newLine()
                .setId("LINE_1_2")
                .setName("LINE_1_2")
                .setVoltageLevel1("VLGEN")
                .setNode1(10)
                .setVoltageLevel2("VLGEN1")
                .setNode2(11)
                .setR(2.0)
                .setX(25.0)
                .setG1(0.0)
                .setB1(300E-6 / 2)
                .setG2(0.0)
                .setB2(300E-6 / 2)
                .add();

        network.newLine()
                .setId("LINE_2_2")
                .setName("LINE_2_2")
                .setVoltageLevel1("VLGEN")
                .setNode1(9)
                .setVoltageLevel2("VLGEN1")
                .setNode2(21)
                .setR(2.0)
                .setX(25.0)
                .setG1(0.0)
                .setB1(300E-6 / 2)
                .setG2(0.0)
                .setB2(300E-6 / 2)
                .add();

        // BYPASS topology
        createSwitch(vlgen, "DISC_BUS1_1", SwitchKind.DISCONNECTOR, true, 1, 15);
        createSwitch(vlgen, "DISC_BUS2_1", SwitchKind.DISCONNECTOR, true, 2, 18);
        createSwitch(vlgen, "DISC_BYPASS", SwitchKind.DISCONNECTOR, true, 15, 18);
        createSwitch(vlgen, "DISC_LINE_1_1", SwitchKind.DISCONNECTOR, true, 15, 11);
        createSwitch(vlgen, "DISC_LINE_2_1", SwitchKind.DISCONNECTOR, true, 18, 21);
        createSwitch(vlgen, "BRK_LINE_1_1", SwitchKind.BREAKER, true, 11, 12);
        createSwitch(vlgen, "BRK_LINE_2_1", SwitchKind.BREAKER, true, 21, 23);
        network.newLine()
                .setId("LINE_1_1")
                .setName("LINE_1_1")
                .setVoltageLevel1("VLGEN")
                .setNode1(12)
                .setVoltageLevel2("VLGEN1")
                .setNode2(3)
                .setR(2.0)
                .setX(25.0)
                .setG1(0.0)
                .setB1(300E-6 / 2)
                .setG2(0.0)
                .setB2(300E-6 / 2)
                .add();

        network.newLine()
                .setId("LINE_2_1")
                .setName("LINE_2_1")
                .setVoltageLevel1("VLGEN")
                .setNode1(23)
                .setVoltageLevel2("VLGEN1")
                .setNode2(41)
                .setR(2.0)
                .setX(25.0)
                .setG1(0.0)
                .setB1(300E-6 / 2)
                .setG2(0.0)
                .setB2(300E-6 / 2)
                .add();

        network.getVariantManager().setWorkingVariant(VariantManagerConstants.INITIAL_VARIANT_ID);
        Properties properties = new Properties();
        network.write("XIIDM", properties, "/tmp", "BusbarSectionFinderTraverserTest");
    }

    @Test
    void testWithOpenSwitch() {
        Line line12 = network.getLine("LINE_1_2");
        Line line22 = network.getLine("LINE_2_2");
        BusbarSectionFinderTraverser.BusbarSectionResult result12 = BusbarSectionFinderTraverser.getBusbarSectionResult(line12.getTerminal1());
        BusbarSectionFinderTraverser.BusbarSectionResult result22 = BusbarSectionFinderTraverser.getBusbarSectionResult(line22.getTerminal1());
        // Both lines must find the same busbar
        assertNotNull(result12);
        assertEquals("BUS1_2", result12.busbarSectionId());
        assertNotNull(result22);
        assertEquals("BUS1_2", result22.busbarSectionId());
        // Check depth and last switch
        assertEquals(4, result12.depth());
        assertEquals(4, result22.depth());
        assertEquals("DISC_BUS1_2", result12.lastSwitch().id());
        assertEquals("DISC_BUS1_2", result22.lastSwitch().id());
        assertTrue(result12.lastSwitch().isOpen());
        assertTrue(result22.lastSwitch().isOpen());
    }

    @Test
    void testWithClosedLastSwitch() {
        network.getSwitch("BRK_LINE_1_1").setOpen(false);
        network.getSwitch("DISC_LINE_1_1").setOpen(false);
        network.getSwitch("DISC_BUS1_1").setOpen(false);
        Line line11 = network.getLine("LINE_1_1");
        Line line21 = network.getLine("LINE_2_1");
        BusbarSectionFinderTraverser.BusbarSectionResult result11 = BusbarSectionFinderTraverser.getBusbarSectionResult(line11.getTerminal1());
        BusbarSectionFinderTraverser.BusbarSectionResult result21 = BusbarSectionFinderTraverser.getBusbarSectionResult(line21.getTerminal1());
        assertNotNull(result11);
        assertEquals("BUS1_1", result11.busbarSectionId());
        assertNotNull(result21);
        assertEquals("BUS1_1", result21.busbarSectionId());
        assertEquals(3, result11.depth());
        assertEquals(4, result21.depth());
        assertEquals("DISC_BUS1_1", result11.lastSwitch().id());
        assertEquals("DISC_BUS1_1", result21.lastSwitch().id());
        assertFalse(result11.lastSwitch().isOpen());
        assertFalse(result21.lastSwitch().isOpen());
    }

    @Test
    void testWithAllClosedSwitch() {
        network.getSwitch("BRK_LINE_1_1").setOpen(false);
        network.getSwitch("DISC_LINE_1_1").setOpen(false);
        network.getSwitch("DISC_BUS1_1").setOpen(false);
        network.getSwitch("DISC_BUS2_1").setOpen(false);
        network.getSwitch("BRK_LINE_2_1").setOpen(false);
        network.getSwitch("DISC_LINE_2_1").setOpen(false);
        Line line11 = network.getLine("LINE_1_1");
        Line line21 = network.getLine("LINE_2_1");
        BusbarSectionFinderTraverser.BusbarSectionResult result11 = BusbarSectionFinderTraverser.getBusbarSectionResult(line11.getTerminal1());
        BusbarSectionFinderTraverser.BusbarSectionResult result21 = BusbarSectionFinderTraverser.getBusbarSectionResult(line21.getTerminal1());
        assertNotNull(result11);
        assertEquals("BUS1_1", result11.busbarSectionId());
        assertNotNull(result21);
        assertEquals("BUS2_1", result21.busbarSectionId());
        assertEquals(3, result11.depth());
        assertEquals(3, result21.depth());
        assertEquals("DISC_BUS1_1", result11.lastSwitch().id());
        assertEquals("DISC_BUS2_1", result21.lastSwitch().id());
        assertTrue(result11.allClosedSwitch());
        assertTrue(result21.allClosedSwitch());
    }
}
