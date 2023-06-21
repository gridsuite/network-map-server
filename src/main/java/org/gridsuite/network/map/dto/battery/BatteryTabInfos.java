/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.battery;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.*;
import com.powsybl.network.store.iidm.impl.MinMaxReactiveLimitsImpl;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.gridsuite.network.map.model.MinMaxReactiveLimitsMapData;
import org.gridsuite.network.map.model.ReactiveCapabilityCurveMapData;

import java.util.List;

import static org.gridsuite.network.map.dto.utils.ElementUtils.nullIfNan;

@SuperBuilder
@Getter
public class BatteryTabInfos extends AbstractBatteryInfos {
    private String voltageLevelId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean activePowerControlOn;

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

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private MinMaxReactiveLimitsMapData minMaxReactiveLimits;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<ReactiveCapabilityCurveMapData> reactiveCapabilityCurvePoints;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean participate;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double droop;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String connectionName;

    private ConnectablePosition.Direction connectionDirection;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer connectionPosition;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String busOrBusbarSectionId;
    public static BatteryTabInfos toData(Identifiable<?> identifiable) {
        Battery battery = (Battery) identifiable;
        Terminal terminal = battery.getTerminal();
        BatteryTabInfos.BatteryTabInfosBuilder builder = BatteryTabInfos.builder()
                .name(battery.getOptionalName().orElse(null))
                .id(battery.getId())
                .voltageLevelId(terminal.getVoltageLevel().getId())
                .targetP(battery.getTargetP())
                .targetQ(nullIfNan(battery.getTargetQ()))
                .minP(battery.getMinP())
                .maxP(battery.getMaxP())
                .p(nullIfNan(terminal.getP()))
                .q(nullIfNan(terminal.getQ()));

        ActivePowerControl<Battery> activePowerControl = battery.getExtension(ActivePowerControl.class);
        if (activePowerControl != null) {
            builder.activePowerControlOn(activePowerControl.isParticipate());
            builder.droop(activePowerControl.getDroop());
        }

        ReactiveLimits reactiveLimits = battery.getReactiveLimits();
        if (reactiveLimits != null) {
            ReactiveLimitsKind limitsKind = reactiveLimits.getKind();
            if (limitsKind == ReactiveLimitsKind.MIN_MAX) {
                MinMaxReactiveLimits minMaxReactiveLimits = battery.getReactiveLimits(MinMaxReactiveLimitsImpl.class);
                builder.minMaxReactiveLimits(MinMaxReactiveLimitsMapData.builder()
                        .maximumReactivePower(minMaxReactiveLimits.getMaxQ())
                        .minimumReactivePower(minMaxReactiveLimits.getMinQ())
                        .build());
            } else if (limitsKind == ReactiveLimitsKind.CURVE) {
                ReactiveCapabilityCurve capabilityCurve = battery.getReactiveLimits(ReactiveCapabilityCurve.class);
                builder.reactiveCapabilityCurvePoints(getReactiveCapabilityCurvePoints(capabilityCurve.getPoints()));
            }
        }

        var connectablePosition = battery.getExtension(ConnectablePosition.class);
        if (connectablePosition != null) {
            builder
                    .connectionDirection(connectablePosition.getFeeder().getDirection())
                    .connectionName(connectablePosition.getFeeder().getName().orElse(null));
            connectablePosition.getFeeder().getOrder().ifPresent(builder::connectionPosition);
        }

        return builder.build();
    }

}
