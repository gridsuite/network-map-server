/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.gridsuite.network.map.model.BusbarSectionMapData;
import org.junit.Test;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class BusbarSectionMapDataTest {

    @Test
    public void equalsContract() {
        EqualsVerifier.simple().forClass(BusbarSectionMapData.class).verify();
    }
}
