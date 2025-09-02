package org.gridsuite.network.map.dto.mapper;

import com.google.common.annotations.VisibleForTesting;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.LoadingLimits.TemporaryLimit;
import com.powsybl.iidm.network.extensions.BranchObservability;
import com.powsybl.iidm.network.extensions.Measurement.Type;
import lombok.NonNull;
import org.gridsuite.network.map.dto.ElementInfos;
import org.gridsuite.network.map.dto.InfoTypeParameters;
import org.gridsuite.network.map.dto.common.CurrentLimitsData;
import org.gridsuite.network.map.dto.common.CurrentLimitsData.Applicability;
import org.gridsuite.network.map.dto.common.CurrentLimitsData.CurrentLimitsDataBuilder;
import org.gridsuite.network.map.dto.common.TemporaryLimitData;
import org.gridsuite.network.map.dto.definition.branch.BranchTabInfos;
import org.gridsuite.network.map.dto.definition.branch.BranchTabInfos.BranchTabInfosBuilder;
import org.gridsuite.network.map.dto.definition.extension.BranchObservabilityInfos;
import org.gridsuite.network.map.dto.utils.ElementUtils;
import org.gridsuite.network.map.dto.utils.ExtensionUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.gridsuite.network.map.dto.InfoTypeParameters.QUERY_PARAM_DC_POWERFACTOR;
import static org.gridsuite.network.map.dto.InfoTypeParameters.QUERY_PARAM_LOAD_OPERATIONAL_LIMIT_GROUPS;
import static org.gridsuite.network.map.dto.common.CurrentLimitsData.Applicability.SIDE1;
import static org.gridsuite.network.map.dto.common.CurrentLimitsData.Applicability.SIDE2;
import static org.gridsuite.network.map.dto.utils.ElementUtils.mapCountry;
import static org.gridsuite.network.map.dto.utils.ExtensionUtils.buildQualityInfos;

public sealed class BranchInfosMapper permits LineInfosMapper, TieLineInfosMapper, TwoWindingsTransformerInfosMapper {
    protected BranchInfosMapper() { }

    public static ElementInfos toData(@NonNull final Identifiable<?> identifiable,
                                      @NonNull final InfoTypeParameters infoTypeParameters) {
        final Branch<?> branch = (Branch<?>) identifiable;
        final Double dcPowerFactor = Optional.ofNullable(infoTypeParameters.getOptionalParameters().get(QUERY_PARAM_DC_POWERFACTOR))
                .map(Double::valueOf).orElse(null);
        final Boolean loadOperationalLimitGroups = Optional.ofNullable(infoTypeParameters.getOptionalParameters().get(QUERY_PARAM_LOAD_OPERATIONAL_LIMIT_GROUPS))
            .map(Boolean::valueOf).orElse(false);
        return switch (infoTypeParameters.getInfoType()) {
            case TAB -> toTabInfos(branch, dcPowerFactor, loadOperationalLimitGroups);
            default -> throw new UnsupportedOperationException("TODO");
        };
    }

