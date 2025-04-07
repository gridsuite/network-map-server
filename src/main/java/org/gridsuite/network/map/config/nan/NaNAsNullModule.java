package org.gridsuite.network.map.config.nan;

import com.fasterxml.jackson.databind.module.SimpleModule;

public class NaNAsNullModule extends SimpleModule {
    public NaNAsNullModule() {
        this.addSerializer(Double.class, new NaNAsNullDoubleSerializer())
                .addSerializer(Float.class, new NaNAsNullFloatSerializer());
    }
}
