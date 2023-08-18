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

    public static ConnectablePositionInfos getConnectablePositionByFeederInfos(ConnectablePosition.Feeder feeder) {
        ConnectablePositionInfos connectablePositionInfos = new ConnectablePositionInfos();
        if (feeder != null) {
            connectablePositionInfos.setConnectionDirection(feeder.getDirection() == null ? null : feeder.getDirection());
            connectablePositionInfos.setConnectionPosition(feeder.getOrder().orElse(null));
            connectablePositionInfos.setConnectionName(feeder.getName().orElse(null));
        }
        return connectablePositionInfos;
    }
}
