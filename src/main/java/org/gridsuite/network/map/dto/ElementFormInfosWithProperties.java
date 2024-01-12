package org.gridsuite.network.map.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@SuperBuilder
@Getter
public class ElementFormInfosWithProperties extends ElementInfos {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, String> properties;

    protected ElementFormInfosWithProperties(ElementInfosBuilder<?, ?> b) {
        super(b);
    }
}
