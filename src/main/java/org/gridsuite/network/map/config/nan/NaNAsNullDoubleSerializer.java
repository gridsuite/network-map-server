package org.gridsuite.network.map.config.nan;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class NaNAsNullDoubleSerializer extends JsonSerializer<Double> {

    @Override
    public void serialize(Double value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null || value.isNaN()) {
            gen.writeNull(); // Convert NaN to null
        } else {
            gen.writeNumber(value);
        }
    }
}
