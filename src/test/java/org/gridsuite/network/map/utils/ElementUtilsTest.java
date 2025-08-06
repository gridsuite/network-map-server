package org.gridsuite.network.map.utils;

import com.powsybl.iidm.network.OperationalLimitsGroup;
import org.assertj.core.api.WithAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.gridsuite.network.map.dto.common.CurrentLimitsData;
import org.gridsuite.network.map.dto.common.TemporaryLimitData;
import org.gridsuite.network.map.dto.utils.ElementUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
            final var mock = MockUtils.mockOperationalLimitsGroup("id", null);
            mocks.add(mock);
            assertThat(ElementUtils.operationalLimitsGroupToMapDataCurrentLimits(mock)).isNull();
            Mockito.verify(mock).getCurrentLimits();
        }

        @Test
        void nanPermLimitAndNullTempLimit() {
            final var clMock = MockUtils.mockCurrentLimits(Double.NaN, null);
            mocks.add(clMock);
            final var olgMock = MockUtils.mockOperationalLimitsGroup("id", clMock);
            mocks.add(olgMock);
            assertThat(ElementUtils.operationalLimitsGroupToMapDataCurrentLimits(olgMock)).isNull();
            Mockito.verify(olgMock, Mockito.times(2)).getCurrentLimits();
            Mockito.verify(olgMock).getId();
            Mockito.verify(clMock).getPermanentLimit();
            Mockito.verify(clMock).getTemporaryLimits();
        }

        @Test
        void nanPermLimitAndEmptyTempLimit() {
            final var clMock = MockUtils.mockCurrentLimits(Double.NaN, List.of());
            mocks.add(clMock);
            final var olgMock = MockUtils.mockOperationalLimitsGroup("id", clMock);
            mocks.add(olgMock);
            assertThat(ElementUtils.operationalLimitsGroupToMapDataCurrentLimits(olgMock)).isNull();
            Mockito.verify(olgMock, Mockito.times(2)).getCurrentLimits();
            Mockito.verify(olgMock).getId();
            Mockito.verify(clMock).getPermanentLimit();
            Mockito.verify(clMock).getTemporaryLimits();
        }

        @Test
        void nanPermLimitAndNonEmptyTempLimit() {
            final var tlMock = MockUtils.mockTemporaryLimits(123, "testLimit", 456.789);
            mocks.add(tlMock);
            final var clMock = MockUtils.mockCurrentLimits(Double.NaN, List.of(tlMock));
            mocks.add(clMock);
            final var olgMock = MockUtils.mockOperationalLimitsGroup("my id", clMock);
            mocks.add(olgMock);
            assertThat(ElementUtils.operationalLimitsGroupToMapDataCurrentLimits(olgMock)).isEqualTo(CurrentLimitsData.builder()
                    .id("my id").applicability(null).permanentLimit(null).temporaryLimits(List.of(TemporaryLimitData.builder()
                            .acceptableDuration(123).name("testLimit").value(456.789).build())).build());
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
            final var clMock = MockUtils.mockCurrentLimits(0.123, null);
            mocks.add(clMock);
            final var olgMock = MockUtils.mockOperationalLimitsGroup("my id", clMock);
            mocks.add(olgMock);
            assertThat(ElementUtils.operationalLimitsGroupToMapDataCurrentLimits(olgMock)).isEqualTo(CurrentLimitsData.builder()
                    .id("my id").applicability(null).permanentLimit(0.123).temporaryLimits(null).build());
            Mockito.verify(olgMock, Mockito.times(2)).getCurrentLimits();
            Mockito.verify(olgMock).getId();
            Mockito.verify(clMock, Mockito.times(2)).getPermanentLimit();
            Mockito.verify(clMock).getTemporaryLimits();
        }

        @Test
        void nonNanPermLimitAndEmptyTempLimit() {
            final var clMock = MockUtils.mockCurrentLimits(0.123, List.of());
            mocks.add(clMock);
            final var olgMock = MockUtils.mockOperationalLimitsGroup("my id", clMock);
            mocks.add(olgMock);
            assertThat(ElementUtils.operationalLimitsGroupToMapDataCurrentLimits(olgMock)).isEqualTo(CurrentLimitsData.builder()
                    .id("my id").applicability(null).permanentLimit(0.123).temporaryLimits(null).build());
            Mockito.verify(olgMock, Mockito.times(2)).getCurrentLimits();
            Mockito.verify(olgMock).getId();
            Mockito.verify(clMock, Mockito.times(2)).getPermanentLimit();
            Mockito.verify(clMock).getTemporaryLimits();
        }

        @Test
        void nonNanPermLimitAndNonEmptyTempLimit() {
            final var tlMock = MockUtils.mockTemporaryLimits(123, "testLimit", 456.789);
            mocks.add(tlMock);
            final var clMock = MockUtils.mockCurrentLimits(0.0, List.of(tlMock));
            mocks.add(clMock);
            final var olgMock = MockUtils.mockOperationalLimitsGroup("my id", clMock);
            mocks.add(olgMock);
            assertThat(ElementUtils.operationalLimitsGroupToMapDataCurrentLimits(olgMock)).isEqualTo(CurrentLimitsData.builder()
                    .id("my id").applicability(null).permanentLimit(0.0).temporaryLimits(List.of(TemporaryLimitData.builder()
                            .acceptableDuration(123).name("testLimit").value(456.789).build())).build());
            Mockito.verify(olgMock, Mockito.times(2)).getCurrentLimits();
            Mockito.verify(olgMock).getId();
            Mockito.verify(clMock, Mockito.times(2)).getPermanentLimit();
            Mockito.verify(clMock, Mockito.times(2)).getTemporaryLimits();
            Mockito.verify(tlMock, Mockito.times(2)).getAcceptableDuration();
            Mockito.verify(tlMock).getName();
            Mockito.verify(tlMock, Mockito.times(2)).getValue();
        }
    }
}
