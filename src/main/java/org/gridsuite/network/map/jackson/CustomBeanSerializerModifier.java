package org.gridsuite.network.map.jackson;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import org.gridsuite.network.map.dto.utils.IdentifiableShortCircuitInfos;

/**
 * Serializer modifier for learning to Jackson what is an "empty" object for some of our DTOs,
 *  so we can use {@code @JsonInclude(Include.NON_EMPTY)}.
 *
 * @implNote we use this to permit custom serializers to use default serializer
 * and only have {@link JsonSerializer#isEmpty(SerializerProvider, Object)} to override.
 */
public class CustomBeanSerializerModifier extends BeanSerializerModifier {
    /**
     * {@inheritDoc}
     *
     * @param config the {@link SerializationConfig configuration} to use
     * @param beanDesc the {@link BeanDescription details} of the bean we work on
     * @param serializer the {@link JsonSerializer default serializer} Jackson would normally use
     */
    @SuppressWarnings("unchecked")
    @Override
    public JsonSerializer<?> modifySerializer(SerializationConfig config, BeanDescription beanDesc, JsonSerializer<?> serializer) {
        // if it's one of our DTOs, use the corresponding custom serializer
        if (beanDesc.getBeanClass().equals(IdentifiableShortCircuitInfos.class)) {
            return new IdentifiableShortCircuitInfosJsonSerializer((JsonSerializer<Object>) serializer);
        } else {
            return super.modifySerializer(config, beanDesc, serializer);
        }
    }
}
