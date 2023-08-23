package org.gridsuite.network.map.dto.voltagelevelequipments;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.IdentifiableType;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class VoltageLevelEquipmentsListInfos extends AbstractVoltageLevelEquipmentsInfos {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private IdentifiableType type;

    public static VoltageLevelEquipmentsListInfos toData(Identifiable<?> identifiable) {
        return VoltageLevelEquipmentsListInfos.builder()
                .name(identifiable.getOptionalName().orElse(null))
                .id(identifiable.getId())
                .type(identifiable.getType())
                .build();
    }
}
