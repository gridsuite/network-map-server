/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.mapper;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.CoordinatedReactiveControl;
import com.powsybl.iidm.network.extensions.GeneratorShortCircuit;
import com.powsybl.iidm.network.extensions.GeneratorStartup;
import com.powsybl.iidm.network.extensions.Measurement.Type;
import com.powsybl.network.store.iidm.impl.MinMaxReactiveLimitsImpl;
import org.gridsuite.network.map.dto.ElementInfos;
import org.gridsuite.network.map.dto.InfoTypeParameters;
import org.gridsuite.network.map.dto.common.MinMaxReactiveLimitsMapData;
import org.gridsuite.network.map.dto.common.ReactiveCapabilityCurveMapData;
import org.gridsuite.network.map.dto.definition.extension.CoordinatedReactiveControlInfos;
import org.gridsuite.network.map.dto.definition.extension.GeneratorStartupInfos;
import org.gridsuite.network.map.dto.definition.generator.GeneratorFormInfos;
import org.gridsuite.network.map.dto.definition.generator.GeneratorTabInfos;
import org.gridsuite.network.map.dto.definition.generator.GeneratorTooltipInfos;
import org.gridsuite.network.map.dto.utils.ElementUtils;
import org.gridsuite.network.map.dto.utils.ExtensionUtils;
import org.springframework.lang.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.gridsuite.network.map.dto.InfoTypeParameters.QUERY_PARAM_LOAD_REGULATING_TERMINALS;
import static org.gridsuite.network.map.dto.utils.ElementUtils.*;

/**
 * @author AJELLAL Ali <ali.ajellal@rte-france.com>
 */

public final class GeneratorInfosMapper {

    private GeneratorInfosMapper() {
    }

    public static ElementInfos toData(Identifiable<?> identifiable, InfoTypeParameters infoTypeParameters) {
        boolean loadRegulatingTerminals = Optional.ofNullable(infoTypeParameters.getOptionalParameters().get(QUERY_PARAM_LOAD_REGULATING_TERMINALS))
            .map(Boolean::valueOf).orElse(false);
        return switch (infoTypeParameters.getInfoType()) {
            case TAB -> toTabInfos(identifiable, loadRegulatingTerminals);
            case FORM -> toFormInfos(identifiable);
            case TOOLTIP -> toTooltipInfos(identifiable);
            case LIST -> ElementInfosMapper.toInfosWithType(identifiable);
            default -> throw handleUnsupportedInfoType(infoTypeParameters.getInfoType(), "Generator");
        };
    }

    private static List<ReactiveCapabilityCurveMapData> getReactiveCapabilityCurvePoints(Collection<ReactiveCapabilityCurve.Point> points) {
        return points.stream()
                .map(point -> ReactiveCapabilityCurveMapData.builder()
                        .p(point.getP())
                        .maxQ(point.getMaxQ())
                        .minQ(point.getMinQ())
                        .build())
                .collect(Collectors.toList());
    }

