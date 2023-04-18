/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.model;

import com.powsybl.iidm.network.*;
import org.gridsuite.network.map.dto.ElementInfos;
import org.gridsuite.network.map.dto.hvdc.HvdcInfos;
import org.gridsuite.network.map.dto.load.LoadInfos;
import org.gridsuite.network.map.dto.substation.SubstationInfos;
import org.gridsuite.network.map.dto.voltagelevel.VoltageLevelInfos;

import java.util.function.BiFunction;


public enum ElementType {
    SUBSTATION(IdentifiableType.NETWORK, Substation.class, SubstationInfos::toData),
    VOLTAGE_LEVEL(IdentifiableType.VOLTAGE_LEVEL, VoltageLevel.class, VoltageLevelInfos::toData),
    HVDC_LINE(IdentifiableType.HVDC_LINE, HvdcLine.class, HvdcInfos::toData),
    BUSBAR_SECTION(IdentifiableType.BUSBAR_SECTION, BusbarSection.class, LoadInfos::toData),
    LINE(IdentifiableType.LINE, Line.class, LoadInfos::toData),
    TWO_WINDINGS_TRANSFORMER(IdentifiableType.TWO_WINDINGS_TRANSFORMER, TwoWindingsTransformer.class, LoadInfos::toData),
    THREE_WINDINGS_TRANSFORMER(IdentifiableType.THREE_WINDINGS_TRANSFORMER, ThreeWindingsTransformer.class, LoadInfos::toData),
    GENERATOR(IdentifiableType.GENERATOR, Generator.class, LoadInfos::toData),
    BATTERY(IdentifiableType.BATTERY, Battery.class, LoadInfos::toData),
    LOAD(IdentifiableType.LOAD, Load.class, LoadInfos::toData),
    SHUNT_COMPENSATOR(IdentifiableType.SHUNT_COMPENSATOR, ShuntCompensator.class, LoadInfos::toData),
    DANGLING_LINE(IdentifiableType.DANGLING_LINE, DanglingLine.class, LoadInfos::toData),
    STATIC_VAR_COMPENSATOR(IdentifiableType.STATIC_VAR_COMPENSATOR, StaticVarCompensator.class, LoadInfos::toData),
    LCC_CONVERTER_STATION(IdentifiableType.HVDC_CONVERTER_STATION, LccConverterStation.class, LoadInfos::toData),
    VSC_CONVERTER_STATION(IdentifiableType.HVDC_CONVERTER_STATION, VscConverterStation.class, LoadInfos::toData);

    private final IdentifiableType identifiableType;

    private final Class<? extends Identifiable> elementClass;

    private final BiFunction<Identifiable, ElementInfos.InfoType, ElementInfos> infosGetter;

    public IdentifiableType getIdentifiableType() {
        return identifiableType;
    }

    public Class<? extends Identifiable> getElementClass() {
        return elementClass;
    }

    public BiFunction<Identifiable, ElementInfos.InfoType, ElementInfos> getInfosGetter() {
        return infosGetter;
    }

    ElementType(IdentifiableType identifiableType, Class<? extends Identifiable> typeClass, BiFunction<Identifiable, ElementInfos.InfoType, ElementInfos> dataGetter) {
        this.identifiableType = identifiableType;
        this.elementClass = typeClass;
        this.infosGetter = dataGetter;
    }
}

