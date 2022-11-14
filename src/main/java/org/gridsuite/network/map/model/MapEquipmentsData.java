package org.gridsuite.network.map.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.gridsuite.network.map.model.LineMapData;
import org.gridsuite.network.map.model.SubstationMapData;

import java.util.List;

@Builder
@Getter
@EqualsAndHashCode
public class MapEquipmentsData {
    private List<SubstationMapData> substations;
    private List<LineMapData> lines;
}