    private static GeneratorTabInfos toTabInfos(Identifiable<?> identifiable, boolean loadRegulatingTerminals) {
        Generator generator = (Generator) identifiable;
        Terminal terminal = generator.getTerminal();
        GeneratorTabInfos.GeneratorTabInfosBuilder<?, ?> builder = GeneratorTabInfos.builder()
                .name(generator.getOptionalName().orElse(null))
                .id(generator.getId())
                .terminalConnected(terminal.isConnected())
                .voltageLevelId(terminal.getVoltageLevel().getId())
                .nominalVoltage(terminal.getVoltageLevel().getNominalV())
                .country(mapCountry(terminal.getVoltageLevel().getSubstation().orElse(null)))
                .targetP(generator.getTargetP())
                .targetQ(nullIfNan(generator.getTargetQ()))
                .targetV(nullIfNan(generator.getTargetV()))
                .minP(generator.getMinP())
                .maxP(generator.getMaxP())
                .ratedS(nullIfNan(generator.getRatedS()))
                .energySource(generator.getEnergySource())
                .voltageRegulatorOn(generator.isVoltageRegulatorOn())
                .p(nullIfNan(terminal.getP()))
                .properties(getProperties(generator))
                .q(nullIfNan(terminal.getQ()));

        builder.activePowerControl(ExtensionUtils.toActivePowerControl(generator))
                .coordinatedReactiveControl(toCoordinatedReactiveControl(generator))
                .generatorShortCircuit(ExtensionUtils.toShortCircuit(() -> generator.getExtension(GeneratorShortCircuit.class)))
                .generatorStartup(toGeneratorStartup(generator));

        if (loadRegulatingTerminals) {
            Terminal regulatingTerminal = generator.getRegulatingTerminal();
            //If there is no regulating terminal in file, regulating terminal voltage level is equal to generator voltage level
            if (regulatingTerminal != null && !regulatingTerminal.getVoltageLevel().equals(terminal.getVoltageLevel())) {
                builder.regulatingTerminalVlName(regulatingTerminal.getVoltageLevel().getOptionalName().orElse(null));
                builder.regulatingTerminalConnectableId(regulatingTerminal.getConnectable().getId());
                builder.regulatingTerminalConnectableType(regulatingTerminal.getConnectable().getType().name());
                builder.regulatingTerminalVlId(regulatingTerminal.getVoltageLevel().getId());
            }
        }

        ReactiveLimits reactiveLimits = generator.getReactiveLimits();
        if (reactiveLimits != null) {
            ReactiveLimitsKind limitsKind = reactiveLimits.getKind();
            if (limitsKind == ReactiveLimitsKind.MIN_MAX) {
                MinMaxReactiveLimits minMaxReactiveLimits = generator.getReactiveLimits(MinMaxReactiveLimitsImpl.class);
                builder.minMaxReactiveLimits(MinMaxReactiveLimitsMapData.builder()
                        .maxQ(minMaxReactiveLimits.getMaxQ())
                        .minQ(minMaxReactiveLimits.getMinQ())
                        .build());
            } else if (limitsKind == ReactiveLimitsKind.CURVE) {
                ReactiveCapabilityCurve capabilityCurve = generator.getReactiveLimits(ReactiveCapabilityCurve.class);
                builder.reactiveCapabilityCurvePoints(getReactiveCapabilityCurvePoints(capabilityCurve.getPoints()));
            }
        }

        builder.connectablePosition(ExtensionUtils.toMapConnectablePosition(generator, 0));

        builder.measurementP(ExtensionUtils.toMeasurement(generator, Type.ACTIVE_POWER, 0))
            .measurementQ(ExtensionUtils.toMeasurement(generator, Type.REACTIVE_POWER, 0));

        builder.isCondenser(generator.isCondenser());

        // substation attrubutes
        builder.substationId(terminal.getVoltageLevel().getSubstation().map(Substation::getId).orElse(null));
        builder.substationName(terminal.getVoltageLevel().getSubstation().map(Substation::getOptionalName).orElse(null).orElse(null));
        builder.substationProperties(terminal.getVoltageLevel().getSubstation().map(ElementUtils::getProperties).orElse(null));

        // voltage level attributes
        builder.voltageLevelName(terminal.getVoltageLevel().getOptionalName().orElse(null));
        builder.voltageLevelProperties(getProperties(terminal.getVoltageLevel()));
        builder.lowVoltageLimit(terminal.getVoltageLevel().getLowVoltageLimit());
        builder.highVoltageLimit(terminal.getVoltageLevel().getHighVoltageLimit());
        builder.voltageLevelShortCircuit(ExtensionUtils.toIdentifiableShortCircuit(terminal.getVoltageLevel()));
        builder.injectionObservability(ExtensionUtils.toInjectionObservability(generator));

        return builder.build();
    }

