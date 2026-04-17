package org.gridsuite.network.map.dto.utils;

import com.powsybl.iidm.network.*;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.apache.commons.lang3.function.TriConsumer;
import org.apache.commons.lang3.tuple.Triple;
import org.mockito.Mockito;

import java.util.List;
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
}

