package org.gridsuite.network.map.config.nan;

public class NullAndNaNFilter {

    @Override
    public boolean equals(Object value) {
        if (value == null) {
            return true; // Ignore null
        } else if (value instanceof Double doubleValue && doubleValue.isNaN()) {
            return true; // Ignore NaN
        } else if (value instanceof Float floatValue && floatValue.isNaN()) {
            return true; // Ignore NaN
        }
        return false;
    }

    @Override
    public int hashCode() {
        return 0; // 0 due to not important
    }
}