    protected static<T extends BranchTabInfos, B extends BranchTabInfosBuilder<T, ?>> B toTabBuilder(
        @NonNull final B builder,
        @NonNull final Branch<?> branch,
        @Nullable final Double dcPowerFactor,
        @NonNull final Boolean loadOperationalLimitGroups
    ) {
        /* even if x & r properties are in branch properties doc, it is not in branch getter but each impls... */
        //TODO https://github.com/powsybl/powsybl-core/issues/3521 tagged for release 09/2025
        switch (branch) {
            case Line line -> builder.r(line.getR()).x(line.getX());
            case TieLine tieLine -> builder.r(tieLine.getR()).x(tieLine.getX());
            case TwoWindingsTransformer twt -> builder.r(twt.getR()).x(twt.getX());
            default -> throw new UnsupportedOperationException("Unsupported branch implementation " + branch.getClass().getName());
        }
        // extensions for connectable type
        if (branch instanceof Connectable<?> connectable) {
            builder.measurementP1(ExtensionUtils.toMeasurement(connectable, Type.ACTIVE_POWER, 0))
                    .measurementQ1(ExtensionUtils.toMeasurement(connectable, Type.REACTIVE_POWER, 0))
                    .measurementP2(ExtensionUtils.toMeasurement(connectable, Type.ACTIVE_POWER, 1))
                    .measurementQ2(ExtensionUtils.toMeasurement(connectable, Type.REACTIVE_POWER, 1));
        }
        // common properties
        final Terminal terminal1 = branch.getTerminal1();
        final Terminal terminal2 = branch.getTerminal2();
        if (loadOperationalLimitGroups) {
            final Map<String, CurrentLimitsData> mapOperationalLimitsGroup1 = buildCurrentLimitsMap(branch.getOperationalLimitsGroups1());
            builder.operationalLimitsGroup1(mapOperationalLimitsGroup1)
                .operationalLimitsGroup1Names(List.copyOf(mapOperationalLimitsGroup1.keySet()))
                .selectedOperationalLimitsGroup1(branch.getSelectedOperationalLimitsGroupId1().orElse(null));
            final Map<String, CurrentLimitsData> mapOperationalLimitsGroup2 = buildCurrentLimitsMap(branch.getOperationalLimitsGroups2());
            builder.operationalLimitsGroup2(mapOperationalLimitsGroup2)
                .operationalLimitsGroup2Names(List.copyOf(mapOperationalLimitsGroup2.keySet()))
                .selectedOperationalLimitsGroup2(branch.getSelectedOperationalLimitsGroupId2().orElse(null));
        }
        //noinspection unchecked
        return (B) builder
                .type(branch.getType().name())
                .name(branch.getOptionalName().orElse(null))
                .id(branch.getId())
                .substationId1(terminal1.getVoltageLevel().getSubstation().map(Substation::getId).orElse(null))
                .substationId2(terminal2.getVoltageLevel().getSubstation().map(Substation::getId).orElse(null))
                .country1(mapCountry(terminal1.getVoltageLevel().getSubstation().orElse(null)))
                .country2(mapCountry(terminal2.getVoltageLevel().getSubstation().orElse(null)))
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
                .branchObservability(toBranchObservability(branch))
                .operatingStatus(ExtensionUtils.toOperatingStatus(branch));
    }

    private static BranchTabInfos toTabInfos(@NonNull final Branch<?> branch, @Nullable final Double dcPowerFactor, @NonNull final Boolean loadOperationalLimitGroups) {
        /// Why is {@link BranchTabInfos#builder()} return wildcards in return type {@code BranchTabInfosBuilder<?, ?>}
        return toTabBuilder((BranchTabInfosBuilder<BranchTabInfos, ?>) BranchTabInfos.builder(), branch, dcPowerFactor, loadOperationalLimitGroups).build();
    }

