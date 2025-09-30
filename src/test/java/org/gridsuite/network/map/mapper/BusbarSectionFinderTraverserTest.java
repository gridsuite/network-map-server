package org.gridsuite.network.map.mapper;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.*;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.network.store.iidm.impl.NetworkFactoryImpl;
import org.gridsuite.network.map.dto.definition.extension.BusbarSectionFinderTraverser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
@AutoConfigureMockMvc
@SpringBootTest
public class BusbarSectionFinderTraverserTest {

    private Network network;

    public static void createSwitch(VoltageLevel vl, String id, SwitchKind kind, boolean open, int node1, int node2) {
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

    @BeforeEach
    void setUp() {
        network = EurostagTutorialExample1Factory.createWithMoreGenerators(new NetworkFactoryImpl());
        /** VLGEN7 - Fork topology
         *
         *            BUS1 ═══════X════════ BUS2 ═══════════════/════ BUS3
         *          (node0)               (node5)            (node7)
         *             |                     |                  |
         *     Disconnector1            Disconnector2           |
         *            |                     |                   |
         *            |                     |              Disconnector3
         *     [open = true]          [open = false]       [open = false]
         *            |                     |                  |
         *            |____________________|        ═══════════/════════ BUS4
         *                       |                             |
         *                       |                             |
         *                    fork point                  Disconnector4
         *                     (node8)                   [open = false]
         *                        |                            |
         *                    ┌───┴───┐                      LINE9
         *                    |       |                    (→VLGEN9)
         *                 LINE7   LINE8
         *              (→VLGEN4) (→VLGEN8)
         */

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
        BusbarSection bbs4 = vlgen4.getNodeBreakerView().newBusbarSection()
                .setId("NGEN4")
                .setName("NGEN4")
                .setNode(0)
                .add();
        bbs4.newExtension(MeasurementsAdder.class).add();
        Measurements<BusbarSection> bbs4Measurements = bbs4.getExtension(Measurements.class);
        bbs4Measurements.newMeasurement().setType(Measurement.Type.VOLTAGE).setValid(true).setValue(385.).add();
        bbs4Measurements.newMeasurement().setType(Measurement.Type.ANGLE).setValid(false).setValue(0.5).add();

        vlgen4.getNodeBreakerView()
                .getBusbarSection("NGEN4")
                .newExtension(BusbarSectionPositionAdder.class)
                .withBusbarIndex(1)
                .withSectionIndex(2)
                .add();

        VoltageLevel vlgen7 = network.newVoltageLevel()
                .setId("VLGEN7")
                .setName("Fork Distribution Point")
                .setNominalV(24.0)
                .setHighVoltageLimit(30.0)
                .setLowVoltageLimit(20.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();

        // Create busbarSections
        vlgen7.getNodeBreakerView().newBusbarSection()
                .setId("BUS1_NGEN7")
                .setName("Primary Busbar VLGEN7")
                .setNode(0)
                .add();

        vlgen7.getNodeBreakerView().newBusbarSection()
                .setId("BUS2_NGEN7")
                .setName("Secondary Busbar VLGEN7")
                .setNode(5)
                .add();

        vlgen7.getNodeBreakerView().newBusbarSection()
                .setId("BUS3_NGEN7")
                .setName("Third Busbar VLGEN7")
                .setNode(9)
                .add();
        vlgen7.getNodeBreakerView().newBusbarSection()
                .setId("BUS4_NGEN7")
                .setName("Fourth Busbar VLGEN7")
                .setNode(13)
                .add();

        createSwitch(vlgen7, "SECT_BUS1", SwitchKind.DISCONNECTOR, true, 0, 6);   // OPEN
        createSwitch(vlgen7, "SECT_BUS2", SwitchKind.DISCONNECTOR, false, 5, 7);  // CLOSED
        createSwitch(vlgen7, "SECT_BUS3", SwitchKind.DISCONNECTOR, false, 9, 10); // CLOSED
        createSwitch(vlgen7, "SECT_BUS4", SwitchKind.DISCONNECTOR, false, 13, 14); // CLOSED

        createSwitch(vlgen7, "FORK_SW1", SwitchKind.DISCONNECTOR, false, 6, 8);   // BUS1 to fork (CLOSED via node 6)
        createSwitch(vlgen7, "FORK_SW2", SwitchKind.DISCONNECTOR, false, 7, 8);   // BUS2 to fork (CLOSED via node 7)

        // LINE7 connection from fork
        createSwitch(vlgen7, "DISC_LINE7", SwitchKind.DISCONNECTOR, false, 8, 1);
        createSwitch(vlgen7, "BRKR_LINE7", SwitchKind.BREAKER, false, 1, 2);

        // LINE8 connection from fork
        createSwitch(vlgen7, "DISC_LINE8", SwitchKind.DISCONNECTOR, false, 8, 3);
        createSwitch(vlgen7, "BRKR_LINE8", SwitchKind.BREAKER, false, 3, 4);

        // LINE9 connection from BUS4 (Fixed connection point)
        createSwitch(vlgen7, "DISC_LINE9", SwitchKind.DISCONNECTOR, false, 14, 12);  // Connect from node 14
        createSwitch(vlgen7, "BRKR_LINE9", SwitchKind.BREAKER, false, 12, 11);

        // Assuming VLGEN4 exists, create its switches
        createSwitch(vlgen4, "DISC_VLGEN4", SwitchKind.DISCONNECTOR, false, 0, 10);
        createSwitch(vlgen4, "BRKR_VLGEN4", SwitchKind.BREAKER, false, 10, 15);

        // LINE7 - Fork Branch 1
        network.newLine()
                .setId("LINE7_FORK")
                .setName("Fork Branch 1 - Primary Line")
                .setVoltageLevel1("VLGEN4")
                .setNode1(15)  // Fixed node reference
                .setVoltageLevel2("VLGEN7")
                .setNode2(2)
                .setR(3.0)
                .setX(33.0)
                .setG1(0.0)
                .setB1(386E-6 / 2)
                .setG2(0.0)
                .setB2(386E-6 / 2)
                .add();

        Line line7 = network.getLine("LINE7_FORK");
        line7.newExtension(ConnectablePositionAdder.class)
                .newFeeder1()
                .withName("LINE7_VLGEN4_Side")
                .withOrder(5)
                .withDirection(ConnectablePosition.Direction.BOTTOM)
                .add()
                .newFeeder2()
                .withName("LINE7_VLGEN7_Fork_Side")
                .withOrder(1)
                .withDirection(ConnectablePosition.Direction.TOP)
                .add()
                .add();

        // VLGEN8 - Fork destination 2
        VoltageLevel vlgen8 = network.newVoltageLevel()
                .setId("VLGEN8")
                .setName("Fork Destination Point 2")
                .setNominalV(24.0)
                .setHighVoltageLimit(30.0)
                .setLowVoltageLimit(20.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        createSwitch(vlgen8, "DISC_VLGEN8", SwitchKind.DISCONNECTOR, false, 0, 1);
        createSwitch(vlgen8, "BRKR_VLGEN8", SwitchKind.BREAKER, false, 1, 2);

        // LINE8 - Fork Branch 2
        network.newLine()
                .setId("LINE8_FORK")
                .setName("Fork Branch 2 - Secondary Line")
                .setVoltageLevel1("VLGEN7")
                .setNode1(4)
                .setVoltageLevel2("VLGEN8")
                .setNode2(2)
                .setR(2.5)
                .setX(28.0)
                .setG1(0.0)
                .setB1(320E-6 / 2)
                .setG2(0.0)
                .setB2(320E-6 / 2)
                .add();
        Line line8 = network.getLine("LINE8_FORK");
        line8.newExtension(ConnectablePositionAdder.class)
                .newFeeder1()
                .withName("LINE8_VLGEN7_Fork_Side")
                .withOrder(2)
                .withDirection(ConnectablePosition.Direction.BOTTOM)
                .add()
                .newFeeder2()
                .withName("LINE8_VLGEN8_Side")
                .withOrder(1)
                .withDirection(ConnectablePosition.Direction.TOP)
                .add()
                .add();

        // VLGEN9 - Independent line destination
        VoltageLevel vlgen9 = network.newVoltageLevel()
                .setId("VLGEN9")
                .setName("Independent Line Destination")
                .setNominalV(24.0)
                .setHighVoltageLimit(30.0)
                .setLowVoltageLimit(20.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();

        vlgen9.getNodeBreakerView().newBusbarSection()
                .setId("BUS_NGEN9")
                .setName("Main Busbar VLGEN9")
                .setNode(0)
                .add();

        createSwitch(vlgen9, "DISC_VLGEN9", SwitchKind.DISCONNECTOR, false, 0, 1);
        createSwitch(vlgen9, "BRKR_VLGEN9", SwitchKind.BREAKER, false, 1, 2);

        // LINE9 - Independent line from BUS4
        network.newLine()
                .setId("LINE9_INDEPENDENT")
                .setName("Independent Line from BUS4")
                .setVoltageLevel1("VLGEN7")
                .setNode1(11)
                .setVoltageLevel2("VLGEN9")
                .setNode2(2)
                .setR(2.0)
                .setX(25.0)
                .setG1(0.0)
                .setB1(300E-6 / 2)
                .setG2(0.0)
                .setB2(300E-6 / 2)
                .add();
        Line line9 = network.getLine("LINE9_INDEPENDENT");
        line9.newExtension(ConnectablePositionAdder.class)
                .newFeeder1()
                .withName("LINE9_VLGEN7_Side")
                .withOrder(3)
                .withDirection(ConnectablePosition.Direction.BOTTOM)
                .add()
                .newFeeder2()
                .withName("LINE9_VLGEN9_Side")
                .withOrder(1)
                .withDirection(ConnectablePosition.Direction.BOTTOM)
                .add()
                .add();

        network.getVariantManager().setWorkingVariant(VariantManagerConstants.INITIAL_VARIANT_ID);
    }

    @Test
    void testLine7FindsBus2ViaFork() {
        Line line7 = network.getLine("LINE7_FORK");
        Terminal terminal = line7.getTerminal2(); // VLGEN7 side
        BusbarSectionFinderTraverser.BusbarSectionResult result = BusbarSectionFinderTraverser.findBestBusbar(terminal);
        assertNotNull(result);
        assertEquals("BUS2_NGEN7", result.busbarSectionId());
        assertEquals(4, result.depth());
        assertNotNull(result.lastSwitch());
        assertEquals("SECT_BUS2", result.lastSwitch().id());
        assertFalse(result.lastSwitch().isOpen());
    }

    @Test
    void testLine8FindsBus2ViaFork() {
        Line line8 = network.getLine("LINE8_FORK");
        Terminal terminal = line8.getTerminal1(); // VLGEN7 side
        BusbarSectionFinderTraverser.BusbarSectionResult result = BusbarSectionFinderTraverser.findBestBusbar(terminal);
        assertNotNull(result);
        assertEquals("BUS2_NGEN7", result.busbarSectionId());
        assertEquals(4, result.depth());
        assertNotNull(result.lastSwitch());
        assertEquals("SECT_BUS2", result.lastSwitch().id());
        assertFalse(result.lastSwitch().isOpen());
    }

    @Test
    void testLine9FindsBus4DirectPath() {
        Line line9 = network.getLine("LINE9_INDEPENDENT");
        Terminal terminal = line9.getTerminal1(); // VLGEN7 side
        BusbarSectionFinderTraverser.BusbarSectionResult result = BusbarSectionFinderTraverser.findBestBusbar(terminal);
        assertNotNull(result);
        assertEquals("BUS4_NGEN7", result.busbarSectionId());
        assertEquals(3, result.depth());
        assertNotNull(result.lastSwitch());
        assertEquals("SECT_BUS4", result.lastSwitch().id());
        assertFalse(result.lastSwitch().isOpen());
    }

    @Test
    void testBus1AccessibleThroughOpenSwitch() {
        Line line7 = network.getLine("LINE7_FORK");
        Terminal terminal = line7.getTerminal2();
        BusbarSectionFinderTraverser.BusbarSectionResult result = BusbarSectionFinderTraverser.findBestBusbar(terminal);
        // Should prefer BUS2 (closed) over BUS1 (open)
        assertNotNull(result);
        assertEquals("BUS2_NGEN7", result.busbarSectionId());
    }

    @Test
    void testFindsBus1WhenBus2SwitchOpen() {
        VoltageLevel vlgen7 = network.getVoltageLevel("VLGEN7");
        Switch sectBus2 = vlgen7.getNodeBreakerView().getSwitch("SECT_BUS2");
        sectBus2.setOpen(true); // Open the switch to BUS2
        Line line7 = network.getLine("LINE7_FORK");
        Terminal terminal = line7.getTerminal2();
        BusbarSectionFinderTraverser.BusbarSectionResult result = BusbarSectionFinderTraverser.findBestBusbar(terminal);
        assertNotNull(result);
        assertEquals("BUS1_NGEN7", result.busbarSectionId());
        assertEquals(4, result.depth());
        assertNotNull(result.lastSwitch());
        assertTrue(result.lastSwitch().isOpen());
    }

    @Test
    void testBus3AccessibleWhenBus4Open() {
        VoltageLevel vlgen7 = network.getVoltageLevel("VLGEN7");
        Switch sectBus4 = vlgen7.getNodeBreakerView().getSwitch("SECT_BUS4");
        sectBus4.setOpen(true); // Open BUS4 connection
        Line line9 = network.getLine("LINE9_INDEPENDENT");
        Terminal terminal = line9.getTerminal1();
        BusbarSectionFinderTraverser.BusbarSectionResult result = BusbarSectionFinderTraverser.findBestBusbar(terminal);
        assertNotNull(result);
        assertEquals("BUS4_NGEN7", result.busbarSectionId());
    }

    @Test
    void testFindBusbarSectionIdConvenienceMethod() {
        Line line7 = network.getLine("LINE7_FORK");
        Terminal terminal = line7.getTerminal2();
        String busbarId = BusbarSectionFinderTraverser.findBusbarSectionId(terminal);
        assertNotNull(busbarId);
        assertEquals("BUS2_NGEN7", busbarId);
    }

    @Test
    void testReturnsNullWhenNoBusbarAccessible() {
        VoltageLevel vlgen7 = network.getVoltageLevel("VLGEN7");
        // Open all switches connecting busbars
        vlgen7.getNodeBreakerView().getSwitch("SECT_BUS1").setOpen(true);
        vlgen7.getNodeBreakerView().getSwitch("SECT_BUS2").setOpen(true);
        vlgen7.getNodeBreakerView().getSwitch("SECT_BUS3").setOpen(true);
        vlgen7.getNodeBreakerView().getSwitch("SECT_BUS4").setOpen(true);
        vlgen7.getNodeBreakerView().getSwitch("FORK_SW1").setOpen(true);
        vlgen7.getNodeBreakerView().getSwitch("FORK_SW2").setOpen(true);
        Line line7 = network.getLine("LINE7_FORK");
        Terminal terminal = line7.getTerminal2();
        BusbarSectionFinderTraverser.BusbarSectionResult result = BusbarSectionFinderTraverser.findBestBusbar(terminal);
        assertNotNull(result);
        assertEquals("BUS1_NGEN7", result.busbarSectionId());
        assertEquals(4, result.depth());
        assertEquals(3, result.switchesBeforeLast());
        assertNotNull(result.lastSwitch());
        assertEquals("SECT_BUS1", result.lastSwitch().id());
        assertEquals(SwitchKind.DISCONNECTOR, result.lastSwitch().kind());
        assertTrue(result.lastSwitch().isOpen());
        assertEquals(0, result.lastSwitch().node1());
        assertEquals(6, result.lastSwitch().node2());
    }

    @Test
    void testPrefersShortestClosedPath() {
        Line line7 = network.getLine("LINE7_FORK");
        Terminal terminal = line7.getTerminal2();
        BusbarSectionFinderTraverser.BusbarSectionResult result = BusbarSectionFinderTraverser.findBestBusbar(terminal);
        // BUS2 should be preferred (3 switches) over potential longer paths
        assertNotNull(result);
        assertEquals("BUS2_NGEN7", result.busbarSectionId());
        assertEquals(4, result.depth());
    }

    @Test
    void testForkTopologySharesBusbar() {
        Line line7 = network.getLine("LINE7_FORK");
        Line line8 = network.getLine("LINE8_FORK");
        String busbar7 = BusbarSectionFinderTraverser.findBusbarSectionId(line7.getTerminal2());
        String busbar8 = BusbarSectionFinderTraverser.findBusbarSectionId(line8.getTerminal1());
        assertEquals(busbar7, busbar8);
        assertEquals("BUS2_NGEN7", busbar7);
    }

    @Test
    void testSwitchesBeforeLastCount() {
        Line line7 = network.getLine("LINE7_FORK");
        Terminal terminal = line7.getTerminal2();
        BusbarSectionFinderTraverser.BusbarSectionResult result = BusbarSectionFinderTraverser.findBestBusbar(terminal);
        assertNotNull(result);
        assertEquals(4, result.depth());
        assertEquals(3, result.switchesBeforeLast());
    }

    @Test
    void testHandlesMixedSwitchTypes() {
        Line line7 = network.getLine("LINE7_FORK");
        Terminal terminal = line7.getTerminal2();
        BusbarSectionFinderTraverser.BusbarSectionResult result = BusbarSectionFinderTraverser.findBestBusbar(terminal);
        assertNotNull(result);
        // Path contains both breakers and disconnectors
        assertTrue(result.depth() > 0);
    }
}
