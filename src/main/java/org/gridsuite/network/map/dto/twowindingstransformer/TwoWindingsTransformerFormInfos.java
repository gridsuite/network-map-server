/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.twowindingstransformer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.powsybl.iidm.network.CurrentLimits;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.extensions.BranchStatus;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.gridsuite.network.map.model.CurrentLimitsData;
import org.gridsuite.network.map.model.TapChangerData;

import static org.gridsuite.network.map.dto.utils.ElementUtils.*;

/**
 * @author AJELLAL Ali <ali.ajellal@rte-france.com>
 */
@SuperBuilder
@Getter
public class TwoWindingsTransformerFormInfos extends AbstractTwoWindingsTransformerInfos {

    private String voltageLevelId1;

    private String voltageLevelName1;

    private String voltageLevelId2;

    private String voltageLevelName2;

    private Boolean terminal1Connected;

    private Boolean terminal2Connected;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double p1;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double q1;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double p2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double q2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double i1;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double i2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private CurrentLimitsData currentLimits1;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private CurrentLimitsData currentLimits2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private TapChangerData phaseTapChanger;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private TapChangerData ratioTapChanger;

    private Double g;

    private Double b;

    private Double r;

    private Double x;

    private Double ratedU1;

    private Double ratedU2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double ratedS;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String connectionName1;

    private ConnectablePosition.Direction connectionDirection1;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String connectionName2;

    private ConnectablePosition.Direction connectionDirection2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer connectionPosition1;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer connectionPosition2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String busOrBusbarSectionId1;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String busOrBusbarSectionId2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String branchStatus;

    public static TwoWindingsTransformerFormInfos toData(Identifiable<?> identifiable) {
        TwoWindingsTransformer twoWT = (TwoWindingsTransformer) identifiable;
        Terminal terminal1 = twoWT.getTerminal1();
        Terminal terminal2 = twoWT.getTerminal2();

        TwoWindingsTransformerFormInfos.TwoWindingsTransformerFormInfosBuilder builder = TwoWindingsTransformerFormInfos.builder()
                .name(twoWT.getOptionalName().orElse(null))
                .id(twoWT.getId())
                .terminal1Connected(terminal1.isConnected())
                .terminal2Connected(terminal2.isConnected())
                .voltageLevelId1(terminal1.getVoltageLevel().getId())
                .voltageLevelName1(terminal1.getVoltageLevel().getOptionalName().orElse(null))
                .voltageLevelId2(terminal2.getVoltageLevel().getId())
                .voltageLevelName2(terminal2.getVoltageLevel().getOptionalName().orElse(null))
                .phaseTapChanger(toMapData(twoWT.getPhaseTapChanger()))
                .ratioTapChanger(toMapData(twoWT.getRatioTapChanger()))
                .r(twoWT.getR())
                .x(twoWT.getX())
                .b(twoWT.getB())
                .g(twoWT.getG())
                .ratedU1(twoWT.getRatedU1())
                .ratedU2(twoWT.getRatedU2());

        builder.busOrBusbarSectionId1(getBusOrBusbarSection(terminal1))
                .busOrBusbarSectionId2(getBusOrBusbarSection(terminal2));
        builder.ratedS(nullIfNan(twoWT.getRatedS()));
        builder.p1(nullIfNan(terminal1.getP()));
        builder.q1(nullIfNan(terminal1.getQ()));
        builder.p2(nullIfNan(terminal2.getP()));
        builder.q2(nullIfNan(terminal2.getQ()));
        builder.i1(nullIfNan(terminal1.getI()));
        builder.i2(nullIfNan(terminal2.getI()));

        CurrentLimits limits1 = twoWT.getCurrentLimits1().orElse(null);
        CurrentLimits limits2 = twoWT.getCurrentLimits2().orElse(null);
        if (limits1 != null) {
            builder.currentLimits1(toMapDataCurrentLimits(limits1));
        }
        if (limits2 != null) {
            builder.currentLimits2(toMapDataCurrentLimits(limits2));
        }
        BranchStatus<TwoWindingsTransformer> branchStatus = twoWT.getExtension(BranchStatus.class);
        if (branchStatus != null) {
            builder.branchStatus(branchStatus.getStatus().name());
        }
        var connectablePosition = twoWT.getExtension(ConnectablePosition.class);
        if (connectablePosition != null) {
            if (connectablePosition.getFeeder1() != null) {
                builder
                        .connectionDirection1(connectablePosition.getFeeder1().getDirection())
                        .connectionName1(connectablePosition.getFeeder1().getName().orElse(null))
                        .connectionPosition1(connectablePosition.getFeeder1().getOrder().orElse(null));
            }

            if (connectablePosition.getFeeder2() != null) {
                builder
                        .connectionDirection2(connectablePosition.getFeeder2().getDirection())
                        .connectionName2(connectablePosition.getFeeder2().getName().orElse(null))
                        .connectionPosition2(connectablePosition.getFeeder2().getOrder().orElse(null));
            }
        }
        return builder.build();
    }
}
