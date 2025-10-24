package org.gridsuite.network.map.dto.utils;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.LoadingLimits.TemporaryLimit;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.function.TriConsumer;
import org.apache.commons.lang3.tuple.Triple;
import org.mockito.Mockito;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public final class MockDto {
    private MockDto() {
        throw new InstantiationError("Utility Class cannot be instantiated.");
    }

    /** @see #buildOperationalLimitsGroup(TriConsumer, String, Double, List) */
    @Nonnull
    @SafeVarargs
    public static Triple<OperationalLimitsGroupDtoTest, CurrentLimitsDtoTest, List<TemporaryLimitDtoTest>> buildOperationalLimitsGroup(
            final boolean mockAndPeek,
            @Nullable final String id,
            @Nullable final Double permanentLimit,
            @Nullable final Triple<String, Double, Integer>... temporaryLimits) {
        final AtomicReference<List<TemporaryLimitDtoTest>> tlMock = new AtomicReference<>();
        final AtomicReference<CurrentLimitsDtoTest> clMock = new AtomicReference<>();
        final OperationalLimitsGroupDtoTest dto = buildOperationalLimitsGroup(!mockAndPeek ? null : (olg, cl, tls) -> {
            tlMock.set(tls);
            clMock.set(cl);
        }, id, permanentLimit, temporaryLimits);
        return Triple.of(dto, clMock.get(), tlMock.get());
    }

    /** @see #buildOperationalLimitsGroup(TriConsumer, String, Double, List) */
    @Nullable
    @SafeVarargs
    public static OperationalLimitsGroupDtoTest buildOperationalLimitsGroup(
            @Nullable final TriConsumer<OperationalLimitsGroupDtoTest, CurrentLimitsDtoTest, List<TemporaryLimitDtoTest>> mockAndPeek,
            @Nullable final String id,
            @Nullable final Double permanentLimit,
            @Nullable final Triple<String, Double, Integer>... temporaryLimits) {
        return buildOperationalLimitsGroup(mockAndPeek, id, permanentLimit, temporaryLimits == null ? null : List.of(temporaryLimits));
    }

    /**
     * Build the different cases of {@link OperationalLimitsGroup}:
     * <table border="1" style="border: 1px solid; border-collapse: collapse;">
     *   <tr><th>id</th>            <th>permanentLimit</th><th>temporaryLimits</th><th>Return</th></tr>
     *   <tr><td>null</td>          <td>*</td>             <td>*</td>              <td>null</td></tr>
     *   <tr><td>&lt;string&gt;</td><td>null</td>          <td>*</td>              <td>new OLG(id, null)</td></tr>
     *   <tr><td>&lt;string&gt;</td><td>&lt;double&gt;</td><td>null</td>           <td>new OLG(id, new CL(permanentLimits, null))</td></tr>
     *   <tr><td>&lt;string&gt;</td><td>&lt;double&gt;</td><td>[*]</td>            <td>new OLG(id, new CL(permanentLimits, temporaryLimits::map(new TL(...)))</td></tr>
     * </table>
     * @param mockAndPeek If is not {@code null}, the dto will be {@link Mockito#spy(Object) mocked} and the consumer will be called, else just return the dto.
     * @return The DTO, possibly mocked.
     * @see TemporaryLimitDtoTest
     * @see CurrentLimitsDtoTest
     * @see OperationalLimitsGroupDtoTest
     */
    @Nullable
    public static OperationalLimitsGroupDtoTest buildOperationalLimitsGroup(
            @Nullable final TriConsumer<OperationalLimitsGroupDtoTest, CurrentLimitsDtoTest, List<TemporaryLimitDtoTest>> mockAndPeek,
            @Nullable final String id,
            @Nullable final Double permanentLimit,
            @Nullable final List<Triple<String, Double, Integer>> temporaryLimits) {
        OperationalLimitsGroupDtoTest oDto = null;
        CurrentLimitsDtoTest cDto = null;
        List<TemporaryLimitDtoTest> tDtos = null;
        if (id != null) {
            if (permanentLimit != null) {
                tDtos = temporaryLimits == null ? null : temporaryLimits.stream()
                        .map(t -> new TemporaryLimitDtoTest(t.getLeft(), t.getMiddle(), t.getRight()))
                        .map(tl -> mockAndPeek == null ? tl : Mockito.spy(tl))
                        .toList();
                cDto = CurrentLimitsDtoTest.build(permanentLimit, tDtos);
                if (mockAndPeek != null) {
                    cDto = Mockito.spy(cDto);
                }
            }
            oDto = new OperationalLimitsGroupDtoTest(id, cDto);
            if (mockAndPeek != null) {
                oDto = Mockito.spy(oDto);
            }
        }
        if (mockAndPeek != null) {
            mockAndPeek.accept(oDto, cDto, tDtos);
        }
        return oDto;
    }

    @Data
    @AllArgsConstructor
    public static class TemporaryLimitDtoTest implements TemporaryLimit {
        private String name;
        private double value;
        private int acceptableDuration;

        @Override
        public boolean isFictitious() {
            throw new NotImplementedException("Not supported yet.");
        }
    }

    @Data
    @AllArgsConstructor
    public static class CurrentLimitsDtoTest implements CurrentLimits {
        @Accessors(chain = true)
        private double permanentLimit;
        private Collection<TemporaryLimit> temporaryLimits;

        public static CurrentLimitsDtoTest build(double permanentLimit, @Nullable Collection<? extends TemporaryLimit> temporaryLimits) {
            return new CurrentLimitsDtoTest(permanentLimit, temporaryLimits == null ? null : List.copyOf(temporaryLimits)); // generic collections are invariant
        }

        @Override
        public TemporaryLimit getTemporaryLimit(int acceptableDuration) {
            throw new NotImplementedException("Not supported yet.");
        }

        @Override
        public double getTemporaryLimitValue(int acceptableDuration) {
            throw new NotImplementedException("Not supported yet.");
        }

        @Override
        public LoadingLimits setTemporaryLimitValue(int acceptableDuration, double temporaryLimitValue) {
            throw new NotImplementedException("Not supported yet.");
        }

        @Override
        public void remove() {
            throw new NotImplementedException("Not supported yet.");
        }

        @Override
        public LimitType getLimitType() {
            throw new NotImplementedException("Not supported yet.");
        }
    }

    @Data
    @AllArgsConstructor
    public static class OperationalLimitsGroupDtoTest implements OperationalLimitsGroup {
        private String id;
        private Optional<CurrentLimits> currentLimits;

        public OperationalLimitsGroupDtoTest(String id, CurrentLimits currentLimits) {
            this(id, Optional.ofNullable(currentLimits));
        }

        @Override
        public Optional<ActivePowerLimits> getActivePowerLimits() {
            throw new NotImplementedException("Not supported yet.");
        }

        @Override
        public Optional<ApparentPowerLimits> getApparentPowerLimits() {
            throw new NotImplementedException("Not supported yet.");
        }

        @Override
        public CurrentLimitsAdder newCurrentLimits() {
            throw new NotImplementedException("Not supported yet.");
        }

        @Override
        public ActivePowerLimitsAdder newActivePowerLimits() {
            throw new NotImplementedException("Not supported yet.");
        }

        @Override
        public ApparentPowerLimitsAdder newApparentPowerLimits() {
            throw new NotImplementedException("Not supported yet.");
        }

        @Override
        public CurrentLimitsAdder newCurrentLimits(CurrentLimits currentLimits) {
            throw new NotImplementedException("Not supported yet.");
        }

        @Override
        public ActivePowerLimitsAdder newActivePowerLimits(ActivePowerLimits activePowerLimits) {
            throw new NotImplementedException("Not supported yet.");
        }

        @Override
        public ApparentPowerLimitsAdder newApparentPowerLimits(ApparentPowerLimits apparentPowerLimits) {
            throw new NotImplementedException("Not supported yet.");
        }

        @Override
        public void removeCurrentLimits() {
            throw new NotImplementedException("Not supported yet.");
        }

        @Override
        public void removeActivePowerLimits() {
            throw new NotImplementedException("Not supported yet.");
        }

        @Override
        public void removeApparentPowerLimits() {
            throw new NotImplementedException("Not supported yet.");
        }

        @Override
        public boolean isEmpty() {
            throw new NotImplementedException("Not supported yet.");
        }

        @Override
        public boolean hasProperty() {
            throw new NotImplementedException("Not supported yet.");
        }

        @Override
        public boolean hasProperty(String key) {
            throw new NotImplementedException("Not supported yet.");
        }

        @Override
        public String getProperty(String key) {
            throw new NotImplementedException("Not supported yet.");
        }

        @Override
        public String getProperty(String key, String defaultValue) {
            throw new NotImplementedException("Not supported yet.");
        }

        @Override
        public String setProperty(String key, String value) {
            throw new NotImplementedException("Not supported yet.");
        }

        @Override
        public boolean removeProperty(String key) {
            throw new NotImplementedException("Not supported yet.");
        }

        @Override
        public Set<String> getPropertyNames() {
            throw new NotImplementedException("Not supported yet.");
        }

        @Override
        public Network getNetwork() {
            throw new NotImplementedException("Not supported yet.");
        }
    }
}
