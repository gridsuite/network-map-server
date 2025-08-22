/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.mapper;

import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import org.gridsuite.network.map.dto.ElementInfos;
import org.gridsuite.network.map.dto.ElementInfosWithOperatingStatus;
import org.gridsuite.network.map.dto.ElementInfosWithType;
import org.gridsuite.network.map.dto.definition.extension.ConnectablePositionInfos;

import static org.gridsuite.network.map.dto.utils.ElementUtils.toOperatingStatus;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
public final class ElementInfosMapper {
    private ElementInfosMapper() {
    }

    public static ElementInfosWithType toInfosWithType(Identifiable<?> identifiable) {
        return ElementInfosWithType.builder()
                .id(identifiable.getId())
                .name(identifiable.getOptionalName().orElse(null))
                .type(identifiable.getType())
                .build();
    }

    public static ElementInfos toListInfos(Identifiable<?> identifiable) {
        return ElementInfos.builder()
                .id(identifiable.getId())
                .name(identifiable.getOptionalName().orElse(null))
                .build();
    }

    public static ElementInfosWithOperatingStatus toInfosWithOperatingStatus(Identifiable<?> identifiable) {
        return ElementInfosWithOperatingStatus.builder()
                .id(identifiable.getId())
                .name(identifiable.getOptionalName().orElse(null))
                .operatingStatus(toOperatingStatus(identifiable))
                .build();
    }

    public static ConnectablePositionInfos toConnectablePositionInfos(Connectable<?> connectable, ConnectablePosition.Feeder feeder) {
        return ConnectablePositionInfos.builder()
            .connectionPosition(feeder.getOrder().orElse(null))
            .connectionDirection(feeder.getDirection())
            .connectionName(feeder.getName().orElse(connectable.getId()))
            .build();
    }
}
