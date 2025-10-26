package org.gridsuite.network.map.dto.utils;

import com.powsybl.commons.extensions.Extendable;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.*;
import com.powsybl.iidm.network.extensions.DiscreteMeasurement.TapChanger;
import org.gridsuite.network.map.dto.definition.extension.*;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Optional;
import java.util.function.Supplier;

public final class ExtensionUtils {
    private ExtensionUtils() { }

    public static ConnectablePositionInfos toMapConnectablePosition(@NonNull final Identifiable<?> identifiable, @NonNull final int index) {
        ConnectablePositionInfos.ConnectablePositionInfosBuilder builder = ConnectablePositionInfos.builder();
        Optional.ofNullable((ConnectablePosition<?>) identifiable.getExtension(ConnectablePosition.class))
                .map(connectablePosition -> switch (index) {
                    case 0 -> connectablePosition.getFeeder();
                    case 1 -> connectablePosition.getFeeder1();
                    case 2 -> connectablePosition.getFeeder2();
                    default -> throw new IllegalArgumentException("Invalid feeder index: " + index);
                })
                .ifPresent(feeder -> builder
                        .connectionDirection(feeder.getDirection() == null ? null : feeder.getDirection())
                        .connectionPosition(feeder.getOrder().orElse(null))
                        .connectionName(feeder.getName().orElse(null)));
        return builder.build();
    }

    public static Optional<ActivePowerControlInfos> toActivePowerControl(@NonNull final Identifiable<?> identifiable) {
        return Optional.ofNullable((ActivePowerControl<?>) identifiable.getExtension(ActivePowerControl.class))
                .map(activePowerControl -> ActivePowerControlInfos.builder()
                        .participate(activePowerControl.isParticipate())
                        .droop(activePowerControl.getDroop())
                        .maxTargetP(activePowerControl.getMaxTargetP().isPresent() ? activePowerControl.getMaxTargetP().getAsDouble() : null)
                        .build());
    }

    public static Optional<ShortCircuitInfos> toShortCircuit(@NonNull final Supplier<ShortCircuitExtension<?>> shortCircuitExtensionSupplier) {
        return Optional.ofNullable(shortCircuitExtensionSupplier.get())
                .map(shortCircuitExtension -> ShortCircuitInfos.builder()
                        .directTransX(shortCircuitExtension.getDirectTransX())
                        .stepUpTransformerX(shortCircuitExtension.getStepUpTransformerX())
                        .build());
    }

    public static String toOperatingStatus(@NonNull final Extendable<?> extendable, boolean isTerminalsConnected) {
        if (extendable instanceof Branch<?>
                || extendable instanceof ThreeWindingsTransformer
                || extendable instanceof HvdcLine
                || extendable instanceof BusbarSection) {
            var operatingStatus = extendable.getExtension(OperatingStatus.class);
            if (operatingStatus == null || isTerminalsConnected) {
                return null;
            }
            return operatingStatus.getStatus().name();
        }
        return null;
    }

    public static Optional<IdentifiableShortCircuitInfos> toIdentifiableShortCircuit(@NonNull final VoltageLevel voltageLevel) {
        return Optional.ofNullable((IdentifiableShortCircuit<VoltageLevel>) voltageLevel.getExtension(IdentifiableShortCircuit.class))
                .map(identifiableShortCircuit -> IdentifiableShortCircuitInfos.builder()
                        .ipMin(identifiableShortCircuit.getIpMin())
                        .ipMax(identifiableShortCircuit.getIpMax())
                        .build());
    }

    public static Optional<MeasurementsInfos> toMeasurement(@NonNull final Connectable<?> connectable, @NonNull final Measurement.Type type, @NonNull final int index) {
        return Optional.ofNullable((Measurements<?>) connectable.getExtension(Measurements.class))
                .flatMap(measurements -> measurements.getMeasurements(type)
                        .stream()
                        .filter(m -> m.getSide() == null || m.getSide().getNum() - 1 == index)
                        .findFirst())
                .map(m -> MeasurementsInfos.builder().value(m.getValue()).validity(m.isValid()).build());
    }

    public static Optional<TapChangerDiscreteMeasurementsInfos> toMeasurementTapChanger(@NonNull final Connectable<?> connectable, @NonNull final DiscreteMeasurement.Type type, @NonNull final TapChanger tapChanger) {
        return Optional.ofNullable((DiscreteMeasurements<?>) connectable.getExtension(DiscreteMeasurements.class))
                .flatMap(measurements -> measurements.getDiscreteMeasurements(type)
                        .stream()
                        .filter(m -> m.getTapChanger() == tapChanger)
                        .findFirst())
                .map(m -> TapChangerDiscreteMeasurementsInfos.builder().value(m.getValueAsInt()).validity(m.isValid()).build());
    }

    public static Optional<InjectionObservabilityInfos> toInjectionObservability(@NonNull final Injection<?> injection) {
        return Optional.ofNullable((InjectionObservability<?>) injection.getExtension(InjectionObservability.class))
                .map(observability -> InjectionObservabilityInfos.builder()
                        .qualityQ(buildQualityInfos(observability.getQualityQ()))
                        .qualityP(buildQualityInfos(observability.getQualityP()))
                        .qualityV(buildQualityInfos(observability.getQualityV()))
                        .isObservable(observability.isObservable())
                        .build());
    }

    public static ObservabilityQualityInfos buildQualityInfos(@Nullable final ObservabilityQuality<?> quality) {
        if (quality == null) {
            return null;
        }
        return ObservabilityQualityInfos.builder()
                .standardDeviation(quality.getStandardDeviation())
                .isRedundant(quality.isRedundant().orElse(null))
                .build();
    }
}
