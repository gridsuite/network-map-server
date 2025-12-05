/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.definition.generator;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.gridsuite.network.map.dto.ElementInfos;
import org.gridsuite.network.map.dto.definition.extension.*;

import java.util.Optional;

/**
 * @author AJELLAL Ali <ali.ajellal@rte-france.com>
 */
@SuperBuilder
@Getter
public class GeneratorTooltipInfos extends ElementInfos {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double p;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double q;

    private Double targetP;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double targetQ;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double targetV;

    private Double minP;

    private Double maxP;

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private Optional<GeneratorStartupInfos> generatorStartup;

    private boolean voltageRegulatorOn;
}
