package org.gridsuite.network.map.dto.voltagelevelequipments;

import com.powsybl.iidm.network.Identifiable;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.gridsuite.network.map.dto.ElementInfos;

@SuperBuilder
@Getter
public class AbstractVoltageLevelEquipmentsInfos extends ElementInfos {

    public static ElementInfos toData(Identifiable<?> identifiable, ElementInfos.InfoType dataType) {
        switch (dataType) {
            case LIST:
                return VoltageLevelEquipmentsListInfos.toData(identifiable);
            default:
                throw new UnsupportedOperationException("TODO");
        }
    }
}
