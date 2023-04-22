/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.powsybl.iidm.network.*;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.gridsuite.network.map.model.CurrentLimitsData;
import org.gridsuite.network.map.model.TemporaryLimitData;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
@SuperBuilder
@Getter
public class ElementInfos {

    public enum InfoType {
        LIST,
        MAP,
        FORM,
        TAB
    }

    private String id;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String name;

    public static ElementInfos toData(Identifiable<?> identifiable) {
        ElementInfos.ElementInfosBuilder builder = builder()
                .name(identifiable.getOptionalName().orElse(null))
                .id(identifiable.getId());
        return builder.build();
    }

    protected static Double nullIfNan(double d) {
        return Double.isNaN(d) ? null : d;
    }

    protected static CurrentLimitsData toMapDataCurrentLimits(CurrentLimits limits) {
        CurrentLimitsData.CurrentLimitsDataBuilder builder = CurrentLimitsData.builder();
        boolean empty = true;
        if (!Double.isNaN(limits.getPermanentLimit())) {
            builder.permanentLimit(limits.getPermanentLimit());
            empty = false;
        }
        if (limits.getTemporaryLimits() != null && !limits.getTemporaryLimits().isEmpty()) {
            builder.temporaryLimits(toMapDataTemporaryLimit(limits.getTemporaryLimits()));
            empty = false;
        }
        return empty ? null : builder.build();
    }

    private static List<TemporaryLimitData> toMapDataTemporaryLimit(Collection<LoadingLimits.TemporaryLimit> limits) {
        return limits.stream()
                .map(l -> TemporaryLimitData.builder()
                        .name(l.getName())
                        .acceptableDuration(l.getAcceptableDuration() == Integer.MAX_VALUE ? null : l.getAcceptableDuration())
                        .value(l.getValue() == Double.MAX_VALUE ? null : l.getValue())
                        .build())
                .collect(Collectors.toList());
    }

    protected static String getBusOrBusbarSection(Terminal terminal) {
        String busOrBusbarSectionId;
        if (terminal.getVoltageLevel().getTopologyKind().equals(TopologyKind.BUS_BREAKER)) {
            if (terminal.isConnected()) {
                busOrBusbarSectionId = terminal.getBusBreakerView().getBus().getId();
            } else {
                busOrBusbarSectionId = terminal.getBusBreakerView().getConnectableBus().getId();
            }
        } else {
            busOrBusbarSectionId = getBusbarSectionId(terminal);
        }
        return busOrBusbarSectionId;
    }

    protected static String getBusbarSectionId(Terminal terminal) {
        BusbarSectionFinderTraverser connectedBusbarSectionFinder = new BusbarSectionFinderTraverser(terminal.isConnected());
        terminal.traverse(connectedBusbarSectionFinder);
        return connectedBusbarSectionFinder.getFirstTraversedBbsId();
    }
}
