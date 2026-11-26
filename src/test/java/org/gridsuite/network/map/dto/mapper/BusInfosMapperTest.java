/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package org.gridsuite.network.map.dto.mapper;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TopologyKind;
import org.gridsuite.network.map.dto.definition.topology.BusInfos;
import org.gridsuite.network.map.dto.utils.ElementUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class BusInfosMapperTest {

    @ParameterizedTest
    @CsvSource({
        "225.5, -12.25, 225.5, -12.25",
        "NaN, -12.25, , -12.25",
        "225.5, NaN, 225.5, ",
        "NaN, NaN, , "
    })
    void toBusInfosMapsVoltageAndAngle(double v, double angle, Double expectedV, Double expectedAngle) {
        Network network = Network.create("test", "test");
        Bus bus = network.newVoltageLevel()
                .setId("VL")
                .setNominalV(225)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add()
                .getBusBreakerView().newBus().setId("BUS").add();
        bus.setV(v);
        bus.setAngle(angle);

        BusInfos infos = ElementUtils.toBusInfos(bus);

        assertNotNull(infos);
        assertEquals("BUS", infos.getId());
        assertEquals(expectedV, infos.getV());
        assertEquals(expectedAngle, infos.getAngle());
    }

    @Test
    void toBusInfosReturnsNullForNullBus() {
        assertNull(ElementUtils.toBusInfos(null));
    }
}
