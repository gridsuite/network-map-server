package org.gridsuite.network.map.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
@EqualsAndHashCode
public class VoltageLevelsEquipmentsMapData {

    private VoltageLevelMapData voltageLevel;

    private List<VoltageLevelConnectableMapData> equipments;
}
