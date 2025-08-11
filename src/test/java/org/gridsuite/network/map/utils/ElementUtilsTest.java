package org.gridsuite.network.map.utils;

import com.powsybl.iidm.network.OperationalLimitsGroup;
import jakarta.annotation.Nullable;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.WithAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.gridsuite.network.map.dto.common.CurrentLimitsData;
import org.gridsuite.network.map.dto.common.CurrentLimitsData.Applicability;
import org.gridsuite.network.map.dto.common.TemporaryLimitData;
import org.gridsuite.network.map.dto.utils.ElementUtils;
import org.gridsuite.network.map.utils.MockDto.CurrentLimitsDtoTest;
import org.gridsuite.network.map.utils.MockDto.OperationalLimitsGroupDtoTest;
import org.gridsuite.network.map.utils.MockDto.TemporaryLimitDtoTest;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Stream;

@ExtendWith({ SoftAssertionsExtension.class, MockitoExtension.class })
class ElementUtilsTest implements WithAssertions {
    /** Tests for {@link ElementUtils#operationalLimitsGroupToMapDataCurrentLimits(OperationalLimitsGroup)} */
    @Nested
    @DisplayName("fn operationalLimitsGroupToMapDataCurrentLimits(…)")
    class OperationalLimitsGroupToMapDataCurrentLimits {
        private final List<Object> mocks = new ArrayList<>();

        @AfterEach
        void setDown() {
            if (!mocks.isEmpty()) {
                Mockito.verifyNoMoreInteractions(mocks.toArray(Object[]::new));
                mocks.clear();
            }
        }

        /* All cases possibles:
         * null -> null
         * operationalLimitsGroup(currentLimits=empty, *=any) -> null
         * operationalLimitsGroup(currentLimits={PermanentLimit=NaN, TemporaryLimits=null, *=any}, *=any) -> null
         * operationalLimitsGroup(currentLimits={PermanentLimit=NaN, TemporaryLimits=[], *=any}, *=any) -> null
         * operationalLimitsGroup(currentLimits={permanentLimit=NaN, temporaryLimits=[any], *=any}, *=any) -> (id=*, applicability=null, permanentLimit=null, temporaryLimits=[any])
         * operationalLimitsGroup(currentLimits={PermanentLimit=_non_NaN_, TemporaryLimits=null, *=any}, *=any) -> (id=*, applicability=null, permanentLimit=*, temporaryLimits=null)
         * operationalLimitsGroup(currentLimits={PermanentLimit=_non_NaN_, TemporaryLimits=[], *=any}, *=any) -> (id=*, applicability=null, permanentLimit=*, temporaryLimits=null)
         * operationalLimitsGroup(currentLimits={PermanentLimit=_non_NaN_, TemporaryLimits=[any], *=any}, *=any) -> (id=*, applicability=null, permanentLimit=*, temporaryLimits=[any])
         */

        @SafeVarargs
        private CurrentLimitsData testResult(final String id, @Nullable final Double permanentLimit, @Nullable final Triple<String, Double, Integer>... temporaryLimits) {
            final var dtoMocks = MockDto.buildOperationalLimitsGroup(true, id, permanentLimit, temporaryLimits);
            final TemporaryLimitDtoTest tlMock = CollectionUtils.isEmpty(dtoMocks.getRight()) ? null : dtoMocks.getRight().getFirst();
            if (tlMock != null) {
                mocks.add(tlMock);
            }
            final CurrentLimitsDtoTest clMock = dtoMocks.getMiddle();
            if (clMock != null) {
                mocks.add(clMock);
            }
            final OperationalLimitsGroupDtoTest olgMock = dtoMocks.getLeft();
            if (olgMock != null) {
                mocks.add(olgMock);
            }
            final CurrentLimitsData result = ElementUtils.operationalLimitsGroupToMapDataCurrentLimits(olgMock);
            if (id != null) {
                Mockito.verify(olgMock, Mockito.times(permanentLimit == null ? 1 : 2)).getCurrentLimits();
                if (permanentLimit != null) {
                    Mockito.verify(olgMock).getId();
                    Mockito.verify(clMock, Mockito.times(Double.isNaN(permanentLimit) ? 1 : 2)).getPermanentLimit();
                    Mockito.verify(clMock, Mockito.times(ArrayUtils.isEmpty(temporaryLimits) ? 1 : 2)).getTemporaryLimits();
                    if (ArrayUtils.isNotEmpty(temporaryLimits)) {
                        Mockito.verify(tlMock).getName();
                        Mockito.verify(tlMock, Mockito.times(2)).getValue();
                        Mockito.verify(tlMock, Mockito.times(2)).getAcceptableDuration();
                    }
                }
            }
            return result;
        }

