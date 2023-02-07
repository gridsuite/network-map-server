package org.gridsuite.network.map.model;

import com.powsybl.iidm.network.*;

public enum EquipmentType {
    NETWORK(IdentifiableType.NETWORK.name(), Network.class),
    SUBSTATION(IdentifiableType.NETWORK.name(), Substation.class),
    VOLTAGE_LEVEL(IdentifiableType.VOLTAGE_LEVEL.name(), VoltageLevel.class),
    HVDC_LINE(IdentifiableType.HVDC_LINE.name(), HvdcLine.class),
    BUSBAR_SECTION(IdentifiableType.BUSBAR_SECTION.name(), BusbarSection.class),
    LINE(IdentifiableType.LINE.name(), Line.class),
    TWO_WINDINGS_TRANSFORMER(IdentifiableType.TWO_WINDINGS_TRANSFORMER.name(), TwoWindingsTransformer.class),
    THREE_WINDINGS_TRANSFORMER(IdentifiableType.THREE_WINDINGS_TRANSFORMER.name(), ThreeWindingsTransformer.class),
    GENERATOR(IdentifiableType.GENERATOR.name(), Generator.class),
    BATTERY(IdentifiableType.BATTERY.name(), Battery.class),
    LOAD(IdentifiableType.LOAD.name(), Load.class),
    SHUNT_COMPENSATOR(IdentifiableType.SHUNT_COMPENSATOR.name(), ShuntCompensator.class),
    DANGLING_LINE(IdentifiableType.DANGLING_LINE.name(), DanglingLine.class),
    STATIC_VAR_COMPENSATOR(IdentifiableType.STATIC_VAR_COMPENSATOR.name(), StaticVarCompensator.class),
    LCC_CONVERTER_STATION("LCC_CONVERTER_STATION", LccConverterStation.class),
    VSC_CONVERTER_STATION("VSC_CONVERTER_STATION", VscConverterStation.class);

    private String typeName;

    private Class<? extends Identifiable> typeClass;

    public Class<? extends Identifiable> getTypeClass() {
        return typeClass;
    }

    public String getName() {
        return typeName;
    }

    private EquipmentType(String typeName, Class<? extends Identifiable> typeClass) {
        this.typeName = typeName;
        this.typeClass = typeClass;
    }

}
