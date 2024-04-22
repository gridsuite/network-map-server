package org.gridsuite.network.map.dto;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class InfoTypesParameters {
    Map<String, String> additionalParams;

    public InfoTypesParameters(ElementInfos.InfoType infoType) {
        additionalParams = new HashMap<>();
        if(infoType != null) {
            additionalParams.put("infoType", infoType.name());
        }
    }

    public void setAdditionalParams(Map<String, String> additionalParams) {
        this.additionalParams = additionalParams;
    }

    public ElementInfos.InfoType getInfoType() {
        String infoType = additionalParams.get("infoType");
        return ElementInfos.InfoType.valueOf(infoType);
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
