/*
  Copyright (c) 2024, RTE (http://www.rte-france.com)
  This Source Code Form is subject to the terms of the Mozilla Public
  License, v. 2.0. If a copy of the MPL was not distributed with this
  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.network.map.dto;

import lombok.Builder;

import java.util.List;
// TODO : delete and adjust Get network elements endpoint

/**
 * @author AJELLAL Ali <ali.ajellal@rte-france.com>
 */
@Builder
public record EquipmentInfos(ElementType elementType, List<String> substationsIds) {
}
