/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.utils;

import com.powsybl.iidm.network.CurrentLimits;
import com.powsybl.iidm.network.LimitType;
import com.powsybl.iidm.network.LoadingLimits;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.NotImplementedException;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@Data
@AllArgsConstructor
public class CurrentLimitsDtoTest implements CurrentLimits {
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
