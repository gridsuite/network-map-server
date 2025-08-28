/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@Configuration
public class NetworkMapSwaggerConfig {
    @Bean
    public OpenAPI createOpenApi() {
        return new OpenAPI()
            .info(new Info()
                .title("Network map API")
                .description("This is the documentation of network map REST API")
                .version(NetworkMapApi.API_VERSION));
    }
}
