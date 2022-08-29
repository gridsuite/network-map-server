package org.gridsuite.network.map.model;

import com.powsybl.iidm.network.IdentifiableType;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Builder
@Getter
@EqualsAndHashCode
public class VoltageLevelConnectableMapData {
    private String id;

    private String name;

    private IdentifiableType type;
}
