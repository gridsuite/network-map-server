package org.gridsuite.network.map.dto.mapper;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.BranchObservability;
import lombok.NonNull;
import org.gridsuite.network.map.dto.ElementInfos;
import org.gridsuite.network.map.dto.InfoTypeParameters;
import org.gridsuite.network.map.dto.common.CurrentLimitsData;
import org.gridsuite.network.map.dto.definition.branch.BranchTabInfos;
import org.gridsuite.network.map.dto.definition.branch.BranchTabInfos.BranchTabInfosBuilder;
import org.gridsuite.network.map.dto.definition.extension.BranchObservabilityInfos;
import org.gridsuite.network.map.dto.utils.ElementUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;

import java.util.*;

import static org.gridsuite.network.map.dto.InfoTypeParameters.QUERY_PARAM_DC_POWERFACTOR;
import static org.gridsuite.network.map.dto.utils.ExtensionUtils.buildQualityInfos;

public sealed class BranchInfosMapper permits LineInfosMapper, TieLineInfosMapper, TwoWindingsTransformerInfosMapper {
    protected BranchInfosMapper() { }

    public static ElementInfos toData(@NonNull final Identifiable<?> identifiable,
                                      @NonNull final InfoTypeParameters infoTypeParameters) {
        final Branch<?> branch = (Branch<?>) identifiable;
        final Double dcPowerFactor = Optional.ofNullable(infoTypeParameters.getOptionalParameters().get(QUERY_PARAM_DC_POWERFACTOR))
                .map(Double::valueOf).orElse(null);
        return switch (infoTypeParameters.getInfoType()) {
            case TAB -> toTabInfos(branch, dcPowerFactor);
            default -> throw new UnsupportedOperationException("TODO");
        };
    }

    protected static<T extends BranchTabInfos, B extends BranchTabInfosBuilder<T, ?>> B toTabBuilder(
        @NonNull final B builder, @NonNull final Branch<?> branch, @Nullable final Double dcPowerFactor
    ) {
        /* even if x & r properties are in branch properties doc, it is not in branch getter but each impls... */
        //TODO https://github.com/powsybl/powsybl-core/issues/3521
        switch (branch) {
            case Line line -> builder.r(line.getR()).x(line.getX());
            case TieLine tieLine -> builder.r(tieLine.getR()).x(tieLine.getX());
            case TwoWindingsTransformer twt -> builder.r(twt.getR()).x(twt.getX());
            default -> throw new UnsupportedOperationException("Unsupported branch implementation " + branch.getClass().getName());
        }
        final Terminal terminal1 = branch.getTerminal1();
        final Terminal terminal2 = branch.getTerminal2();
        final Map<String, CurrentLimitsData> mapOperationalLimitsGroup1 = buildCurrentLimitsMap(branch.getOperationalLimitsGroups1());
        builder.operationalLimitsGroup1(mapOperationalLimitsGroup1)
                .operationalLimitsGroup1Names(List.copyOf(mapOperationalLimitsGroup1.keySet()))
                .selectedOperationalLimitsGroup1(branch.getSelectedOperationalLimitsGroupId1().orElse(null));
        final Map<String, CurrentLimitsData> mapOperationalLimitsGroup2 = buildCurrentLimitsMap(branch.getOperationalLimitsGroups2());
        builder.operationalLimitsGroup2(mapOperationalLimitsGroup2)
                .operationalLimitsGroup2Names(List.copyOf(mapOperationalLimitsGroup2.keySet()))
                .selectedOperationalLimitsGroup2(branch.getSelectedOperationalLimitsGroupId2().orElse(null));
        //noinspection unchecked
        return (B) builder
                .name(branch.getOptionalName().orElse(null))
                .id(branch.getId())
                .terminal1Connected(terminal1.isConnected())
                .terminal2Connected(terminal2.isConnected())
                .voltageLevelId1(terminal1.getVoltageLevel().getId())
                .voltageLevelId2(terminal2.getVoltageLevel().getId())
                .voltageLevelName1(terminal1.getVoltageLevel().getOptionalName().orElse(null))
                .voltageLevelName2(terminal2.getVoltageLevel().getOptionalName().orElse(null))
                .nominalVoltage1(terminal1.getVoltageLevel().getNominalV())
                .nominalVoltage2(terminal2.getVoltageLevel().getNominalV())
                .p1(ElementUtils.nullIfNan(terminal1.getP()))
                .p2(ElementUtils.nullIfNan(terminal2.getP()))
                .q1(ElementUtils.nullIfNan(terminal1.getQ()))
                .q2(ElementUtils.nullIfNan(terminal2.getQ()))
                .i1(ElementUtils.nullIfNan(computeIntensity(terminal1, dcPowerFactor)))
                .i2(ElementUtils.nullIfNan(computeIntensity(terminal2, dcPowerFactor)))
                .properties(ElementUtils.getProperties(branch))
                .voltageLevelProperties1(ElementUtils.getProperties(terminal1.getVoltageLevel()))
                .voltageLevelProperties2(ElementUtils.getProperties(terminal2.getVoltageLevel()))
                .substationProperties1(terminal1.getVoltageLevel().getSubstation().map(ElementUtils::getProperties).orElse(null))
                .substationProperties2(terminal2.getVoltageLevel().getSubstation().map(ElementUtils::getProperties).orElse(null))
                .branchObservability(toBranchObservability(branch));
    }

