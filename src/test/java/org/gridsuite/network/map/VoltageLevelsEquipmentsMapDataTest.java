package org.gridsuite.network.map;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.gridsuite.network.map.model.VoltageLevelsEquipmentsMapData;
import org.junit.Test;

public class VoltageLevelsEquipmentsMapDataTest {

    @Test
    public void equalsContract() {
        EqualsVerifier.simple().forClass(VoltageLevelsEquipmentsMapData.class).verify();
    }
}
