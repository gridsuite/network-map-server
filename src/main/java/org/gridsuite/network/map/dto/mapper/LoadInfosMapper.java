/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.mapper;

import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.extensions.Measurement;
import org.gridsuite.network.map.dto.ElementInfos;
import org.gridsuite.network.map.dto.InfoTypeParameters;
import org.gridsuite.network.map.dto.definition.load.LoadFormInfos;
import org.gridsuite.network.map.dto.definition.load.LoadTabInfos;
import org.gridsuite.network.map.dto.utils.ElementUtils;

import static org.gridsuite.network.map.dto.utils.ElementUtils.*;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
public final class LoadInfosMapper {

    private LoadInfosMapper() {
    }

    public static ElementInfos toData(Identifiable<?> identifiable, InfoTypeParameters infoTypeParameters) {
        switch (infoTypeParameters.getInfoType()) {
            case TAB:
                return toTabInfos(identifiable);
            case FORM:
                return toFormInfos(identifiable);
            case LIST:
                return ElementInfosMapper.toInfosWithType(identifiable);
            default:
                throw new UnsupportedOperationException("TODO");
        }
    }

    private static LoadFormInfos toFormInfos(Identifiable<?> identifiable) {
        Load load = (Load) identifiable;
        Terminal terminal = load.getTerminal();
        LoadFormInfos.LoadFormInfosBuilder<?, ?> builder = LoadFormInfos.builder()
                .name(load.getOptionalName().orElse(null))
                .id(load.getId())
                .type(load.getLoadType())
                .terminalConnected(terminal.isConnected())
                .voltageLevelId(terminal.getVoltageLevel().getId())
                .p0(load.getP0())
                .q0(load.getQ0())
                .properties(getProperties(load));

        if (!Double.isNaN(terminal.getP())) {
            builder.p(terminal.getP());
        }
        if (!Double.isNaN(terminal.getQ())) {
            builder.q(terminal.getQ());
        }

        builder.busOrBusbarSectionId(getBusOrBusbarSection(terminal));
        builder.connectablePosition(toMapConnectablePosition(load, 0));

        builder.measurementP(toMeasurement(load, Measurement.Type.ACTIVE_POWER, 0))
            .measurementQ(toMeasurement(load, Measurement.Type.REACTIVE_POWER, 0));

        return builder.build();
    }

    private static LoadTabInfos toTabInfos(Identifiable<?> identifiable) {
        Load load = (Load) identifiable;
        Terminal terminal = load.getTerminal();
        LoadTabInfos.LoadTabInfosBuilder<?, ?> builder = LoadTabInfos.builder()
                .name(load.getOptionalName().orElse(null))
                .id(load.getId())
                .type(load.getLoadType())
                .terminalConnected(terminal.isConnected())
                .voltageLevelId(terminal.getVoltageLevel().getId())
                .nominalVoltage(terminal.getVoltageLevel().getNominalV())
                .country(mapCountry(terminal.getVoltageLevel().getSubstation().orElse(null)))
                .substationId(terminal.getVoltageLevel().getSubstation().map(Substation::getId).orElse(null))
                .p0(load.getP0())
                .properties(getProperties(load))
                .q0(load.getQ0());

        if (!Double.isNaN(terminal.getP())) {
            builder.p(terminal.getP());
        }
        if (!Double.isNaN(terminal.getQ())) {
            builder.q(terminal.getQ());
        }

        builder.connectablePosition(toMapConnectablePosition(load, 0));

        // voltageLevel and substation properties
        builder.voltageLevelProperties(getProperties(terminal.getVoltageLevel()));
        builder.substationProperties(terminal.getVoltageLevel().getSubstation().map(ElementUtils::getProperties).orElse(null));

        builder.measurementP(toMeasurement(load, Measurement.Type.ACTIVE_POWER, 0))
            .measurementQ(toMeasurement(load, Measurement.Type.REACTIVE_POWER, 0));

        builder.injectionObservability(toInjectionObservability(load));

        return builder.build();
    }
}
