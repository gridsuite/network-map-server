package org.gridsuite.network.map.dto;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class InfoTypeParameters {
    Map<String, String> additionalParams;

    public InfoTypeParameters(ElementInfos.InfoType infoType) {
        additionalParams = new HashMap<>();
        additionalParams.put("infoType", infoType.name());
    }

    public void setAdditionalParams(Map<String, String> additionalParams) {
        this.additionalParams = additionalParams;
    }

    public void setInfoType(ElementInfos.InfoType infoType) {
        additionalParams.put("infoType", infoType.name());
    }

    public Double getDcPowerFactor() {
        String dcPowerFactorStr = additionalParams.getOrDefault("dcPowerFactor", null);
        if (dcPowerFactorStr == null) {
            return null;
        }
        return Double.parseDouble(dcPowerFactorStr);
    }

    public ElementInfos.Operation getOperation() {
        String operationStr = additionalParams.getOrDefault("operation", null);
        if (operationStr == null) {
            return null;
        }
        return ElementInfos.Operation.valueOf(operationStr);
    }
}
