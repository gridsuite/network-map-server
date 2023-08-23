package org.gridsuite.network.map.dto.voltagelevelequipments;

import com.powsybl.iidm.network.Identifiable;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.gridsuite.network.map.dto.ElementInfos;

@SuperBuilder
@Getter
public abstract class AbstractVoltageLevelEquipmentsInfos extends ElementInfos {

    public static ElementInfos toData(Identifiable<?> identifiable, ElementInfos.InfoType dataType) {
        switch (dataType) {
            case FORM:
                throw new UnsupportedOperationException("This case is added to fix sonar issue");
            case LIST:
                return VoltageLevelEquipmentsListInfos.toData(identifiable);
            default:
                throw new UnsupportedOperationException("TODO");
        }
    }
}