    private static Map<String, CurrentLimitsData> buildCurrentLimitsMap(@NonNull final Collection<OperationalLimitsGroup> operationalLimitsGroups) {
        if (CollectionUtils.isEmpty(operationalLimitsGroups)) {
            return Map.of();
        }
        Map<String, CurrentLimitsData> res = HashMap.newHashMap(operationalLimitsGroups.size());
        operationalLimitsGroups.forEach(operationalLimitsGroup -> operationalLimitsGroup.getCurrentLimits()
                .ifPresent(limits -> res.put(operationalLimitsGroup.getId(), toMapDataCurrentLimits(limits, null, null))));
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

    protected static CurrentLimitsData toMapDataCurrentLimits(@NonNull final CurrentLimits limits,
                                                              @Nullable final String id, @Nullable final Applicability applicability) {
        CurrentLimitsDataBuilder builder = CurrentLimitsData.builder().id(id).applicability(applicability);
        boolean empty = true;
        if (!Double.isNaN(limits.getPermanentLimit())) {
            builder.permanentLimit(limits.getPermanentLimit());
            empty = false;
        }
        if (!CollectionUtils.isEmpty(limits.getTemporaryLimits())) {
            builder.temporaryLimits(toMapDataTemporaryLimit(limits.getTemporaryLimits()));
            empty = false;
        }
        return empty ? null : builder.build();
    }

    protected static void buildCurrentLimits(@NonNull final Collection<OperationalLimitsGroup> currentLimits, @NonNull final Consumer<List<CurrentLimitsData>> build) {
        final List<CurrentLimitsData> currentLimitsData = currentLimits.stream()
            .map(olg -> operationalLimitsGroupToMapDataCurrentLimits(olg, null))
            .toList();
        if (!currentLimitsData.isEmpty()) {
            build.accept(currentLimitsData);
        }
    }

    /**
     * Combine 2 sides in one list
     */
    @Nullable
    protected static List<CurrentLimitsData> mergeCurrentLimits(@NonNull final Collection<OperationalLimitsGroup> operationalLimitsGroups1,
                                                                @NonNull final Collection<OperationalLimitsGroup> operationalLimitsGroups2) {
        // Build temporary limit from side 1 and 2
        List<CurrentLimitsData> currentLimitsData1 = operationalLimitsGroups1.stream()
            .map(currentLimitsData -> operationalLimitsGroupToMapDataCurrentLimits(currentLimitsData, SIDE1))
            .filter(Objects::nonNull)
            .toList();
        List<CurrentLimitsData> currentLimitsData2 = new ArrayList<>(operationalLimitsGroups2.stream()
            .map(currentLimitsData -> operationalLimitsGroupToMapDataCurrentLimits(currentLimitsData, SIDE2))
            .filter(Objects::nonNull)
            .toList());

        // simple case: one of the arrays is empty
        if (currentLimitsData2.isEmpty() && !currentLimitsData1.isEmpty()) {
            return currentLimitsData1;
        }
        if (currentLimitsData1.isEmpty() && !currentLimitsData2.isEmpty()) {
            return currentLimitsData2;
        }

        // more complex case
        final List<CurrentLimitsData> mergedLimitsData = Stream.concat(currentLimitsData1.stream(), currentLimitsData2.stream())
                .collect(Collectors.groupingByConcurrent(CurrentLimitsData::getId))
                .values()
                .parallelStream()
                .<CurrentLimitsData>mapMulti((currentLimitsData, acc) -> {
                    if (currentLimitsData.isEmpty() || currentLimitsData.size() > 2) {
                        throw new UnsupportedOperationException("IDs are assumed unique in each lists");
                    } else if (currentLimitsData.size() == 2) {
                        final CurrentLimitsData limitsData = currentLimitsData.get(0);
                        if (limitsData.limitsEquals(currentLimitsData.get(1))) { // case 1: both sides have limits which are equals
                            acc.accept(CurrentLimitsData.builder()
                                .id(limitsData.getId())
                                .applicability(Applicability.EQUIPMENT)
                                .temporaryLimits(limitsData.getTemporaryLimits())
                                .permanentLimit(limitsData.getPermanentLimit())
                                .build());
                            return;
                        }
                    }
                    // case 2: both sides have limits and are different: create 2 different limit sets
                    // case 3: only one side has limits
                    currentLimitsData.forEach(acc);
                })
                .toList();

        if (!mergedLimitsData.isEmpty()) {
            return mergedLimitsData;
        }
        return null;
    }

    @VisibleForTesting // or else is private
    @Nullable
    static CurrentLimitsData operationalLimitsGroupToMapDataCurrentLimits(@Nullable final OperationalLimitsGroup operationalLimitsGroup,
                                                                          @Nullable final Applicability applicability) {
        if (operationalLimitsGroup == null || operationalLimitsGroup.getCurrentLimits().isEmpty()) {
            return null;
        }
        return toMapDataCurrentLimits(operationalLimitsGroup.getCurrentLimits().get(), operationalLimitsGroup.getId(), applicability);
    }

    private static List<TemporaryLimitData> toMapDataTemporaryLimit(@NonNull final Collection<TemporaryLimit> limits) {
        return limits.stream()
            .map(l -> TemporaryLimitData.builder()
                .name(l.getName())
                .acceptableDuration(l.getAcceptableDuration() == Integer.MAX_VALUE ? null : l.getAcceptableDuration())
                .value(l.getValue() == Double.MAX_VALUE ? null : l.getValue())
                .build())
            .collect(Collectors.toList());
    }
}
