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

import static org.gridsuite.network.map.dto.utils.ElementUtils.getBusOrBusbarSection;

@SuperBuilder
@Getter
public class BatteryFormInfos extends AbstractBatteryInfos {

    private String voltageLevelId;


    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String busOrBusbarSectionId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String connectionName;

    private ConnectablePosition.Direction connectionDirection;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer connectionPosition;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private double minActivePower;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private double maxActivePower;


    @JsonInclude(JsonInclude.Include.NON_NULL)
    private MinMaxReactiveLimitsMapData minMaxReactiveLimits;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<ReactiveCapabilityCurveMapData> reactiveCapabilityCurvePoints;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private double activePowerSetpoint;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double reactivePowerSetpoint;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean participate;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double droop;



    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean activePowerControlOn;


    public static BatteryFormInfos toData(Identifiable<?> identifiable) {
        Battery battery = (Battery) identifiable;
        Terminal terminal = battery.getTerminal();
        BatteryFormInfos.BatteryFormInfosBuilder  builder = BatteryFormInfos.builder()
                .name(battery.getOptionalName().orElse(null))
                .id(battery.getId())
                .voltageLevelId(terminal.getVoltageLevel().getId())
                .minActivePower(battery.getMinP())
                .maxActivePower(battery.getMaxP())
                .activePowerSetpoint(battery.getTargetP())
                .reactivePowerSetpoint(battery.getTargetQ());
        builder.busOrBusbarSectionId(getBusOrBusbarSection(terminal));

        ActivePowerControl<Battery> activePowerControl = battery.getExtension(ActivePowerControl.class);
        if (activePowerControl != null) {
            builder.activePowerControlOn(activePowerControl.isParticipate());
            builder.droop(activePowerControl.getDroop());
        }

        var connectablePosition = battery.getExtension(ConnectablePosition.class);
        if (connectablePosition != null) {
            builder
                    .connectionDirection(connectablePosition.getFeeder().getDirection())
                    .connectionName(connectablePosition.getFeeder().getName().orElse(null));
            connectablePosition.getFeeder().getOrder().ifPresent(builder::connectionPosition);
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

        return builder.build();
    }

}
