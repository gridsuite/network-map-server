package org.gridsuite.network.map.dto.utils;

import com.powsybl.iidm.network.LoadingLimits;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.NotImplementedException;

import java.util.Set;

@Data
@AllArgsConstructor
public class TemporaryLimitDtoTest implements LoadingLimits.TemporaryLimit {
    private String name;
    private double value;
    private int acceptableDuration;

    @Override
    public boolean isFictitious() {
        throw new NotImplementedException("Not supported yet.");
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        return null;
    }

    @Override
    public String getProperty(String key) {
        return null;
    }

    @Override
    public boolean hasProperty() {
        return false;
    }

    @Override
    public boolean hasProperty(String key) {
        return false;
    }

    @Override
    public boolean removeProperty(String key) {
        return false;
    }

    @Override
    public String setProperty(String key, String value) {
        return null;
    }

    @Override
    public Set<String> getPropertyNames() {
        return null;
    }
}
