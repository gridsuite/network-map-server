package org.gridsuite.network.map;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.gridsuite.network.map.model.VoltageLevelConnectableMapData;
import org.junit.Test;

public class VoltageLevelConnectableMapDataTest {
    @Test
    public void equalsContract() {
        EqualsVerifier.simple().forClass(VoltageLevelConnectableMapData.class).verify();
    }

}
