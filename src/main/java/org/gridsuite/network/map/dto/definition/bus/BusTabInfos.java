package org.gridsuite.network.map.dto.definition.bus;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.gridsuite.network.map.dto.ElementInfos;

/**
 * @author Hugo Marcellin <hugo.marcelin at rte-france.com>
 */
@SuperBuilder
@Getter
public class BusTabInfos extends ElementInfos {
    private Double v;

    private Double angle;

    private Integer synchronousComponentNum;

    private Integer connectedComponentNum;

    private String voltageLevelId;

    private Double nominalVoltage;

    private String countryName;

}