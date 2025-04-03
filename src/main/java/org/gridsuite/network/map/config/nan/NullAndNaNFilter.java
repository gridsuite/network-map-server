package org.gridsuite.network.map.config.nan;

public class NullAndNaNFilter {
    @SuppressWarnings("checkstyle:EqualsHashCode")
    @Override
    public boolean equals(Object value) {
        return switch (value) {
            case null -> true; // Ignore null
            case Double doubleValue when doubleValue.isNaN() -> true; // Ignore NaN
            case Float floatValue when floatValue.isNaN() -> true; // Ignore NaN
            default -> false;
        };
    }
}
