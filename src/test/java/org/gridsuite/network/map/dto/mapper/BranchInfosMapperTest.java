package org.gridsuite.network.map.dto.mapper;

import com.powsybl.iidm.network.OperationalLimitsGroup;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.assertj.core.api.WithAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.gridsuite.network.map.dto.common.CurrentLimitsData;
import org.gridsuite.network.map.dto.common.CurrentLimitsData.Applicability;
import org.gridsuite.network.map.dto.common.TemporaryLimitData;
import org.gridsuite.network.map.dto.utils.MockDto;
import org.gridsuite.network.map.dto.utils.MockDto.CurrentLimitsDtoTest;
import org.gridsuite.network.map.dto.utils.MockDto.OperationalLimitsGroupDtoTest;
import org.gridsuite.network.map.dto.utils.MockDto.TemporaryLimitDtoTest;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

@ExtendWith({ SoftAssertionsExtension.class, MockitoExtension.class })
class BranchInfosMapperTest implements WithAssertions {
    /** Tests for {@link BranchInfosMapper#operationalLimitsGroupToMapDataCurrentLimits(OperationalLimitsGroup, Applicability)} */
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
            final CurrentLimitsData result = BranchInfosMapper.operationalLimitsGroupToMapDataCurrentLimits(olgMock, null);
            if (id != null) {
                Mockito.verify(olgMock, Mockito.times(1)).getCurrentLimits();
                if (permanentLimit != null) {
                    Mockito.verify(olgMock).getId();
                    Mockito.verify(olgMock, Mockito.times(1)).getPropertyNames();
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

    /** Tests for {@link BranchInfosMapper#mergeCurrentLimits(Collection, Collection)} */
    @Nested
    @DisplayName("fn mergeCurrentLimits(…, …, …)")
    class MergeCurrentLimitsTest {
        private static OperationalLimitsGroupDtoTest buildParam(final boolean withPL, final boolean withTL) {
            if (!withTL) {
                return MockDto.buildOperationalLimitsGroup(null, "group1", withPL ? 220.0 : Double.NaN);
            }
            return MockDto.buildOperationalLimitsGroup(null, "group1", withPL ? 220.0 : Double.NaN, Triple.of("temporary1", 50.0, 100), Triple.of("temporary2", 70.0, 150));
        }

        private static CurrentLimitsData buildResult(final boolean withPL, final boolean withTL, @Nullable final Boolean isSide1or2) {
            return CurrentLimitsData.builder().id("group1").permanentLimit(withPL ? 220.0 : null).temporaryLimits(!withTL ? null : List.of(
                TemporaryLimitData.builder().acceptableDuration(100).name("temporary1").value(50.0).build(),
                TemporaryLimitData.builder().acceptableDuration(150).name("temporary2").value(70.0).build()
            )).applicability(isSide1or2 == null ? Applicability.EQUIPMENT : (isSide1or2 ? Applicability.SIDE1 : Applicability.SIDE2))
            .build();
        }

        @ParameterizedTest(name = ParameterizedTest.INDEX_PLACEHOLDER)
        @MethodSource({"mergeCurrentLimitsTestData", "mergeCurrentLimitsTestDataSwappable", "mergeCurrentLimitsTestDataNotMergeable"}) // test al the combinations possible for the inputs
        void shouldNotThrow(final Collection<OperationalLimitsGroup> olg1, final Collection<OperationalLimitsGroup> olg2, final List<CurrentLimitsData> expected) {
            System.out.print("L1: ");
            System.out.println(olg1);
            System.out.print("L2: ");
            System.out.println(olg2);
            System.out.print("Expect: ");
            System.out.println(expected);
            assertThat(BranchInfosMapper.mergeCurrentLimits(olg1, olg2)).as("Result").isEqualTo(expected);
        }

        private static Stream<Arguments> mergeCurrentLimitsTestData() {
            // particular cases (p1&p2 the same if swapped)
            return Stream.of(
                // 1 - same limits on both sides for an id
                Arguments.of(List.of(buildParam(true, true)), List.of(buildParam(true, true)), List.of(buildResult(true, true, null))),
                // 2 - with no limits on either side for an id
                Arguments.of(List.of(buildParam(false, false)), List.of(buildParam(false, false)), null),
                // 3
                Arguments.of(List.of(buildParam(true, false)), List.of(buildParam(true, false)), List.of(buildResult(true, false, null))),
                // 4
                Arguments.of(List.of(buildParam(false, true)), List.of(buildParam(false, true)), List.of(buildResult(false, true, null)))
            );
        }

        private static Stream<Arguments> mergeCurrentLimitsTestDataSwappable() {
            // cases with param1 and param2 swappable
            // will map [l;r]->[(l,r)=s1, (r,l)=s2], so the pair is implicitly for case with side1 as result
            return Stream.<Triple<OperationalLimitsGroupDtoTest, OperationalLimitsGroupDtoTest, Function<Boolean, CurrentLimitsData>>>of(
                    // 5 & 6 & 7 & 8 - id present only on one side, because OLG() with no limit is converted to null
                    Triple.of(buildParam(true, true), null, s2 -> buildResult(true, true, !s2)),
                    Triple.of(buildParam(true, true), buildParam(false, false), s2 -> buildResult(true, true, !s2)),
                    // 9 & 10 & 11 & 12 - id present only on one side, because OLG() with no limit is converted to null
                    Triple.of(buildParam(true, false), null, s2 -> buildResult(true, false, !s2)),
                    Triple.of(buildParam(true, false), buildParam(false, false), s2 -> buildResult(true, false, !s2)),
                    // 9 & 10 - id present only on one side, because OLG() with no limit is converted to null
                    Triple.of(buildParam(false, true), null, s2 -> buildResult(false, true, !s2)),
                    Triple.of(buildParam(false, true), buildParam(false, false), s2 -> buildResult(false, true, !s2)),
                    // 11 & 12 - after dto conversion, the same as test n°2
                    Triple.of(buildParam(false, false), null, s2 -> null)
            ).mapMulti((args, argsTests) -> {
                final var arg1 = args.getLeft() == null ? List.of() : List.of(args.getLeft());
                final var arg2 = args.getMiddle() == null ? List.of() : List.of(args.getMiddle());
                argsTests.accept(Arguments.of(arg1, arg2, Optional.ofNullable(args.getRight().apply(false)).map(List::of).orElse(null)));
                argsTests.accept(Arguments.of(arg2, arg1, Optional.ofNullable(args.getRight().apply(true)).map(List::of).orElse(null)));
            });
        }

        private static Stream<Arguments> mergeCurrentLimitsTestDataNotMergeable() {
            return Stream.of(
                // 13 & 14
                Arguments.of(List.of(buildParam(true, true)), List.of(buildParam(true, false)),
                            List.of(buildResult(true, true, true), buildResult(true, false, false))),
                // 15 & 16
                Arguments.of(List.of(buildParam(true, true)), List.of(buildParam(false, true)),
                            List.of(buildResult(true, true, true), buildResult(false, true, false))),
                // 17 & 18
                Arguments.of(List.of(buildParam(true, false)), List.of(buildParam(false, true)),
                            List.of(buildResult(true, false, true), buildResult(false, true, false)))
            );
        }
    }
}
