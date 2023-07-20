/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.model;

import com.powsybl.iidm.network.*;
import org.gridsuite.network.map.dto.ElementInfos;
import org.gridsuite.network.map.dto.battery.AbstractBatteryInfos;
import org.gridsuite.network.map.dto.busbarsection.AbstractBusBarSectionInfos;
import org.gridsuite.network.map.dto.danglingline.AbstractDanglingLineInfos;
import org.gridsuite.network.map.dto.generator.AbstractGeneratorInfos;
import org.gridsuite.network.map.dto.hvdc.AbstractHvdcInfos;
import org.gridsuite.network.map.dto.lccconverterstation.AbstractLccConverterStationInfos;
import org.gridsuite.network.map.dto.line.AbstractLineInfos;
import org.gridsuite.network.map.dto.load.AbstractLoadInfos;
import org.gridsuite.network.map.dto.shuntcompensator.AbstractShuntCompensator;
import org.gridsuite.network.map.dto.staticvarcompensator.AbstractStaticVarCompensatorInfos;
import org.gridsuite.network.map.dto.substation.AbstractSubstationInfos;
import org.gridsuite.network.map.dto.threewindingstransformer.AbstractThreeWindingsTransformerInfos;
import org.gridsuite.network.map.dto.twowindingstransformer.AbstractTwoWindingsTransformerInfos;
import org.gridsuite.network.map.dto.voltagelevel.AbstractVoltageLevelInfos;
import org.gridsuite.network.map.dto.vscconverterstation.AbstractVscConverterStationInfos;

import java.util.function.BiFunction;


public enum ElementType {
    SUBSTATION(Substation.class, AbstractSubstationInfos::toData),
    VOLTAGE_LEVEL(VoltageLevel.class, AbstractVoltageLevelInfos::toData),
    LINE(Line.class, AbstractLineInfos::toData),
    HVDC_LINE(HvdcLine.class, AbstractHvdcInfos::toData),
    LOAD(Load.class, AbstractLoadInfos::toData),
    TWO_WINDINGS_TRANSFORMER(TwoWindingsTransformer.class, AbstractTwoWindingsTransformerInfos::toData),
    THREE_WINDINGS_TRANSFORMER(ThreeWindingsTransformer.class, AbstractThreeWindingsTransformerInfos::toData),
    BUSBAR_SECTION(BusbarSection.class, AbstractBusBarSectionInfos::toData),
    GENERATOR(Generator.class, AbstractGeneratorInfos::toData),
    BATTERY(Battery.class, AbstractBatteryInfos::toData),
    SHUNT_COMPENSATOR(ShuntCompensator.class, AbstractShuntCompensator::toData),
    DANGLING_LINE(DanglingLine.class, AbstractDanglingLineInfos::toData),
    STATIC_VAR_COMPENSATOR(StaticVarCompensator.class, AbstractStaticVarCompensatorInfos::toData),
    LCC_CONVERTER_STATION(LccConverterStation.class, AbstractLccConverterStationInfos::toData),
    VSC_CONVERTER_STATION(VscConverterStation.class, AbstractVscConverterStationInfos::toData);

    private final Class<? extends Identifiable> elementClass;

    private final BiFunction<Identifiable<?>, ElementInfos.InfoType, ElementInfos> infosGetter;

    public Class<? extends Identifiable> getElementClass() {
        return elementClass;
    }

    public BiFunction<Identifiable<?>, ElementInfos.InfoType, ElementInfos> getInfosGetter() {
        return infosGetter;
    }

    ElementType(Class<? extends Identifiable> typeClass, BiFunction<Identifiable<?>, ElementInfos.InfoType, ElementInfos> dataGetter) {
        this.elementClass = typeClass;
        this.infosGetter = dataGetter;
    }
}

