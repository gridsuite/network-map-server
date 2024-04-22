package org.gridsuite.network.map.dto;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class InfoTypesParameters {
    Map<String, String> additionalParams;
    public static final String QUERY_PARAM_OPERATION = "operation";
    public static final String QUERY_PARAM_INFO_TYPE = "infoType";
    public static final String QUERY_PARAM_DC_POWER_FACTOR = "dcPowerFactor";

    public InfoTypesParameters(ElementInfos.InfoType infoType) {
        additionalParams = new HashMap<>();
        if (infoType != null) {
            additionalParams.put(QUERY_PARAM_INFO_TYPE, infoType.name());
        }
    }

    public ElementInfos.InfoType getInfoType() {
        String infoType = additionalParams.get(QUERY_PARAM_INFO_TYPE);
        return ElementInfos.InfoType.valueOf(infoType);
    }

    public Double getDcPowerFactor() {
        String dcPowerFactorStr = additionalParams.getOrDefault(QUERY_PARAM_DC_POWER_FACTOR, null);
        if (dcPowerFactorStr == null) {
            return null;
        }
        return Double.parseDouble(dcPowerFactorStr);
    }

    public ElementInfos.Operation getOperation() {
        String operationStr = additionalParams.getOrDefault(QUERY_PARAM_OPERATION, null);
        if (operationStr == null) {
            return null;
        }
        return ElementInfos.Operation.valueOf(operationStr);
    }
}
