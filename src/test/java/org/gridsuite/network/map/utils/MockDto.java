package org.gridsuite.network.map.utils;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.LoadingLimits.TemporaryLimit;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.NotImplementedException;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public final class MockDto {
    private MockDto() {
        throw new InstantiationError("Utility Class cannot be instantiated.");
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
