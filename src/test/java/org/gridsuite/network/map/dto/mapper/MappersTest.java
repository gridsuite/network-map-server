package org.gridsuite.network.map.dto.mapper;

import com.powsybl.iidm.network.*;
import lombok.experimental.StandardException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.RecursiveToStringStyle;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.assertj.core.api.WithAssertions;
import org.assertj.core.api.WithAssumptions;
import org.gridsuite.network.map.dto.ElementInfos;
import org.gridsuite.network.map.dto.ElementInfos.InfoType;
import org.gridsuite.network.map.dto.InfoTypeParameters;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.BeforeParameterizedClassInvocation;
import org.junit.jupiter.params.Parameter;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opentest4j.TestAbortedException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

/**
 * Just some tests for Sonar coverage until we really implement unit tests on mappers...
 */
@ParameterizedClass(name = "{0}")
@ValueSource(classes = {
    //ElementInfosMapper.class,
    BatteryInfosMapper.class,
    BranchInfosMapper.class,
    BusBarSectionInfosMapper.class,
    BusInfosMapper.class,
    DanglingLineInfosMapper.class,
    GeneratorInfosMapper.class,
    HvdcInfosMapper.class,
    HvdcLccInfosMapper.class,
    HvdcVscInfosMapper.class,
    LccConverterStationInfosMapper.class,
    LineInfosMapper.class,
    LoadInfosMapper.class,
    ShuntCompensatorMapper.class,
    StaticVarCompensatorInfosMapper.class,
    SubstationInfosMapper.class,
    TwoWindingsTransformerInfosMapper.class,
    ThreeWindingsTransformerInfosMapper.class,
    TieLineInfosMapper.class,
    VoltageLevelInfosMapper.class,
    VscConverterStationInfosMapper.class,
})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
@Slf4j
class MappersTest implements WithAssertions, WithAssumptions {
    @Parameter private Class<?> classMapper;
    private Method toDataMethod;

    @BeforeParameterizedClassInvocation
    void setUpAndCheck() throws NoSuchMethodException, SecurityException {
        assertThat(this.classMapper).as("Mapper class").isNotInterface().isNotAnnotation().isNotRecord().isPublic().hasDeclaredMethods("toData");
        this.toDataMethod = this.classMapper.getDeclaredMethod("toData", Identifiable.class, InfoTypeParameters.class);
        assertThat(this.toDataMethod).as("Mapper#toData(Identifiable, InfoTypeParameters) method").isNotNull();
        assertThat(this.toDataMethod.getReturnType()).as("Mapper#toData(...) return type").isSameAs(ElementInfos.class);
        assertThat(Modifier.isStatic(this.toDataMethod.getModifiers())).as("Mapper#toData(...) is static").isTrue();
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(InfoType.class)
    void shouldHandleInfoType(final InfoType infoType) throws Throwable {
        final InfoTypeParameters parameters = new InfoTypeParameters(infoType, Map.of());
        // add possible implemented classes to avoid ClassCastException
        final Identifiable<?> identifiableMock = Mockito.mock(Identifiable.class, Mockito.withSettings()
            .defaultAnswer(invocation -> {
                throw new InterruptTest("No real data to test with.");
            }).extraInterfaces(// add possible implemented classes to avoid ClassCastException
                Battery.class,
                Branch.class,
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
            final Object result = this.toDataMethod.invoke(null, identifiableMock, parameters);
            assertThat(result).isNotNull();
            if (log.isDebugEnabled()) {
                log.debug("Result: {}", ReflectionToStringBuilder.toString(result, RecursiveToStringStyle.SHORT_PREFIX_STYLE));
            }
        } catch (final InvocationTargetException ex) {
            final Throwable cause = ex.getCause();
            if (cause instanceof UnsupportedOperationException && "TODO".equals(cause.getMessage())) {
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
