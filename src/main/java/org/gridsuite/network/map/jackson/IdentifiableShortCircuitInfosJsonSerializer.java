package org.gridsuite.network.map.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.gridsuite.network.map.dto.utils.IdentifiableShortCircuitInfos;

import java.io.IOException;

/**
 * The main goal of this serializer is to say to Jackson what we refer as "empty" for our DTO.
 */
public class IdentifiableShortCircuitInfosJsonSerializer extends StdSerializer<IdentifiableShortCircuitInfos> {
    private static final IdentifiableShortCircuitInfos EMPTY_OBJ = new IdentifiableShortCircuitInfos(null, null);
    private final transient JsonSerializer<Object> defaultSerializer;

    public IdentifiableShortCircuitInfosJsonSerializer(final JsonSerializer<Object> defaultSerializer) {
        super(IdentifiableShortCircuitInfos.class);
        this.defaultSerializer = defaultSerializer;
    }

    /**
     * {@inheritDoc}
     * @implNote We use the true serializer Jackson would use.
     */
    @Override
    public void serialize(IdentifiableShortCircuitInfos value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        defaultSerializer.serialize(value, gen, provider);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty(SerializerProvider provider, IdentifiableShortCircuitInfos value) {
        return super.isEmpty(provider, value) || EMPTY_OBJ.equals(value);
    }
}
