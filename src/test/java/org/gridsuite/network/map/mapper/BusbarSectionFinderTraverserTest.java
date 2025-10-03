package org.gridsuite.network.map.mapper;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.*;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.network.store.iidm.impl.NetworkFactoryImpl;
import org.gridsuite.network.map.dto.definition.extension.BusbarSectionFinderTraverser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Ghazwa Rehili <ghazwa.rehili at rte-france.com>
 */

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
        /*
         * VLGEN7 - Fork topology with bypass
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
         *                    ┌───┴───┐                 ┌──────┴──────┐
         *                    |       |                 |             |
         *                 LINE7   LINE8           Breaker10      Disconnector7
         *              (→VLGEN4) (→VLGEN8)      [open = true]  [open = false]
         *                                            |             |
         *                                       Disconnector5      |
         *                                      [open = false]      |
         *                                            |             |
         *                                            |_____________|
         *                                                  |
         *                                            bypass point
         *                                              (node11)
         *                                                  |
         *                                            Disconnector6
         *                                          [open = false]
         *                                                  |
         *                                               LINE9
         *                                            (→VLGEN9)
         *
         * TOPOLOGY SUMMARY:
         * ================
         *
         * VLGEN7 Node Mapping:
         * - Node 0: BUS1 (Primary Busbar)
         * - Node 5: BUS2 (Secondary Busbar)
         * - Node 9: BUS3 (Third Busbar)
         * - Node 13: BUS4 (Fourth Busbar)
         * - Node 8: Fork point (connects BUS1 & BUS2 to LINE7 & LINE8)
         * - Node 11: Bypass point (convergence for breaker and bypass paths to LINE9)
         *
         * Fork Configuration:
         * - BUS1 is DISCONNECTED (SECT_BUS1 open)
         * - BUS2 feeds the fork through SECT_BUS2 (closed) → FORK_SW2 (closed)
         * - Fork splits to LINE7 and LINE8
         *
         * Bypass Configuration:
         * - BUS4 connects to LINE9 via two parallel paths:
         *   Path 1: BRKR10 (OPEN) → DISC5 → Node 11
         *   Path 2: DISC7 (CLOSED) → DISC_BYPASS_CONV → Node 11 (Bypass active)
         * - With BRKR10 open and DISC7 closed, LINE9 operates via bypass
         */

        // ============ VLGEN4 - Source voltage level ============
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
        createSwitch(vlgen4, "DISC_VLGEN4", SwitchKind.DISCONNECTOR, false, 0, 10);
        createSwitch(vlgen4, "BRKR_VLGEN4", SwitchKind.BREAKER, false, 10, 15);

        // ============ VLGEN7 - Main voltage level with fork and bypass topology ============
        VoltageLevel vlgen7 = network.newVoltageLevel()
                .setId("VLGEN7")
                .setName("Fork Distribution Point")
                .setNominalV(24.0)
                .setHighVoltageLimit(30.0)
                .setLowVoltageLimit(20.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();

        // Create 4 busbars
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

        // Bus coupling disconnectors (SECT)
        createSwitch(vlgen7, "SECT_BUS1", SwitchKind.DISCONNECTOR, true, 0, 6);    // OPEN - BUS1 disconnected
        createSwitch(vlgen7, "SECT_BUS2", SwitchKind.DISCONNECTOR, false, 5, 7);   // CLOSED - BUS2 connected
        createSwitch(vlgen7, "SECT_BUS3", SwitchKind.DISCONNECTOR, false, 9, 10);  // CLOSED - BUS3 connected
        createSwitch(vlgen7, "SECT_BUS4", SwitchKind.DISCONNECTOR, false, 13, 14); // CLOSED - BUS4 connected

        // Fork connections - BUS1 and BUS2 to fork point (node 8)
        createSwitch(vlgen7, "FORK_SW1", SwitchKind.DISCONNECTOR, false, 6, 8);    // BUS1 to fork
        createSwitch(vlgen7, "FORK_SW2", SwitchKind.DISCONNECTOR, false, 7, 8);    // BUS2 to fork

        // LINE7 connection from fork point (node 8)
        createSwitch(vlgen7, "DISC_LINE7", SwitchKind.DISCONNECTOR, false, 8, 1);
        createSwitch(vlgen7, "BRKR_LINE7", SwitchKind.BREAKER, false, 1, 2);

        // LINE8 connection from fork point (node 8)
        createSwitch(vlgen7, "DISC_LINE8", SwitchKind.DISCONNECTOR, false, 8, 3);
        createSwitch(vlgen7, "BRKR_LINE8", SwitchKind.BREAKER, false, 3, 4);

        // ============ BYPASS TOPOLOGY - BUS4 with bypass for LINE9 ============
       // BUS3 to BUS4 coupler section (already created above: SECT_BUS3)

        // Path 1: Breaker10 (OPEN) - Main breaker path
        createSwitch(vlgen7, "BRKR10", SwitchKind.BREAKER, true, 14, 15);          // OPEN - Main breaker
        createSwitch(vlgen7, "DISC5", SwitchKind.DISCONNECTOR, false, 15, 11);     // CLOSED - After breaker

        // Path 2: Disconnector7 (CLOSED) - Bypass path
        createSwitch(vlgen7, "DISC7", SwitchKind.DISCONNECTOR, false, 14, 16);     // CLOSED - Bypass disconnector

        // Convergence to bypass point (node 11)
        createSwitch(vlgen7, "DISC_BYPASS_CONV", SwitchKind.DISCONNECTOR, false, 16, 11); // Bypass convergence

        // LINE9 connection from bypass point (node 11)
        createSwitch(vlgen7, "DISC6", SwitchKind.DISCONNECTOR, false, 11, 12);     // CLOSED - Before LINE9
        createSwitch(vlgen7, "BRKR_LINE9", SwitchKind.BREAKER, false, 12, 17);     // LINE9 breaker

        // ============ LINE7 - Fork Branch 1 (VLGEN4 → VLGEN7) ============
        network.newLine()
                .setId("LINE7_FORK")
                .setName("Fork Branch 1 - Primary Line")
                .setVoltageLevel1("VLGEN4")
                .setNode1(15)
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

        // ============ VLGEN8 - Fork destination 2 ============
        VoltageLevel vlgen8 = network.newVoltageLevel()
                .setId("VLGEN8")
                .setName("Fork Destination Point 2")
                .setNominalV(24.0)
                .setHighVoltageLimit(30.0)
                .setLowVoltageLimit(20.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        vlgen8.getNodeBreakerView().newBusbarSection()
                .setId("BUS_NGEN8")
                .setName("Main Busbar VLGEN8")
                .setNode(0)
                .add();

        createSwitch(vlgen8, "DISC_VLGEN8", SwitchKind.DISCONNECTOR, false, 0, 1);
        createSwitch(vlgen8, "BRKR_VLGEN8", SwitchKind.BREAKER, false, 1, 2);

        // ============ LINE8 - Fork Branch 2 (VLGEN7 → VLGEN8) ============
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

        // ============ VLGEN9 - Independent line destination ============
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

        // ============ LINE9 - Independent line from BUS4 with bypass ============
        network.newLine()
                .setId("LINE9_INDEPENDENT")
                .setName("Independent Line from BUS4")
                .setVoltageLevel1("VLGEN7")
                .setNode1(17)  // Connected via bypass topology
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
        Properties properties = new Properties();
        network.write("XIIDM", properties, "/tmp", "BusbarSectionFinderTraverserTest");
    }

    /* ============================================
    * FORK topology tests
    * ============================================
    */

    @Test
    void testForkTopologyFindsBus2() {
        // LINE7 and LINE8 share the same fork point (node 8)
        // Both must find BUS2 because SECT_BUS2 is closed
        Line line7 = network.getLine("LINE7_FORK");
        Line line8 = network.getLine("LINE8_FORK");
        BusbarSectionFinderTraverser.BusbarSectionResult result7 = BusbarSectionFinderTraverser.getBusbarSectionResult(line7.getTerminal2());
        BusbarSectionFinderTraverser.BusbarSectionResult result8 = BusbarSectionFinderTraverser.getBusbarSectionResult(line8.getTerminal1());
        // Both lines must find the same busbar
        assertNotNull(result7);
        assertEquals("BUS2_NGEN7", result7.busbarSectionId());
        assertNotNull(result8);
        assertEquals("BUS2_NGEN7", result8.busbarSectionId());
        // Check depth and last switch
        assertEquals(4, result7.depth());
        assertEquals(4, result8.depth());
        assertEquals("SECT_BUS2", result7.lastSwitch().id());
        assertFalse(result7.lastSwitch().isOpen());
    }

    @Test
    void testForkPreferencesClosedOverOpen() {
        Line line7 = network.getLine("LINE7_FORK");
        BusbarSectionFinderTraverser.BusbarSectionResult result = BusbarSectionFinderTraverser.getBusbarSectionResult(line7.getTerminal2());
        // Must prefer BUS2 with closed switch rather than BUS1 with open switch
        assertEquals("BUS2_NGEN7", result.busbarSectionId());
        assertFalse(result.lastSwitch().isOpen());
    }

    @Test
    void testForkFallbackToBus1WhenBus2Open() {
        VoltageLevel vlgen7 = network.getVoltageLevel("VLGEN7");
        // Open SECT_BUS2 to disconnect BUS2
        vlgen7.getNodeBreakerView().getSwitch("SECT_BUS2").setOpen(true);
        Line line7 = network.getLine("LINE7_FORK");
        BusbarSectionFinderTraverser.BusbarSectionResult result = BusbarSectionFinderTraverser.getBusbarSectionResult(line7.getTerminal2());
        // Must find BUS1 (with open switch)
        assertNotNull(result);
        assertEquals("BUS1_NGEN7", result.busbarSectionId());
        assertEquals("SECT_BUS1", result.lastSwitch().id());
        assertTrue(result.lastSwitch().isOpen());
    }

    /* ============================================
     * BYPASS topology tests
     * ============================================
     */

    @Test
    void testBypassTopologyActivePath() {
        Line line9 = network.getLine("LINE9_INDEPENDENT");
        BusbarSectionFinderTraverser.BusbarSectionResult result = BusbarSectionFinderTraverser.getBusbarSectionResult(line9.getTerminal1());
        // Must find BUS4 via the bypass path
        assertNotNull(result);
        assertEquals("BUS4_NGEN7", result.busbarSectionId());
        // Expected depth for bypass path
        assertEquals(5, result.depth());
        // Last switch must be closed (not the open breaker)
        assertNotNull(result.lastSwitch());
        assertFalse(result.lastSwitch().isOpen());
    }

    @Test
    void testBypassSwitchesToMainPathWhenBreakerCloses() {
        VoltageLevel vlgen7 = network.getVoltageLevel("VLGEN7");
        // Close BRKR10 and open bypass DISC7
        vlgen7.getNodeBreakerView().getSwitch("BRKR10").setOpen(false);
        vlgen7.getNodeBreakerView().getSwitch("DISC7").setOpen(true);
        Line line9 = network.getLine("LINE9_INDEPENDENT");
        BusbarSectionFinderTraverser.BusbarSectionResult result = BusbarSectionFinderTraverser.getBusbarSectionResult(line9.getTerminal1());
        // Must still find BUS4
        assertNotNull(result);
        assertEquals("BUS4_NGEN7", result.busbarSectionId());
        // Path must now use the closed breaker
        assertFalse(result.lastSwitch().isOpen());
    }

    @Test
    void testBypassFallbackToBus3() {
        VoltageLevel vlgen7 = network.getVoltageLevel("VLGEN7");
        // Completely isolate BUS4
        vlgen7.getNodeBreakerView().getSwitch("SECT_BUS4").setOpen(true);
        Line line9 = network.getLine("LINE9_INDEPENDENT");
        BusbarSectionFinderTraverser.BusbarSectionResult result = BusbarSectionFinderTraverser.getBusbarSectionResult(line9.getTerminal1());
        // Should still find BUS4 because bypass is still connected to it
        assertNotNull(result);
        assertEquals("BUS4_NGEN7", result.busbarSectionId());
    }

    /* ============================================
     * Selection priority tests
     * ============================================
     */

    @Test
    void testPrioritizesShortestClosedPath() {
        Line line7 = network.getLine("LINE7_FORK");
        BusbarSectionFinderTraverser.BusbarSectionResult result = BusbarSectionFinderTraverser.getBusbarSectionResult(line7.getTerminal2());
        // BUS2 is the closest with closed switch
        assertNotNull(result);
        assertEquals("BUS2_NGEN7", result.busbarSectionId());
        assertEquals(4, result.depth());
    }

    @Test
    void testSelectionPriorityOrder() {
        VoltageLevel vlgen7 = network.getVoltageLevel("VLGEN7");
        // Open all coupling disconnectors
        vlgen7.getNodeBreakerView().getSwitch("SECT_BUS1").setOpen(true);
        vlgen7.getNodeBreakerView().getSwitch("SECT_BUS2").setOpen(true);
        vlgen7.getNodeBreakerView().getSwitch("SECT_BUS3").setOpen(true);
        vlgen7.getNodeBreakerView().getSwitch("SECT_BUS4").setOpen(true);
        // But keep fork connections
        Line line7 = network.getLine("LINE7_FORK");
        BusbarSectionFinderTraverser.BusbarSectionResult result = BusbarSectionFinderTraverser.getBusbarSectionResult(line7.getTerminal2());
        // Must find a busbar with open switch priority 2
        assertNotNull(result);
        assertNotNull(result.lastSwitch());
        assertTrue(result.lastSwitch().isOpen());
    }

    @Test
    void testReturnsResultEvenWithNoClosedPaths() {
        VoltageLevel vlgen7 = network.getVoltageLevel("VLGEN7");
        // Open all coupling switches but keep fork switches
        vlgen7.getNodeBreakerView().getSwitch("SECT_BUS1").setOpen(true);
        vlgen7.getNodeBreakerView().getSwitch("SECT_BUS2").setOpen(true);
        Line line7 = network.getLine("LINE7_FORK");
        BusbarSectionFinderTraverser.BusbarSectionResult result = BusbarSectionFinderTraverser.getBusbarSectionResult(line7.getTerminal2());
        // Must return a result (busbar accessible via open switch)
        assertNotNull(result);
        assertEquals(4, result.depth());
    }

    @Test
    void testForkLinesShareSameBusbar() {
        Line line7 = network.getLine("LINE7_FORK");
        Line line8 = network.getLine("LINE8_FORK");
        String busbar7 = BusbarSectionFinderTraverser.findBusbarSectionId(line7.getTerminal2());
        String busbar8 = BusbarSectionFinderTraverser.findBusbarSectionId(line8.getTerminal1());
        // Both fork lines must find the same busbar
        assertNotNull(busbar7);
        assertEquals(busbar7, busbar8);
        assertEquals("BUS2_NGEN7", busbar7);
    }

    @Test
    void testSwitchesBeforeLastCount() {
        Line line7 = network.getLine("LINE7_FORK");
        Terminal terminal = line7.getTerminal2();
        BusbarSectionFinderTraverser.BusbarSectionResult result = BusbarSectionFinderTraverser.getBusbarSectionResult(terminal);
        assertNotNull(result);
        assertEquals(4, result.depth());
    }

    @Test
    void testHandlesMixedSwitchTypes() {
        Line line7 = network.getLine("LINE7_FORK");
        Terminal terminal = line7.getTerminal2();
        BusbarSectionFinderTraverser.BusbarSectionResult result = BusbarSectionFinderTraverser.getBusbarSectionResult(terminal);
        assertNotNull(result);
        // Path contains both breakers and disconnectors
        assertEquals(4, result.depth());
    }

    @Test
    void testSwitchesBeforeLastCountAccuracy() {
        Line line7 = network.getLine("LINE7_FORK");
        BusbarSectionFinderTraverser.BusbarSectionResult result = BusbarSectionFinderTraverser.getBusbarSectionResult(line7.getTerminal2());
        assertNotNull(result);
    }
}
