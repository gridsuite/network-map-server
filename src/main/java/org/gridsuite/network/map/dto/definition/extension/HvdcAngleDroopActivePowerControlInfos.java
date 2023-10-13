package org.gridsuite.network.map.dto.definition.extension;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

/**
 * @author Souissi Maissa <maissa.souissi at rte-france.com>
 */

@Getter
@Builder
public class HvdcAngleDroopActivePowerControlInfos {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Float droop;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean isEnabled;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Float p0;
}
