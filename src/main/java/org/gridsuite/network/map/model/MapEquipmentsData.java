package org.gridsuite.network.map.model;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class MapEquipmentsData {
    private List<LineMapData> lines;
    private List<SubstationMapData> substations;
}
