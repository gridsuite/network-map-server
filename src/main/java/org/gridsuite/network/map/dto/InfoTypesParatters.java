package org.gridsuite.network.map.dto;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class InfoTypesParatters {
    private static final String DC_POWER_FACTOR = "dcPowerFactor";
    private static final String OPERATION = "operation";
    @Getter
    private ElementInfos.InfoType infoType;
    private Map<String, String> additionalParams;

    public InfoTypesParatters(ElementInfos.InfoType infoType, Map<String, String> additionalParams) {
        this.infoType = infoType;
        this.additionalParams = additionalParams;
    }

    public InfoTypesParatters(ElementInfos.InfoType infoType) {
        this.infoType = infoType;
        additionalParams = new HashMap<>();
    }

    public Double getDcPowerFactor() {
        String dcPowerFactorStr = additionalParams.get(DC_POWER_FACTOR);
        return Double.parseDouble(dcPowerFactorStr);
    }

    public ElementInfos.Operation getOperation() {
        String operationStr = additionalParams.get(OPERATION);
        return ElementInfos.Operation.valueOf(operationStr);
    }
}
