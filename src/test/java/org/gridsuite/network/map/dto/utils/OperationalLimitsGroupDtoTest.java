package org.gridsuite.network.map.dto.utils;

import com.powsybl.iidm.network.ActivePowerLimits;
import com.powsybl.iidm.network.ActivePowerLimitsAdder;
import com.powsybl.iidm.network.ApparentPowerLimits;
import com.powsybl.iidm.network.ApparentPowerLimitsAdder;
import com.powsybl.iidm.network.CurrentLimits;
import com.powsybl.iidm.network.CurrentLimitsAdder;
import com.powsybl.iidm.network.OperationalLimitsGroup;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.NotImplementedException;

import java.util.HashMap;
import java.util.Optional;
import java.util.Set;

@Data
@AllArgsConstructor
public class OperationalLimitsGroupDtoTest implements OperationalLimitsGroup {
    private String id;
    private Optional<CurrentLimits> currentLimits;
    private HashMap<String, String> properties;

    public OperationalLimitsGroupDtoTest(String id, CurrentLimits currentLimits) {
        this(id, Optional.ofNullable(currentLimits), new HashMap<>());
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
        return properties.get(key);
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        throw new NotImplementedException("Not supported yet.");
    }

    @Override
    public String setProperty(String key, String value) {
        return properties.put(key, value);
    }

    @Override
    public boolean removeProperty(String key) {
        return properties.remove(key) != null;
    }

    @Override
    public Set<String> getPropertyNames() {
        return properties.keySet();
    }
}
