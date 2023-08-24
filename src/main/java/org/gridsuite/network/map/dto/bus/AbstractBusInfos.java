package org.gridsuite.network.map.dto.bus;

import com.powsybl.iidm.network.Identifiable;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.gridsuite.network.map.dto.ElementInfos;

@SuperBuilder
@Getter
public abstract class AbstractBusInfos extends ElementInfos {

    public static ElementInfos toData(Identifiable<?> identifiable, ElementInfos.InfoType dataType) {
        switch (dataType) {
            case LIST:
                return BusListInfos.toData(identifiable);
            default:
                throw new UnsupportedOperationException("TODO");
        }
    }
}
