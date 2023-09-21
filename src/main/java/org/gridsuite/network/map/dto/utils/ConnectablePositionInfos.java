package org.gridsuite.network.map.dto.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ConnectablePositionInfos {
    private ConnectablePosition.Direction connectionDirection;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer connectionPosition;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String connectionName;

}


