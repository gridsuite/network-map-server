/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.mapper;

import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.extensions.Measurement.Type;
import com.powsybl.iidm.network.extensions.StandbyAutomaton;
import org.gridsuite.network.map.dto.ElementInfos;
import org.gridsuite.network.map.dto.InfoTypeParameters;
import org.gridsuite.network.map.dto.definition.extension.StandbyAutomatonInfos;
import org.gridsuite.network.map.dto.definition.staticvarcompensator.StaticVarCompensatorFormInfos;
import org.gridsuite.network.map.dto.definition.staticvarcompensator.StaticVarCompensatorTabInfos;
import org.gridsuite.network.map.dto.utils.ElementUtils;
import org.gridsuite.network.map.dto.utils.ExtensionUtils;
import org.springframework.lang.NonNull;

import java.util.Optional;

import static org.gridsuite.network.map.dto.utils.ElementUtils.*;

/**
 * @author AJELLAL Ali <ali.ajellal@rte-france.com>
 */
public final class StaticVarCompensatorInfosMapper {
    private StaticVarCompensatorInfosMapper() {
    }

    public static ElementInfos toData(Identifiable<?> identifiable, InfoTypeParameters infoTypeParameters) {
        return switch (infoTypeParameters.getInfoType()) {
            case TAB -> toTabInfos(identifiable);
            case FORM -> toFormInfos(identifiable);
            case LIST -> ElementInfosMapper.toInfosWithType(identifiable);
            default -> throw new UnsupportedOperationException(
                    "InfoType '" + infoTypeParameters.getInfoType() + "' is not supported for StaticVarCompensator elements"
            );
        };
    }

    private static StaticVarCompensatorFormInfos toFormInfos(Identifiable<?> identifiable) {
        StaticVarCompensator staticVarCompensator = (StaticVarCompensator) identifiable;
        Terminal terminal = staticVarCompensator.getTerminal();
        StaticVarCompensatorFormInfos.StaticVarCompensatorFormInfosBuilder<?, ?> builder = StaticVarCompensatorFormInfos.builder()
                .name(staticVarCompensator.getOptionalName().orElse(null))
                .id(staticVarCompensator.getId())
                .terminalConnected(terminal.isConnected())
                .voltageLevelId(terminal.getVoltageLevel().getId())
                .nominalV(nullIfNan(terminal.getVoltageLevel().getNominalV()))
                .regulationMode(staticVarCompensator.getRegulationMode())
                .isRegulating(staticVarCompensator.isRegulating())
                .maxSusceptance(nullIfNan(staticVarCompensator.getBmax()))
                .minSusceptance(nullIfNan(staticVarCompensator.getBmin()))
                .voltageSetpoint(nullIfNan(staticVarCompensator.getVoltageSetpoint()))
                .reactivePowerSetpoint(nullIfNan(staticVarCompensator.getReactivePowerSetpoint()))
                .busOrBusbarSectionId(getBusOrBusbarSection(terminal))
                .properties(getProperties(staticVarCompensator))
                .connectablePosition(ExtensionUtils.toMapConnectablePosition(staticVarCompensator, 0))
                .standbyAutomatonInfos(toStandbyAutomaton(staticVarCompensator));
        Terminal regulatingTerminal = staticVarCompensator.getRegulatingTerminal();
        //If there is no regulating terminal in file, regulating terminal voltage level is equal to cspr voltage level
        if (regulatingTerminal != null && !regulatingTerminal.getVoltageLevel().equals(terminal.getVoltageLevel())) {
            builder.regulatingTerminalVlId(regulatingTerminal.getVoltageLevel().getId());
            builder.regulatingTerminalConnectableType(regulatingTerminal.getConnectable().getType().name());
            builder.regulatingTerminalConnectableId(regulatingTerminal.getConnectable().getId());
        }
        return builder.build();
    }

    private static StaticVarCompensatorTabInfos toTabInfos(Identifiable<?> identifiable) {
        StaticVarCompensator staticVarCompensator = (StaticVarCompensator) identifiable;
        Terminal terminal = staticVarCompensator.getTerminal();
        StaticVarCompensatorTabInfos.StaticVarCompensatorTabInfosBuilder<?, ?> builder = StaticVarCompensatorTabInfos.builder()
                .name(staticVarCompensator.getOptionalName().orElse(null))
                .id(staticVarCompensator.getId())
                .terminalConnected(terminal.isConnected())
                .voltageLevelId(terminal.getVoltageLevel().getId())
                .nominalV(terminal.getVoltageLevel().getNominalV())
                .country(mapCountry(terminal.getVoltageLevel().getSubstation().orElse(null)))
                .properties(getProperties(staticVarCompensator))
                .regulationMode(staticVarCompensator.getRegulationMode());

        if (!Double.isNaN(terminal.getQ())) {
            builder.q(terminal.getQ());
        }
        if (!Double.isNaN(terminal.getP())) {
            builder.p(terminal.getP());
        }
        if (!Double.isNaN(staticVarCompensator.getVoltageSetpoint())) {
            builder.voltageSetpoint(staticVarCompensator.getVoltageSetpoint());
        }
        if (!Double.isNaN(staticVarCompensator.getReactivePowerSetpoint())) {
            builder.reactivePowerSetpoint(staticVarCompensator.getReactivePowerSetpoint());
        }

        // voltageLevel and substation properties
        builder.voltageLevelProperties(getProperties(terminal.getVoltageLevel()));
        builder.substationProperties(terminal.getVoltageLevel().getSubstation().map(ElementUtils::getProperties).orElse(null));

        builder.measurementQ(ExtensionUtils.toMeasurement(staticVarCompensator, Type.REACTIVE_POWER, 0));

        builder.injectionObservability(ExtensionUtils.toInjectionObservability(staticVarCompensator));

        return builder.build();
    }

    private static Optional<StandbyAutomatonInfos> toStandbyAutomaton(@NonNull final StaticVarCompensator staticVarCompensator) {
        return Optional.ofNullable((StandbyAutomaton) staticVarCompensator.getExtension(StandbyAutomaton.class))
                .map(standbyAutomatonInfos -> StandbyAutomatonInfos.builder()
                        .standby(standbyAutomatonInfos.isStandby())
                        .b0(nullIfNan(standbyAutomatonInfos.getB0()))
                        .lowVoltageSetpoint(nullIfNan(standbyAutomatonInfos.getLowVoltageSetpoint()))
                        .highVoltageSetpoint(nullIfNan(standbyAutomatonInfos.getHighVoltageSetpoint()))
                        .highVoltageThreshold(nullIfNan(standbyAutomatonInfos.getHighVoltageThreshold()))
                        .lowVoltageThreshold(nullIfNan(standbyAutomatonInfos.getLowVoltageThreshold())).build());
    }
}
