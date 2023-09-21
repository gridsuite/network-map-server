/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.model;

import com.powsybl.iidm.network.*;
import org.gridsuite.network.map.dto.ElementInfos;
import org.gridsuite.network.map.dto.mapper.BatteryInfosMapper;
import org.gridsuite.network.map.dto.mapper.BusBarSectionInfosMapper;
import org.gridsuite.network.map.dto.mapper.DanglingLineInfosMapper;
import org.gridsuite.network.map.dto.mapper.GeneratorInfosMapper;
import org.gridsuite.network.map.dto.mapper.HvdcInfosMapper;
import org.gridsuite.network.map.dto.mapper.LccConverterStationInfosMapper;
import org.gridsuite.network.map.dto.mapper.LineInfosMapper;
import org.gridsuite.network.map.dto.mapper.LoadInfosMapper;
import org.gridsuite.network.map.dto.mapper.ShuntCompensatorMapper;
import org.gridsuite.network.map.dto.mapper.StaticVarCompensatorInfosMapper;
import org.gridsuite.network.map.dto.mapper.SubstationInfosMapper;
import org.gridsuite.network.map.dto.mapper.ThreeWindingsTransformerInfosMapper;
import org.gridsuite.network.map.dto.mapper.TwoWindingsTransformerInfosMapper;
import org.gridsuite.network.map.dto.mapper.VoltageLevelInfosMapper;
import org.gridsuite.network.map.dto.mapper.VscConverterStationInfosMapper;
import org.gridsuite.network.map.dto.ElementInfos.ElementInfoType;
import java.util.function.BiFunction;


public enum ElementType {
    SUBSTATION(Substation.class, SubstationInfosMapper::toData),
    VOLTAGE_LEVEL(VoltageLevel.class, VoltageLevelInfosMapper::toData),
    LINE(Line.class, LineInfosMapper::toData),
    HVDC_LINE(HvdcLine.class, HvdcInfosMapper::toData),
    LOAD(Load.class, LoadInfosMapper::toData),
    TWO_WINDINGS_TRANSFORMER(TwoWindingsTransformer.class, TwoWindingsTransformerInfosMapper::toData),
    THREE_WINDINGS_TRANSFORMER(ThreeWindingsTransformer.class, ThreeWindingsTransformerInfosMapper::toData),
    BUSBAR_SECTION(BusbarSection.class, BusBarSectionInfosMapper::toData),
    GENERATOR(Generator.class, GeneratorInfosMapper::toData),
    BATTERY(Battery.class, BatteryInfosMapper::toData),
    SHUNT_COMPENSATOR(ShuntCompensator.class, ShuntCompensatorMapper::toData),
    DANGLING_LINE(DanglingLine.class, DanglingLineInfosMapper::toData),
    STATIC_VAR_COMPENSATOR(StaticVarCompensator.class, StaticVarCompensatorInfosMapper::toData),
    LCC_CONVERTER_STATION(LccConverterStation.class, LccConverterStationInfosMapper::toData),
    VSC_CONVERTER_STATION(VscConverterStation.class, VscConverterStationInfosMapper::toData);

    private final Class<? extends Identifiable> elementClass;

    private final BiFunction<Identifiable<?>, ElementInfoType, ElementInfos> infosGetter;

    public Class<? extends Identifiable> getElementClass() {
        return elementClass;
    }

    public BiFunction<Identifiable<?>, ElementInfoType, ElementInfos> getInfosGetter() {
        return infosGetter;
    }

    ElementType(Class<? extends Identifiable> typeClass, BiFunction<Identifiable<?>, ElementInfoType, ElementInfos> dataGetter) {
        this.elementClass = typeClass;
        this.infosGetter = dataGetter;
    }
}

