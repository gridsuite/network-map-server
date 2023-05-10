/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.model;

import lombok.Builder;
import lombok.Getter;
import org.gridsuite.network.map.dto.ElementInfos;

import java.util.List;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@Builder
@Getter
public class AllMapData {
    private List<ElementInfos> substations;
    private List<ElementInfos> lines;
    private List<ElementInfos> hvdcLines;
    private List<ElementInfos> loads;

    private List<ElementInfos> twoWindingsTransformers;
    private List<ElementInfos> threeWindingsTransformers;
    private List<ElementInfos> generators;
    private List<ElementInfos> batteries;
    private List<ElementInfos> danglingLines;
    private List<ElementInfos> lccConverterStations;
    private List<ElementInfos> shuntCompensators;
    private List<ElementInfos> staticVarCompensators;
    private List<ElementInfos> vscConverterStations;
}