    private static BranchTabInfos toTabInfos(@NonNull final Branch<?> branch, @Nullable final Double dcPowerFactor) {
        /// Why is {@link BranchTabInfos#builder()} return wildcards in return type {@code BranchTabInfosBuilder<?, ?>}
        return toTabBuilder((BranchTabInfosBuilder<BranchTabInfos, ?>) BranchTabInfos.builder(), branch, dcPowerFactor).build();
    }

    private static Map<String, CurrentLimitsData> buildCurrentLimitsMap(@NonNull final Collection<OperationalLimitsGroup> operationalLimitsGroups) {
        if (CollectionUtils.isEmpty(operationalLimitsGroups)) {
            return Map.of();
        }
        Map<String, CurrentLimitsData> res = HashMap.newHashMap(operationalLimitsGroups.size());
        operationalLimitsGroups.forEach(operationalLimitsGroup -> operationalLimitsGroup.getCurrentLimits()
                .ifPresent(limits -> res.put(operationalLimitsGroup.getId(), toMapDataCurrentLimits(limits))));
        return res;
    }

    private static Optional<BranchObservabilityInfos> toBranchObservability(@NonNull final Branch<?> branch) {
        return Optional.ofNullable((BranchObservability<?>) branch.getExtension(BranchObservability.class))
            .map(observability -> BranchObservabilityInfos.builder()
                .qualityP1(buildQualityInfos(observability.getQualityP1()))
                .qualityP2(buildQualityInfos(observability.getQualityP2()))
                .qualityQ1(buildQualityInfos(observability.getQualityQ1()))
                .qualityQ2(buildQualityInfos(observability.getQualityQ2()))
                .isObservable(observability.isObservable())
                .build());
    }

    protected static double computeIntensity(@NonNull final Terminal terminal, @Nullable final Double dcPowerFactor) {
        double intensity = terminal.getI();

        if (Double.isNaN(intensity) && !Double.isNaN(terminal.getP()) && dcPowerFactor != null) {
            // After a DC load flow, the current at a terminal can be undefined (NaN). In that case, we use the DC power factor,
            // the nominal voltage and the active power at the terminal in order to approximate the current following formula
            // P = sqrt(3) x Vnom x I x dcPowerFactor
            intensity = 1000. * terminal.getP() / (Math.sqrt(3) * dcPowerFactor * terminal.getVoltageLevel().getNominalV());
        }
        return intensity;
    }

    protected static CurrentLimitsData toMapDataCurrentLimits(@NonNull final CurrentLimits limits) {
        CurrentLimitsData.CurrentLimitsDataBuilder builder = CurrentLimitsData.builder();
        boolean empty = true;
        if (!Double.isNaN(limits.getPermanentLimit())) {
            builder.permanentLimit(limits.getPermanentLimit());
            empty = false;
        }
        if (!CollectionUtils.isEmpty(limits.getTemporaryLimits())) {
            builder.temporaryLimits(ElementUtils.toMapDataTemporaryLimit(limits.getTemporaryLimits()));
            empty = false;
        }
        return empty ? null : builder.build();
    }
}
