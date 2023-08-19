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

    public static ConnectablePositionInfos toConnectablePositionInfos(Branch<?> branch, Branch.Side side) {
        ConnectablePositionInfos connectablePositionInfos = new ConnectablePositionInfos();
        var connectablePosition = branch.getExtension(ConnectablePosition.class);
        if (connectablePosition != null) {
            ConnectablePosition.Feeder feeder = side == Branch.Side.ONE ? connectablePosition.getFeeder1() : connectablePosition.getFeeder2();
            if (feeder != null) {
                connectablePositionInfos.setConnectionDirection(feeder.getDirection());
                connectablePositionInfos.setConnectionPosition(feeder.getOrder().orElse(null));
                connectablePositionInfos.setConnectionName(feeder.getName().orElse(null));
            }
        }
        return connectablePositionInfos;
    }
}
