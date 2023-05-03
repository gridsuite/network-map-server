/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.model;

import com.powsybl.iidm.network.*;
import org.gridsuite.network.map.dto.ElementInfos;
import org.gridsuite.network.map.dto.hvdc.AbstractHvdcInfos;
import org.gridsuite.network.map.dto.line.AbstractLineInfos;
import org.gridsuite.network.map.dto.load.AbstractLoadInfos;
import org.gridsuite.network.map.dto.substation.AbstractSubstationInfos;
import org.gridsuite.network.map.dto.threewindingstransformer.AbstractThreeWindingsTransformerInfos;
import org.gridsuite.network.map.dto.twowindingstransformer.AbstractTwoWindingsTransformerInfos;
import org.gridsuite.network.map.dto.voltagelevel.AbstractVoltageLevelInfos;

import java.util.function.BiFunction;


public enum ElementType {
    SUBSTATION(IdentifiableType.NETWORK, Substation.class, AbstractSubstationInfos::toData),
    VOLTAGE_LEVEL(IdentifiableType.VOLTAGE_LEVEL, VoltageLevel.class, AbstractVoltageLevelInfos::toData),
    LINE(IdentifiableType.LINE, Line.class, AbstractLineInfos::toData),
    HVDC_LINE(IdentifiableType.HVDC_LINE, HvdcLine.class, AbstractHvdcInfos::toData),
    LOAD(IdentifiableType.LOAD, Load.class, AbstractLoadInfos::toData),
    TWO_WINDINGS_TRANSFORMER(IdentifiableType.TWO_WINDINGS_TRANSFORMER, TwoWindingsTransformer.class, AbstractTwoWindingsTransformerInfos::toData),
    THREE_WINDINGS_TRANSFORMER(IdentifiableType.THREE_WINDINGS_TRANSFORMER, ThreeWindingsTransformer.class, AbstractThreeWindingsTransformerInfos::toData),

    BUSBAR_SECTION(IdentifiableType.BUSBAR_SECTION, BusbarSection.class, AbstractLoadInfos::toData),
    GENERATOR(IdentifiableType.GENERATOR, Generator.class, AbstractLoadInfos::toData),
    BATTERY(IdentifiableType.BATTERY, Battery.class, AbstractLoadInfos::toData),
    SHUNT_COMPENSATOR(IdentifiableType.SHUNT_COMPENSATOR, ShuntCompensator.class, AbstractLoadInfos::toData),
    DANGLING_LINE(IdentifiableType.DANGLING_LINE, DanglingLine.class, AbstractLoadInfos::toData),
    STATIC_VAR_COMPENSATOR(IdentifiableType.STATIC_VAR_COMPENSATOR, StaticVarCompensator.class, AbstractLoadInfos::toData),
    LCC_CONVERTER_STATION(IdentifiableType.HVDC_CONVERTER_STATION, LccConverterStation.class, AbstractLoadInfos::toData),
    VSC_CONVERTER_STATION(IdentifiableType.HVDC_CONVERTER_STATION, VscConverterStation.class, AbstractLoadInfos::toData);

    private final IdentifiableType identifiableType;

    private final Class<? extends Identifiable> elementClass;

    private final BiFunction<Identifiable<?>, ElementInfos.InfoType, ElementInfos> infosGetter;

    public IdentifiableType getIdentifiableType() {
        return identifiableType;
    }

    public Class<? extends Identifiable> getElementClass() {
        return elementClass;
    }

    public BiFunction<Identifiable<?>, ElementInfos.InfoType, ElementInfos> getInfosGetter() {
        return infosGetter;
    }

    ElementType(IdentifiableType identifiableType, Class<? extends Identifiable> typeClass, BiFunction<Identifiable<?>, ElementInfos.InfoType, ElementInfos> dataGetter) {
        this.identifiableType = identifiableType;
        this.elementClass = typeClass;
        this.infosGetter = dataGetter;
    }
}