        @Test
        @Order(1)
        void nullInput() {
            // null -> null
            assertThat(testResult(null, null, (Triple<String, Double, Integer>[]) null)).isNull();
        }

        @Test
        @Order(2)
        void emptyCurrentLimits() {
            // OLG("id", null) -> null
            assertThat(testResult("id", null /*,new Triple[0]*/)).isNull();
        }

        @Test
        @Order(3)
        void nanPermLimitAndNullTempLimit() {
            // OLG("id", CL(NaN, null)) -> null
            assertThat(testResult("id", Double.NaN, (Triple<String, Double, Integer>[]) null)).isNull();
        }

        @Test
        @Order(4)
        void nanPermLimitAndEmptyTempLimit() {
            // OLG("id", CL(NaN, [])) -> null
            assertThat(testResult("id", Double.NaN /*,new Triple[0]*/)).isNull();
        }

        @Test
        @Order(5)
        void nanPermLimitAndNonEmptyTempLimit() {
            // OLG("my id", CL(Nan, [TL("testLimit", 456.789, 123)])) -> CLD("my id", null, [TLD("testLimit", 456.789, 123)], null)
            assertThat(testResult("my id", Double.NaN, Triple.of("testLimit", 456.789, 123)))
                .isEqualTo(CurrentLimitsData.builder().id("my id").applicability(null).permanentLimit(null).temporaryLimits(
                    List.of(TemporaryLimitData.builder().acceptableDuration(123).name("testLimit").value(456.789).build())).build());
        }

        @Test
        @Order(6)
        void nonNanPermLimitAndNullTempLimit() {
            // OLG("my id", CL(0.123, null)) -> CLD("my id", 0.123, null, null)
            assertThat(testResult("my id", 0.123, (Triple<String, Double, Integer>[]) null))
                .isEqualTo(CurrentLimitsData.builder().id("my id").applicability(null).permanentLimit(0.123).temporaryLimits(null).build());
        }

        @Test
        @Order(7)
        void nonNanPermLimitAndEmptyTempLimit() {
            // OLG("my id", CL(0.123, [])) -> CLD("my id", 0.123, [], null)
            assertThat(testResult("my id", 0.123 /*,new Triple[0]*/))
                .isEqualTo(CurrentLimitsData.builder().id("my id").applicability(null).permanentLimit(0.123).temporaryLimits(null).build());
        }

        @Test
        @Order(8)
        void nonNanPermLimitAndNonEmptyTempLimit() {
            // OLG("my id", CL(0.0, [TL("testLimit", 456.789, 123)])) -> CLD("my id", 0.0, [TLD("testLimit", 456.789, 123)], null)
            assertThat(testResult("my id", 0.0, Triple.of("testLimit", 456.789, 123)))
                .isEqualTo(CurrentLimitsData.builder().id("my id").applicability(null).permanentLimit(0.0).temporaryLimits(
                    List.of(TemporaryLimitData.builder().acceptableDuration(123).name("testLimit").value(456.789).build())).build());
        }
    }

    /** Tests for {@link ElementUtils#mergeCurrentLimits(Collection, Collection, Consumer)} */
    @Nested
    @DisplayName("fn mergeCurrentLimits(…, …, …)")
    class MergeCurrentLimitsTest {
        @ParameterizedTest(name = ParameterizedTest.INDEX_PLACEHOLDER)
        @MethodSource("mergeCurrentLimitsTestData")
        void shouldNotThrow(final Collection<OperationalLimitsGroup> olg1, final Collection<OperationalLimitsGroup> olg2, final List<CurrentLimitsData> expected) {
            AtomicReference<List<CurrentLimitsData>> results = new AtomicReference<>();
            ElementUtils.mergeCurrentLimits(olg1, olg2, results::set);
            assertThat(results.get()).as("Result").isEqualTo(expected);
        }

