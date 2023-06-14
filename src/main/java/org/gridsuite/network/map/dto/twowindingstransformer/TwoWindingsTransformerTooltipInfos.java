package org.gridsuite.network.map.dto.twowindingstransformer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.BranchStatus;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.gridsuite.network.map.model.CurrentLimitsData;

import static org.gridsuite.network.map.dto.utils.ElementUtils.*;

@SuperBuilder
@Getter
public class TwoWindingsTransformerTooltipInfos extends AbstractTwoWindingsTransformerInfos {

    private String voltageLevelId1;

    private String voltageLevelId2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double p1;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double p2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double i1;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double i2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private CurrentLimitsData currentLimits1;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private CurrentLimitsData currentLimits2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String branchStatus;

    public static TwoWindingsTransformerTooltipInfos toData(Identifiable<?> identifiable) {
        TwoWindingsTransformer twoWindingsTransformer = (TwoWindingsTransformer) identifiable;
        Terminal terminal1 = twoWindingsTransformer.getTerminal1();
        Terminal terminal2 = twoWindingsTransformer.getTerminal2();

        TwoWindingsTransformerTooltipInfos.TwoWindingsTransformerTooltipInfosBuilder builder = TwoWindingsTransformerTooltipInfos.builder()
            .id(twoWindingsTransformer.getId())
            .name(twoWindingsTransformer.getOptionalName().orElse(null))
            .voltageLevelId1(terminal1.getVoltageLevel().getId())
            .voltageLevelId2(terminal2.getVoltageLevel().getId())
            .i1(nullIfNan(terminal1.getI()))
            .i2(nullIfNan(terminal2.getI()))
            .p1(nullIfNan(terminal1.getP()))
            .p2(nullIfNan(terminal2.getP()));

        twoWindingsTransformer.getCurrentLimits1().ifPresent(limits1 -> builder.currentLimits1(toMapDataCurrentLimits(limits1)));
        twoWindingsTransformer.getCurrentLimits2().ifPresent(limits2 -> builder.currentLimits2(toMapDataCurrentLimits(limits2)));

        BranchStatus<TwoWindingsTransformer> branchStatus = twoWindingsTransformer.getExtension(BranchStatus.class);
        if (branchStatus != null) {
            builder.branchStatus(branchStatus.getStatus().name());
        }

        return builder.build();
    }
}
