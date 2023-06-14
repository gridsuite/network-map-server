package org.gridsuite.network.map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@Configuration
public class JacksonMapperConfig {

    @Bean
    ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        return buildObjectMapper(builder);
    }

    public static ObjectMapper buildObjectMapper(Jackson2ObjectMapperBuilder builder) {
        return builder.defaultViewInclusion(true).build();
    }
}