        private static Stream<Arguments> mergeCurrentLimitsTestData() {
            return Stream.of(
                // 1
                Arguments.of(List.of(
                    new OperationalLimitsGroupDtoTest("group1", new CurrentLimitsDtoTest(220.0, List.of(new TemporaryLimitDtoTest("temporary1", 50.0, 100), new TemporaryLimitDtoTest("temporary2", 70.0, 150))))
                ), List.of(
                    new OperationalLimitsGroupDtoTest("group1", new CurrentLimitsDtoTest(220.0, List.of(new TemporaryLimitDtoTest("temporary1", 50.0, 100), new TemporaryLimitDtoTest("temporary2", 70.0, 150))))
                ), List.of(
                    CurrentLimitsData.builder().id("group1").permanentLimit(220.0).temporaryLimits(List.of(
                        TemporaryLimitData.builder().acceptableDuration(100).name("temporary1").value(50.0).build(),
                        TemporaryLimitData.builder().acceptableDuration(150).name("temporary2").value(70.0).build()
                    )).applicability(Applicability.EQUIPMENT).build()
                )),
                // 2
                Arguments.of(List.of(), List.of(
                    new OperationalLimitsGroupDtoTest("group1", new CurrentLimitsDtoTest(220.0, List.of(new TemporaryLimitDtoTest("temporary1", 50.0, 100), new TemporaryLimitDtoTest("temporary2", 70.0, 150))))
                ), List.of(
                    CurrentLimitsData.builder().id("group1").permanentLimit(220.0).temporaryLimits(List.of(
                        TemporaryLimitData.builder().acceptableDuration(100).name("temporary1").value(50.0).build(),
                        TemporaryLimitData.builder().acceptableDuration(150).name("temporary2").value(70.0).build()
                    )).applicability(Applicability.SIDE2).build()
                )),
                // 3
                Arguments.of(List.of(
                    new OperationalLimitsGroupDtoTest("group1", new CurrentLimitsDtoTest(220.0, List.of(new TemporaryLimitDtoTest("temporary1", 50.0, 100), new TemporaryLimitDtoTest("temporary2", 70.0, 150))))
                ), List.of(), List.of(
                    CurrentLimitsData.builder().id("group1").permanentLimit(220.0).temporaryLimits(List.of(
                        TemporaryLimitData.builder().acceptableDuration(100).name("temporary1").value(50.0).build(),
                        TemporaryLimitData.builder().acceptableDuration(150).name("temporary2").value(70.0).build()
                    )).applicability(Applicability.SIDE1).build()
                )),
                // 4
                Arguments.of(List.of(
                    new OperationalLimitsGroupDtoTest("group1", new CurrentLimitsDtoTest(220.0, List.of()))
                ), List.of(
                    new OperationalLimitsGroupDtoTest("group1", new CurrentLimitsDtoTest(220.0, List.of(new TemporaryLimitDtoTest("temporary1", 50.0, 100), new TemporaryLimitDtoTest("temporary2", 70.0, 150))))
                ), List.of(
                    CurrentLimitsData.builder().id("group1").permanentLimit(220.0).temporaryLimits(List.of(
                        TemporaryLimitData.builder().acceptableDuration(100).name("temporary1").value(50.0).build(),
                        TemporaryLimitData.builder().acceptableDuration(150).name("temporary2").value(70.0).build()
                    )).applicability(Applicability.SIDE2).build()
                )),
                // 5
                Arguments.of(List.of(
                    new OperationalLimitsGroupDtoTest("group1", new CurrentLimitsDtoTest(Double.NaN, List.of()))
                ), List.of(
                    new OperationalLimitsGroupDtoTest("group1", new CurrentLimitsDtoTest(220.0, List.of(new TemporaryLimitDtoTest("temporary1", 50.0, 100), new TemporaryLimitDtoTest("temporary2", 70.0, 150))))
                ), List.of(
                    CurrentLimitsData.builder().id("group1").permanentLimit(220.0).temporaryLimits(List.of(
                        TemporaryLimitData.builder().acceptableDuration(100).name("temporary1").value(50.0).build(),
                        TemporaryLimitData.builder().acceptableDuration(150).name("temporary2").value(70.0).build()
                    )).applicability(Applicability.SIDE2).build()
                )),
                // 6
                Arguments.of(List.of(
                    new OperationalLimitsGroupDtoTest("group1", new CurrentLimitsDtoTest(Double.NaN, List.of(new TemporaryLimitDtoTest("temporary1", 50.0, 100), new TemporaryLimitDtoTest("temporary2", 70.0, 150))))
                ), List.of(
                    new OperationalLimitsGroupDtoTest("group1", new CurrentLimitsDtoTest(220.0, List.of(new TemporaryLimitDtoTest("temporary1", 50.0, 100), new TemporaryLimitDtoTest("temporary2", 70.0, 150))))
                ), List.of(
                    CurrentLimitsData.builder().id("group1").permanentLimit(220.0).temporaryLimits(List.of(
                        TemporaryLimitData.builder().acceptableDuration(100).name("temporary1").value(50.0).build(),
                        TemporaryLimitData.builder().acceptableDuration(150).name("temporary2").value(70.0).build()
                    )).applicability(Applicability.SIDE2).build()
                )),
                // 7
                Arguments.of(List.of(
                    new OperationalLimitsGroupDtoTest("group1", new CurrentLimitsDtoTest(220.0, List.of(new TemporaryLimitDtoTest("temporary1", 50.0, 100), new TemporaryLimitDtoTest("temporary2", 70.0, 150))))
                ), List.of(
                    new OperationalLimitsGroupDtoTest("group1", new CurrentLimitsDtoTest(220.0, List.of()))
                ), List.of(
                    CurrentLimitsData.builder().id("group1").permanentLimit(220.0).temporaryLimits(List.of(
                        TemporaryLimitData.builder().acceptableDuration(100).name("temporary1").value(50.0).build(),
                        TemporaryLimitData.builder().acceptableDuration(150).name("temporary2").value(70.0).build()
                    )).applicability(Applicability.SIDE1).build()
                )),
                // 8
                Arguments.of(List.of(
                    new OperationalLimitsGroupDtoTest("group1", new CurrentLimitsDtoTest(220.0, List.of(new TemporaryLimitDtoTest("temporary1", 50.0, 100), new TemporaryLimitDtoTest("temporary2", 70.0, 150))))
                ), List.of(
                    new OperationalLimitsGroupDtoTest("group1", new CurrentLimitsDtoTest(Double.NaN, List.of()))
                ), List.of(
                    CurrentLimitsData.builder().id("group1").permanentLimit(220.0).temporaryLimits(List.of(
                        TemporaryLimitData.builder().acceptableDuration(100).name("temporary1").value(50.0).build(),
                        TemporaryLimitData.builder().acceptableDuration(150).name("temporary2").value(70.0).build()
                    )).applicability(Applicability.SIDE1).build()
                )),
                // 9
                Arguments.of(List.of(
                    new OperationalLimitsGroupDtoTest("group1", new CurrentLimitsDtoTest(220.0, List.of(new TemporaryLimitDtoTest("temporary1", 50.0, 100), new TemporaryLimitDtoTest("temporary2", 70.0, 150))))
                ), List.of(
                    new OperationalLimitsGroupDtoTest("group1", new CurrentLimitsDtoTest(Double.NaN, List.of(new TemporaryLimitDtoTest("temporary1", 50.0, 100), new TemporaryLimitDtoTest("temporary2", 70.0, 150))))
                ), List.of(
                    CurrentLimitsData.builder().id("group1").permanentLimit(220.0).temporaryLimits(List.of(
                        TemporaryLimitData.builder().acceptableDuration(100).name("temporary1").value(50.0).build(),
                        TemporaryLimitData.builder().acceptableDuration(150).name("temporary2").value(70.0).build()
                    )).applicability(Applicability.SIDE1).build()
                ))
            );
        }

        @Test
        void shouldThrowOnNanLimit(final SoftAssertions softly) {
            final Collection<OperationalLimitsGroup> l1 = List.of(new OperationalLimitsGroupDtoTest("group1", new CurrentLimitsDtoTest(Double.NaN, List.of(new TemporaryLimitDtoTest("temporary1", 50.0, 100), new TemporaryLimitDtoTest("temporary2", 70.0, 150)))));
            final Collection<OperationalLimitsGroup> l2 = List.of(new OperationalLimitsGroupDtoTest("group1", new CurrentLimitsDtoTest(220.0, List.of(new TemporaryLimitDtoTest("temporary1", 50.0, 100), new TemporaryLimitDtoTest("temporary2", 70.0, 150)))));
            AtomicReference<List<CurrentLimitsData>> results = new AtomicReference<>();
            softly.assertThatNullPointerException().isThrownBy(() -> ElementUtils.mergeCurrentLimits(l1, l2, results::set));
            softly.assertThatNullPointerException().isThrownBy(() -> ElementUtils.mergeCurrentLimits(l2, l1, results::set));
        }
    }
}
