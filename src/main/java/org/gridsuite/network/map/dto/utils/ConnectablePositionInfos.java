package org.gridsuite.network.map.dto.utils;

import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConnectablePositionInfos {
    private ConnectablePosition.Direction connectionDirection;
    private String connectionName;
    private Integer connectionPosition;

    public static ConnectablePosition.Feeder getFeederInfosByIndex(Branch<?> branch, int index) {
        var connectablePosition = branch.getExtension(ConnectablePosition.class);
        if (connectablePosition == null) {
            return null;
        }

        switch (index) {
            case 0:
                return connectablePosition.getFeeder();
            case 1:
                return connectablePosition.getFeeder1();
            case 2:
                return connectablePosition.getFeeder2();
            default:
                throw new IllegalArgumentException("Invalid feeder index: " + index);
        }
    }

    public static ConnectablePositionInfos getConnectablePositionByFeederInfos(Branch<?> branch, int index) {
        ConnectablePosition.Feeder feeder = getFeederInfosByIndex(branch, index);
        ConnectablePositionInfos connectablePositionInfos = new ConnectablePositionInfos();
        if (feeder != null) {
            connectablePositionInfos.setConnectionDirection(feeder.getDirection() == null ? null : feeder.getDirection());
            connectablePositionInfos.setConnectionPosition(feeder.getOrder().orElse(null));
            connectablePositionInfos.setConnectionName(feeder.getName().orElse(null));
        }
        return connectablePositionInfos;
    }
}
