package org.gridsuite.network.map.utils;

import com.powsybl.iidm.network.LoadingLimits.TemporaryLimit;
import com.powsybl.iidm.network.OperationalLimitsGroup;
import org.assertj.core.api.InstanceOfAssertFactories;
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
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Stream;

@ExtendWith({ SoftAssertionsExtension.class, MockitoExtension.class })
class ElementUtilsTest implements WithAssertions {
    /** Tests for {@link ElementUtils#operationalLimitsGroupToMapDataCurrentLimits(OperationalLimitsGroup)} */
    @Nested
    @DisplayName("fn operationalLimitsGroupToMapDataCurrentLimits(…)")
    class OperationalLimitsGroupToMapDataCurrentLimits {
        private Collection<Object> mocks = new ArrayList<>();

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

        @Test
        void nullInput() {
            assertThat(ElementUtils.operationalLimitsGroupToMapDataCurrentLimits(null)).isNull();
        }

        @Test
        void emptyCurrentLimits() {
            final var mock = Mockito.spy(new OperationalLimitsGroupDtoTest("id", Optional.empty()));
            mocks.add(mock);
            assertThat(ElementUtils.operationalLimitsGroupToMapDataCurrentLimits(mock)).isNull();
            Mockito.verify(mock).getCurrentLimits();
        }

        @Test
        void nanPermLimitAndNullTempLimit() {
            final var clMock = Mockito.spy(new CurrentLimitsDtoTest(Double.NaN, null));
            mocks.add(clMock);
            final var olgMock = Mockito.spy(new OperationalLimitsGroupDtoTest("id", clMock));
            mocks.add(olgMock);
            assertThat(ElementUtils.operationalLimitsGroupToMapDataCurrentLimits(olgMock)).isNull();
            Mockito.verify(olgMock, Mockito.times(2)).getCurrentLimits();
            Mockito.verify(olgMock).getId();
            Mockito.verify(clMock).getPermanentLimit();
            Mockito.verify(clMock).getTemporaryLimits();
        }

        @Test
        void nanPermLimitAndEmptyTempLimit() {
            final var clMock = Mockito.spy(new CurrentLimitsDtoTest(Double.NaN, List.of()));
            mocks.add(clMock);
            final var olgMock = Mockito.spy(new OperationalLimitsGroupDtoTest("id", clMock));
            mocks.add(olgMock);
            assertThat(ElementUtils.operationalLimitsGroupToMapDataCurrentLimits(olgMock)).isNull();
            Mockito.verify(olgMock, Mockito.times(2)).getCurrentLimits();
            Mockito.verify(olgMock).getId();
            Mockito.verify(clMock).getPermanentLimit();
            Mockito.verify(clMock).getTemporaryLimits();
        }

        @Test
        void nanPermLimitAndNonEmptyTempLimit() {
            final var tlMock = Mockito.spy(new TemporaryLimitDtoTest("testLimit", 456.789, 123));
            mocks.add(tlMock);
            final var clMock = Mockito.spy(new CurrentLimitsDtoTest(Double.NaN, List.of(tlMock)));
            mocks.add(clMock);
            final var olgMock = Mockito.spy(new OperationalLimitsGroupDtoTest("my id", clMock));
            mocks.add(olgMock);
            assertThat(ElementUtils.operationalLimitsGroupToMapDataCurrentLimits(olgMock))
                .isEqualTo(CurrentLimitsData.builder().id("my id").applicability(null).permanentLimit(null).temporaryLimits(
                    List.of(TemporaryLimitData.builder().acceptableDuration(123).name("testLimit").value(456.789).build())).build());
            Mockito.verify(olgMock, Mockito.times(2)).getCurrentLimits();
            Mockito.verify(olgMock).getId();
            Mockito.verify(clMock).getPermanentLimit();
            Mockito.verify(clMock, Mockito.times(2)).getTemporaryLimits();
            Mockito.verify(tlMock, Mockito.times(2)).getAcceptableDuration();
            Mockito.verify(tlMock).getName();
            Mockito.verify(tlMock, Mockito.times(2)).getValue();
        }

