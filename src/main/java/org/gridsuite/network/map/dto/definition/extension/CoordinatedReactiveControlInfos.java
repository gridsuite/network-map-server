package org.gridsuite.network.map.dto.definition.extension;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import org.gridsuite.network.map.config.nan.NullAndNaNFilter;

/**
 * @author Souissi Maissa <maissa.souissi at rte-france.com>
 */

@Getter
@Builder
public class CoordinatedReactiveControlInfos {

    // As this attribute has only one lower case letter at its start (xXXXX), the getters is parsed as getQPercent and the field for Jackson is parsed as qpercent
    // while we expect qPercent. JsonProperty let fix the json field to qPercent
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NullAndNaNFilter.class)
    @JsonProperty("qPercent")
    @Getter(AccessLevel.NONE)
    private Double qPercent;

}


