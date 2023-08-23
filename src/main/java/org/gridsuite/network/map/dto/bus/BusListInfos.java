package org.gridsuite.network.map.dto.bus;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Identifiable;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class BusListInfos extends AbstractBusInfos {

        public static BusListInfos toData(Identifiable<?> identifiable) {
            Bus bus = (Bus) identifiable;
            return BusListInfos.builder()
                    .name(bus.getOptionalName().orElse(null))
                    .id(bus.getId())
                    .build();
        }
}
