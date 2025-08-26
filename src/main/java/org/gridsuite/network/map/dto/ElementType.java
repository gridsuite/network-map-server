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
import java.util.function.Function;
import java.util.stream.Stream;

public enum ElementType {
    SUBSTATION(Substation.class, SubstationInfosMapper::toData, null),
    VOLTAGE_LEVEL(VoltageLevel.class, VoltageLevelInfosMapper::toData, null),
    BRANCH(Branch.class, BranchInfosMapper::toData,
        network -> Stream.concat(network.getLineStream(), network.getTwoWindingsTransformerStream()),
        Line.class, TwoWindingsTransformer.class),
    LINE(Line.class, LineInfosMapper::toData, Network::getLineStream),
    TIE_LINE(TieLine.class, TieLineInfosMapper::toData, null),
    HVDC_LINE(HvdcLine.class, HvdcInfosMapper::toData, null),
    HVDC_LINE_LCC(HvdcLine.class, HvdcLccInfosMapper::toData, null),
    HVDC_LINE_VSC(HvdcLine.class, HvdcVscInfosMapper::toData, null),
    LOAD(Load.class, LoadInfosMapper::toData, Network::getLoadStream),
    TWO_WINDINGS_TRANSFORMER(TwoWindingsTransformer.class, TwoWindingsTransformerInfosMapper::toData, Network::getTwoWindingsTransformerStream),
    THREE_WINDINGS_TRANSFORMER(ThreeWindingsTransformer.class, ThreeWindingsTransformerInfosMapper::toData, Network::getThreeWindingsTransformerStream),
    BUSBAR_SECTION(BusbarSection.class, BusBarSectionInfosMapper::toData, Network::getBusbarSectionStream),
    BUS(Bus.class, BusInfosMapper::toData, Network::getBusbarSectionStream),
    GENERATOR(Generator.class, GeneratorInfosMapper::toData, Network::getGeneratorStream),
    BATTERY(Battery.class, BatteryInfosMapper::toData, Network::getBatteryStream),
    SHUNT_COMPENSATOR(ShuntCompensator.class, ShuntCompensatorMapper::toData, Network::getShuntCompensatorStream),
    DANGLING_LINE(DanglingLine.class, DanglingLineInfosMapper::toData, Network::getDanglingLineStream),
    STATIC_VAR_COMPENSATOR(StaticVarCompensator.class, StaticVarCompensatorInfosMapper::toData, Network::getStaticVarCompensatorStream),
    LCC_CONVERTER_STATION(LccConverterStation.class, LccConverterStationInfosMapper::toData, Network::getLccConverterStationStream),
    VSC_CONVERTER_STATION(VscConverterStation.class, VscConverterStationInfosMapper::toData, Network::getVscConverterStationStream);

    /**
     * @since 2.22.0
     * @apiNote Is {@code null} if it's not a concrete class.
     */
    @NonNull private final Class<? extends Identifiable<?>> elementClass;

    /**
     * For composed element classes
     * @since 2.22.0
     */
    @Nullable private final Set<Class<? extends Identifiable<?>>> subClasses;

    /**
     * Is {@link #elementClass}, or all {@link #subClasses} if present, is an instance of {@link Connectable}?
     * @apiNote Is only to not have to do the test each time the information is needed.
     * @since 2.22.0
     */
    @Getter final boolean isConnectable;

    /**
     * {@code toData()} function from the mapper
     */
    @Getter @NonNull
    private final BiFunction</*? extends*/ Identifiable<?>, InfoTypeParameters, ElementInfos> infosGetter;

    /**
     * @since 2.22.0
     */
    @NonNull
    private final Function<Network, Stream</*? extends*/ Connectable<?>>> connectableStream;

    @SafeVarargs
    <T extends Identifiable<T>> ElementType(
            @NonNull final Class<T> typeClass,
            @NonNull final BiFunction<T, InfoTypeParameters, ElementInfos> dataGetter,
            @Nullable final Function<Network, Stream</*T*/ ? extends Connectable<?>>> connectableStream,
            @Nullable final Class</*? extends T*/? extends Identifiable<?>>... subTypes) {
        this.connectableStream = connectableStream != null ? ((Function<Network, Stream<Connectable<?>>>) (Object) connectableStream) : (n -> {
            throw new IllegalStateException("Unexpected connectable type:" + this);
        });
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
        if (!this.isConnectable) {
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

    public Stream<Connectable<?>> getConnectableStream(final Network network) {
        return this.connectableStream.apply(network);
    }
}
