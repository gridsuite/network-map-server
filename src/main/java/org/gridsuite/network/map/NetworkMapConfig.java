package org.gridsuite.network.map;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.gridsuite.network.map.jackson.CustomBeanSerializerModifier;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NetworkMapConfig {
    /**
     * We have some additional parameters for Jackson
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer(final BuildProperties buildProperties) {
        // declare our BeanSerializerModifier to Jackson
        return builder -> builder.findModulesViaServiceLoader(true)
                .modulesToInstall(new SimpleModule("NetworkMapServer_customize_Jackson", new Version(1, 0, 0, null, buildProperties.getGroup(), buildProperties.getArtifact()))
                        .setSerializerModifier(new CustomBeanSerializerModifier()));
    }
}
