/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.model;

import com.powsybl.iidm.network.*;



public enum EquipmentType {
    NETWORK(IdentifiableType.NETWORK, Network.class),
    SUBSTATION(IdentifiableType.NETWORK, Substation.class),
    VOLTAGE_LEVEL(IdentifiableType.VOLTAGE_LEVEL, VoltageLevel.class),
    HVDC_LINE(IdentifiableType.HVDC_LINE, HvdcLine.class),
    BUSBAR_SECTION(IdentifiableType.BUSBAR_SECTION, BusbarSection.class),
    LINE(IdentifiableType.LINE, Line.class),
    TWO_WINDINGS_TRANSFORMER(IdentifiableType.TWO_WINDINGS_TRANSFORMER, TwoWindingsTransformer.class),
    THREE_WINDINGS_TRANSFORMER(IdentifiableType.THREE_WINDINGS_TRANSFORMER, ThreeWindingsTransformer.class),
    GENERATOR(IdentifiableType.GENERATOR, Generator.class),
    BATTERY(IdentifiableType.BATTERY, Battery.class),
    LOAD(IdentifiableType.LOAD, Load.class),
    SHUNT_COMPENSATOR(IdentifiableType.SHUNT_COMPENSATOR, ShuntCompensator.class),
    DANGLING_LINE(IdentifiableType.DANGLING_LINE, DanglingLine.class),
    STATIC_VAR_COMPENSATOR(IdentifiableType.STATIC_VAR_COMPENSATOR, StaticVarCompensator.class),
    LCC_CONVERTER_STATION(IdentifiableType.HVDC_CONVERTER_STATION, LccConverterStation.class),
    VSC_CONVERTER_STATION(IdentifiableType.HVDC_CONVERTER_STATION, VscConverterStation.class);

    private final IdentifiableType identifiableType;

    private final Class<? extends Identifiable> typeClass;

    public IdentifiableType getIdentifiableType() {
        return identifiableType;
    }

    public Class<? extends Identifiable> getTypeClass() {
        return typeClass;
    }

    EquipmentType(IdentifiableType identifiableType, Class<? extends Identifiable> typeClass) {
        this.identifiableType = identifiableType;
        this.typeClass = typeClass;
    }
}

