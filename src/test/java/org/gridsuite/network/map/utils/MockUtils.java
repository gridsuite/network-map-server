package org.gridsuite.network.map.utils;

import com.powsybl.iidm.network.CurrentLimits;
import com.powsybl.iidm.network.LoadingLimits.TemporaryLimit;
import com.powsybl.iidm.network.OperationalLimitsGroup;
import org.mockito.Mockito;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;

public final class MockUtils {
    private MockUtils() {
        throw new InstantiationError("Utility Class cannot be instantiated.");
    }

    public static TemporaryLimit mockTemporaryLimits(final int acceptableDuration, final String name, final double value) {
        TemporaryLimit mock = Mockito.mock(TemporaryLimit.class);
        Mockito.when(mock.getAcceptableDuration()).thenReturn(acceptableDuration);
        Mockito.when(mock.getName()).thenReturn(name);
        Mockito.when(mock.getValue()).thenReturn(value);
        return mock;
    }

    public static CurrentLimits mockCurrentLimits(double permanentLimit, List<TemporaryLimit> temporaryLimits) {
        CurrentLimits mock = Mockito.mock(CurrentLimits.class);
        //Mockito.when(mock.getLimitType()).thenCallRealMethod();
        Mockito.when(mock.getPermanentLimit()).thenReturn(permanentLimit);
        Mockito.when(mock.getTemporaryLimits()).thenReturn(temporaryLimits);
        return mock;
    }

    public static OperationalLimitsGroup mockOperationalLimitsGroup(final String id, double permanentLimit, List<TemporaryLimit> temporaryLimits) {
        return mockOperationalLimitsGroup(id, mockCurrentLimits(permanentLimit, temporaryLimits));
    }

    public static OperationalLimitsGroup mockOperationalLimitsGroup(final String id, final CurrentLimits cl) {
        OperationalLimitsGroup mock = Mockito.mock(OperationalLimitsGroup.class, Mockito.withSettings().strictness(Strictness.LENIENT));
        Mockito.when(mock.getId()).thenReturn(id);
        Mockito.when(mock.getCurrentLimits()).thenReturn(Optional.ofNullable(cl));
        return mock;
    }
}
