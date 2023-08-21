package org.gridsuite.network.map.dto.utils;

import com.powsybl.iidm.network.extensions.ConnectablePosition;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConnectablePositionInfos {
    private ConnectablePosition.Direction connectionDirection;
    private String connectionName;
    private Integer connectionPosition;

}