        @Test
        void nonNanPermLimitAndNullTempLimit() {
            final var clMock = Mockito.spy(new CurrentLimitsDtoTest(0.123, null));
            mocks.add(clMock);
            final var olgMock = Mockito.spy(new OperationalLimitsGroupDtoTest("my id", clMock));
            mocks.add(olgMock);
            assertThat(ElementUtils.operationalLimitsGroupToMapDataCurrentLimits(olgMock))
                .isEqualTo(CurrentLimitsData.builder().id("my id").applicability(null).permanentLimit(0.123).temporaryLimits(null).build());
            Mockito.verify(olgMock, Mockito.times(2)).getCurrentLimits();
            Mockito.verify(olgMock).getId();
            Mockito.verify(clMock, Mockito.times(2)).getPermanentLimit();
            Mockito.verify(clMock).getTemporaryLimits();
        }

        @Test
        void nonNanPermLimitAndEmptyTempLimit() {
            final var clMock = Mockito.spy(new CurrentLimitsDtoTest(0.123, List.of()));
            mocks.add(clMock);
            final var olgMock = Mockito.spy(new OperationalLimitsGroupDtoTest("my id", clMock));
            mocks.add(olgMock);
            assertThat(ElementUtils.operationalLimitsGroupToMapDataCurrentLimits(olgMock))
                .isEqualTo(CurrentLimitsData.builder().id("my id").applicability(null).permanentLimit(0.123).temporaryLimits(null).build());
            Mockito.verify(olgMock, Mockito.times(2)).getCurrentLimits();
            Mockito.verify(olgMock).getId();
            Mockito.verify(clMock, Mockito.times(2)).getPermanentLimit();
            Mockito.verify(clMock).getTemporaryLimits();
        }

        @Test
        void nonNanPermLimitAndNonEmptyTempLimit() {
            final var tlMock = Mockito.spy(new TemporaryLimitDtoTest("testLimit", 456.789, 123));
            mocks.add(tlMock);
            final var clMock = Mockito.spy(new CurrentLimitsDtoTest(0.0, List.of(tlMock)));
            mocks.add(clMock);
            final var olgMock = Mockito.spy(new OperationalLimitsGroupDtoTest("my id", clMock));
            mocks.add(olgMock);
            assertThat(ElementUtils.operationalLimitsGroupToMapDataCurrentLimits(olgMock))
                .isEqualTo(CurrentLimitsData.builder().id("my id").applicability(null).permanentLimit(0.0).temporaryLimits(
                    List.of(TemporaryLimitData.builder().acceptableDuration(123).name("testLimit").value(456.789).build())).build());
            Mockito.verify(olgMock, Mockito.times(2)).getCurrentLimits();
            Mockito.verify(olgMock).getId();
            Mockito.verify(clMock, Mockito.times(2)).getPermanentLimit();
            Mockito.verify(clMock, Mockito.times(2)).getTemporaryLimits();
            Mockito.verify(tlMock, Mockito.times(2)).getAcceptableDuration();
            Mockito.verify(tlMock).getName();
            Mockito.verify(tlMock, Mockito.times(2)).getValue();
        }
    }

    /** Tests for {@link ElementUtils#mergeCurrentLimits(Collection, Collection, Consumer)} */
    @Nested
    @DisplayName("fn mergeCurrentLimits(…, …, …)")
    class MergeCurrentLimitsTest {
        /**
         * This test check that after that the two input collections has been converted with {@link ElementUtils#operationalLimitsGroupToMapDataCurrentLimits(OperationalLimitsGroup)}
         * that {@link CurrentLimitsData#hasLimits()} work as intended.
         */
        @Order(1)
        @ParameterizedTest
        @MethodSource("testDtoHasLimitAfterMapData")
        void testDtoHasLimitAfterMapped(final boolean expected, final double pl, final Collection<TemporaryLimit> tl) {
            assertThat(ElementUtils.operationalLimitsGroupToMapDataCurrentLimits(new OperationalLimitsGroupDtoTest("id", new CurrentLimitsDtoTest(pl, tl)))).as("DTO")
                .extracting(CurrentLimitsData::hasLimits, InstanceOfAssertFactories.BOOLEAN).as("hasLimits")
                .isEqualTo(expected);
        }

        private static Stream<Arguments> testDtoHasLimitAfterMapData() {
            return Stream.of(
                Arguments.of(false, Double.NaN, null),
                Arguments.of(false, Double.NaN, List.of()),
                Arguments.of(true, Double.NaN, List.of(new TemporaryLimitDtoTest("tl", 1.2, 123))),
                Arguments.of(true, 0.123, null),
                Arguments.of(true, 0.123, List.of()),
                Arguments.of(true, 0.123, List.of(new TemporaryLimitDtoTest("tl", 1.2, 123)))
            );
        }

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

        // TODO what to do when one side has duplicate ID?
    }
}
