/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.voltagelevel;

import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.SwitchKind;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.extensions.BusbarSectionPosition;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.gridsuite.network.map.dto.ElementInfos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
@SuperBuilder
@Getter
public class VoltageLevelInfos extends ElementInfos {

    public static VoltageLevelInfos toData(Identifiable identifiable, InfoType dataType) {
        switch (dataType) {
            case TAB:
                return VoltageLevelTabInfos.toData(identifiable);
            case LIST:
                return VoltageLevelListInfos.toData(identifiable);
            default:
                throw new UnsupportedOperationException("TODO");
        }
    }

    protected static void mapVoltageLevelSwitchKindsAndSectionCount(VoltageLevelTabInfos.VoltageLevelTabInfosBuilder builder, VoltageLevel voltageLevel) {
        AtomicInteger busbarCount = new AtomicInteger(1);
        AtomicInteger sectionCount = new AtomicInteger(1);
        AtomicBoolean warning = new AtomicBoolean(false);
        voltageLevel.getNodeBreakerView().getBusbarSections().forEach(bbs -> {
            var pos = bbs.getExtension(BusbarSectionPosition.class);
            if (pos != null) {
                if (pos.getBusbarIndex() > busbarCount.get()) {
                    busbarCount.set(pos.getBusbarIndex());
                }
                if (pos.getSectionIndex() > sectionCount.get()) {
                    sectionCount.set(pos.getSectionIndex());
                }
            } else {
                warning.set(true);
            }
        });
        builder.isPartiallyCopied(warning.get());
        if (!warning.get()) {
            builder.busbarCount(busbarCount.get());
            builder.sectionCount(sectionCount.get());
            builder.switchKinds(new ArrayList<>(Collections.nCopies(sectionCount.get() - 1, SwitchKind.DISCONNECTOR)));
        } else {
            builder.busbarCount(1);
            builder.sectionCount(1);
            builder.switchKinds(Collections.emptyList());
        }
    }
}
