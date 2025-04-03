package org.gridsuite.network.map.dto.definition.extension;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import org.gridsuite.network.map.config.nan.NullAndNaNFilter;

/**
 * @author Souissi Maissa <maissa.souissi at rte-france.com>
 */

@Getter
@Builder
public class ActivePowerControlInfos {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean participate;

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NullAndNaNFilter.class)
    private Double droop;

}