    private static GeneratorFormInfos toFormInfos(Identifiable<?> identifiable) {
        Generator generator = (Generator) identifiable;
        Terminal terminal = generator.getTerminal();
        GeneratorFormInfos.GeneratorFormInfosBuilder<?, ?> builder = GeneratorFormInfos.builder()
                .name(generator.getOptionalName().orElse(null))
                .id(generator.getId())
                .terminalConnected(terminal.isConnected())
                .voltageLevelId(terminal.getVoltageLevel().getId())
                .targetP(generator.getTargetP())
                .targetQ(nullIfNan(generator.getTargetQ()))
                .targetV(nullIfNan(generator.getTargetV()))
                .minP(generator.getMinP())
                .maxP(generator.getMaxP())
                .ratedS(nullIfNan(generator.getRatedS()))
                .energySource(generator.getEnergySource())
                .voltageRegulatorOn(generator.isVoltageRegulatorOn())
                .p(nullIfNan(terminal.getP()))
                .q(nullIfNan(terminal.getQ()))
                .properties(getProperties(generator));
        builder.busOrBusbarSectionId(getBusOrBusbarSection(terminal))
                .activePowerControl(ExtensionUtils.toActivePowerControl(generator));

        builder.generatorShortCircuit(ExtensionUtils.toShortCircuit(() -> generator.getExtension(GeneratorShortCircuit.class)))
                .generatorStartup(toGeneratorStartup(generator))
                .coordinatedReactiveControl(toCoordinatedReactiveControl(generator));

        Terminal regulatingTerminalForm = generator.getRegulatingTerminal();
        //If there is no regulating terminal in file, regulating terminal voltage level is equal to generator voltage level
        if (regulatingTerminalForm != null && !regulatingTerminalForm.getVoltageLevel().equals(terminal.getVoltageLevel())) {
            builder.regulatingTerminalVlId(regulatingTerminalForm.getVoltageLevel().getId());
            builder.regulatingTerminalConnectableType(regulatingTerminalForm.getConnectable().getType().name());
            builder.regulatingTerminalConnectableId(regulatingTerminalForm.getConnectable().getId());
            builder.regulatingTerminalVlName(regulatingTerminalForm.getVoltageLevel().getOptionalName().orElse(null));
        }
        ReactiveLimits reactiveLimits = generator.getReactiveLimits();
        if (reactiveLimits != null) {
            ReactiveLimitsKind limitsKind = reactiveLimits.getKind();
            if (limitsKind == ReactiveLimitsKind.MIN_MAX) {
                MinMaxReactiveLimits minMaxReactiveLimitsForm = generator.getReactiveLimits(MinMaxReactiveLimitsImpl.class);
                builder.minMaxReactiveLimits(MinMaxReactiveLimitsMapData.builder()
                        .maxQ(minMaxReactiveLimitsForm.getMaxQ())
                        .minQ(minMaxReactiveLimitsForm.getMinQ())
                        .build());
            } else if (limitsKind == ReactiveLimitsKind.CURVE) {
                ReactiveCapabilityCurve capabilityCurve = generator.getReactiveLimits(ReactiveCapabilityCurve.class);
                builder.reactiveCapabilityCurvePoints(getReactiveCapabilityCurvePoints(capabilityCurve.getPoints()));
            }
        }

        builder.connectablePosition(ExtensionUtils.toMapConnectablePosition(generator, 0));
        return builder.build();
    }

    private static GeneratorTooltipInfos toTooltipInfos(Identifiable<?> identifiable) {
        Generator generator = (Generator) identifiable;
        Terminal terminal = generator.getTerminal();
        GeneratorTooltipInfos.GeneratorTooltipInfosBuilder<?, ?> builder = GeneratorTooltipInfos.builder()
            .name(generator.getOptionalName().orElse(null))
            .id(generator.getId())
            .targetP(generator.getTargetP())
            .targetQ(nullIfNan(generator.getTargetQ()))
            .targetV(nullIfNan(generator.getTargetV()))
            .minP(generator.getMinP())
            .maxP(generator.getMaxP())
            .voltageRegulatorOn(generator.isVoltageRegulatorOn())
            .p(nullIfNan(terminal.getP()))
            .q(nullIfNan(terminal.getQ()));
        return builder.build();
    }

    private static CoordinatedReactiveControlInfos toCoordinatedReactiveControl(@NonNull final Generator generator) {
        final CoordinatedReactiveControl coordinatedReactiveControl = generator.getExtension(CoordinatedReactiveControl.class);
        return CoordinatedReactiveControlInfos.builder()
                .qPercent(coordinatedReactiveControl != null ? coordinatedReactiveControl.getQPercent() : Double.NaN)
                .build();
    }

    private static Optional<GeneratorStartupInfos> toGeneratorStartup(@NonNull final Generator generator) {
        return Optional.ofNullable((GeneratorStartup) generator.getExtension(GeneratorStartup.class))
                .map(generatorStartup -> GeneratorStartupInfos.builder()
                        .plannedActivePowerSetPoint(nullIfNan(generatorStartup.getPlannedActivePowerSetpoint()))
                        .marginalCost(nullIfNan(generatorStartup.getMarginalCost()))
                        .plannedOutageRate(nullIfNan(generatorStartup.getPlannedOutageRate()))
                        .forcedOutageRate(nullIfNan(generatorStartup.getForcedOutageRate()))
                        .build());
    }
}
