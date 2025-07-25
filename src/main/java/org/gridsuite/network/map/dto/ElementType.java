/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto;

import com.powsybl.iidm.network.*;
import lombok.Getter;
import lombok.NonNull;
import org.gridsuite.network.map.dto.mapper.*;
import org.springframework.lang.Nullable;

import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Stream;

public enum ElementType {
    SUBSTATION(Substation.class, SubstationInfosMapper::toData),
    VOLTAGE_LEVEL(VoltageLevel.class, VoltageLevelInfosMapper::toData),
    BRANCH(Branch.class, BranchInfosMapper::toData, Line.class, TwoWindingsTransformer.class),
    LINE(Line.class, LineInfosMapper::toData),
    TIE_LINE(TieLine.class, TieLineInfosMapper::toData),
    HVDC_LINE(HvdcLine.class, HvdcInfosMapper::toData),
    HVDC_LINE_LCC(HvdcLine.class, HvdcLccInfosMapper::toData),
    HVDC_LINE_VSC(HvdcLine.class, HvdcVscInfosMapper::toData),
    LOAD(Load.class, LoadInfosMapper::toData),
    TWO_WINDINGS_TRANSFORMER(TwoWindingsTransformer.class, TwoWindingsTransformerInfosMapper::toData),
    THREE_WINDINGS_TRANSFORMER(ThreeWindingsTransformer.class, ThreeWindingsTransformerInfosMapper::toData),
    BUSBAR_SECTION(BusbarSection.class, BusBarSectionInfosMapper::toData),
    BUS(Bus.class, BusInfosMapper::toData),
    GENERATOR(Generator.class, GeneratorInfosMapper::toData),
    BATTERY(Battery.class, BatteryInfosMapper::toData),
    SHUNT_COMPENSATOR(ShuntCompensator.class, ShuntCompensatorMapper::toData),
    DANGLING_LINE(DanglingLine.class, DanglingLineInfosMapper::toData),
    STATIC_VAR_COMPENSATOR(StaticVarCompensator.class, StaticVarCompensatorInfosMapper::toData),
    LCC_CONVERTER_STATION(LccConverterStation.class, LccConverterStationInfosMapper::toData),
    VSC_CONVERTER_STATION(VscConverterStation.class, VscConverterStationInfosMapper::toData);

    /**
     * @since 2.21.0
     * @apiNote Is {@code null} if it's not a concrete class.
     */
    @NonNull private final Class<? extends Identifiable<?>> elementClass;

    @Nullable private final Set<Class<? extends Identifiable<?>>> subClasses;

    /**
     * Is {@link #elementClass}, or all {@link #subClasses} if present, is an instance of {@link Connectable}?
     * @apiNote Is only to not have to do the test each time the information is needed.
     * @since 2.21.0
     */
    @Getter final boolean isConnectable;

    @Getter @NonNull
    private final BiFunction</*? extends*/ Identifiable<?>, InfoTypeParameters, ElementInfos> infosGetter;

    @SafeVarargs
    <T extends Identifiable<T>> ElementType(
            @NonNull final Class<T> typeClass,
            @NonNull final BiFunction<T, InfoTypeParameters, ElementInfos> dataGetter,
            @Nullable final Class</*? extends T*/? extends Identifiable<?>>... subTypes) {
        this.elementClass = typeClass;
        //noinspection unchecked
        this.infosGetter = (BiFunction<Identifiable<?>, InfoTypeParameters, ElementInfos>) dataGetter;
        if (subTypes != null && subTypes.length > 0) {
            this.subClasses = Set.of(subTypes);
            if (subClasses.stream().anyMatch(subType -> !typeClass.isAssignableFrom(subType))) {
                // typing the list as Class<? extends T> to check during compile-time don't work, so let's at least check during class loading
                throw new VerifyError("Not all sub-classes extends base class");
            }
            this.isConnectable = subClasses.stream().allMatch(Connectable.class::isAssignableFrom);
        } else {
            this.subClasses = null;
            this.isConnectable = Connectable.class.isAssignableFrom(this.elementClass);
        }
    }

    public Stream<Connectable<?>> getVoltageLevelConnectableStream(@NonNull final VoltageLevel voltageLevel) {
        if (this.isConnectable) {
            throw new UnsupportedOperationException("This element type is not a connectable type");
        }
        if (this.subClasses == null) {
            return voltageLevel.getConnectableStream((Class<Connectable<?>>) this.elementClass);
        } else {
            return this.subClasses.stream()
                    .map(c -> (Class<? extends Connectable<?>>) c)
                    .flatMap(voltageLevel::getConnectableStream);
        }
    }
}
