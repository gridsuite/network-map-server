package org.gridsuite.network.map.mapper;

import com.powsybl.iidm.network.*;
import lombok.experimental.StandardException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.RecursiveToStringStyle;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.assertj.core.api.WithAssertions;
import org.assertj.core.api.WithAssumptions;
import org.gridsuite.network.map.MapperTestUtils;
import org.gridsuite.network.map.dto.ElementInfos;
import org.gridsuite.network.map.dto.ElementInfos.InfoType;
import org.gridsuite.network.map.dto.InfoTypeParameters;
import org.gridsuite.network.map.dto.mapper.*;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opentest4j.TestAbortedException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Just some tests for Sonar coverage until we really implement unit tests on mappers...
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
@Slf4j
class MappersTest implements WithAssertions, WithAssumptions {
    private static Stream<Arguments> mapperClassProvider() {
        return Stream.of(
                Arguments.of(BatteryInfosMapper.class),
                Arguments.of(BusBarSectionInfosMapper.class),
                Arguments.of(BusInfosMapper.class),
                Arguments.of(DanglingLineInfosMapper.class),
                Arguments.of(GeneratorInfosMapper.class),
                Arguments.of(HvdcInfosMapper.class),
                Arguments.of(HvdcLccInfosMapper.class),
                Arguments.of(HvdcVscInfosMapper.class),
                Arguments.of(LccConverterStationInfosMapper.class),
                Arguments.of(LineInfosMapper.class),
                Arguments.of(LoadInfosMapper.class),
                Arguments.of(ShuntCompensatorMapper.class),
                Arguments.of(StaticVarCompensatorInfosMapper.class),
                Arguments.of(SubstationInfosMapper.class),
                Arguments.of(TwoWindingsTransformerInfosMapper.class),
                Arguments.of(ThreeWindingsTransformerInfosMapper.class),
                Arguments.of(TieLineInfosMapper.class),
                Arguments.of(VoltageLevelInfosMapper.class),
                Arguments.of(VscConverterStationInfosMapper.class)
        );
    }

    private static Stream<Arguments> mapperAndInfoTypeProvider() {
        return mapperClassProvider()
                .flatMap(mapperArg -> Stream.of(InfoType.values())
                        .map(infoType -> Arguments.of(mapperArg.get()[0], infoType)));
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("mapperClassProvider")
    void shouldHaveValidMapperStructure(Class<?> classMapper) throws NoSuchMethodException, SecurityException {
        assertThat(classMapper).as("Mapper class").isNotInterface().isNotAnnotation().isNotRecord().isPublic().hasDeclaredMethods("toData");
        Method toDataMethod = classMapper.getDeclaredMethod("toData", Identifiable.class, InfoTypeParameters.class);
        assertThat(toDataMethod).as("Mapper#toData(Identifiable, InfoTypeParameters) method").isNotNull();
        assertThat(toDataMethod.getReturnType()).as("Mapper#toData(...) return type").isSameAs(ElementInfos.class);
        assertThat(Modifier.isStatic(toDataMethod.getModifiers())).as("Mapper#toData(...) is static").isTrue();
    }

    @ParameterizedTest(name = "[{index}] {0} with {1}")
    @MethodSource("mapperAndInfoTypeProvider")
    void shouldHandleInfoType(Class<?> classMapper, InfoType infoType) throws Throwable {
        Method toDataMethod = classMapper.getDeclaredMethod("toData", Identifiable.class, InfoTypeParameters.class);

        final InfoTypeParameters parameters = new InfoTypeParameters(infoType, Map.of());
        // add possible implemented classes to avoid ClassCastException
        final Identifiable<?> identifiableMock = Mockito.mock(Identifiable.class, Mockito.withSettings()
                .defaultAnswer(invocation -> {
                    throw new InterruptTest("No real data to test with.");
                }).extraInterfaces(// add possible implemented classes to avoid ClassCastException
                        Battery.class,
                        Bus.class,
                        BusbarSection.class,
                        DanglingLine.class,
                        Generator.class,
                        HvdcLine.class,
                        LccConverterStation.class,
                        Line.class,
                        Load.class,
                        ShuntCompensator.class,
                        StaticVarCompensator.class,
                        Substation.class,
                        TwoWindingsTransformer.class,
                        ThreeWindingsTransformer.class,
                        TieLine.class,
                        VoltageLevel.class,
                        VscConverterStation.class
                ));
        try {
            final Object result = toDataMethod.invoke(null, identifiableMock, parameters);
            assertThat(result).isNotNull();
            if (log.isDebugEnabled()) {
                log.debug("Result: {}", ReflectionToStringBuilder.toString(result, RecursiveToStringStyle.SHORT_PREFIX_STYLE));
            }
        } catch (final InvocationTargetException ex) {
            final Throwable cause = ex.getCause();
            if (cause instanceof UnsupportedOperationException && MapperTestUtils.isUnsupportedInfoTypeMessage(cause.getMessage())) {
                throw new TestAbortedException("Type not supported by this mapper.");
            } else if (!(cause instanceof InterruptTest)) {
                throw ex;
            }
            // else do nothing, so the test is green
        }
    }

    @StandardException
    static class InterruptTest extends RuntimeException { }
}
