package org.gridsuite.network.map.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
@EqualsAndHashCode
public class MapEquipmentsData {
    private List<SubstationMapData> substations;
    private List<LineMapData> lines;
}
