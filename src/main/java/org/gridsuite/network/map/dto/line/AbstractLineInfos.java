/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.line;

import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.extensions.BranchStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.gridsuite.network.map.dto.ElementInfos;
import org.gridsuite.network.map.model.CurrentLimitsData;
import static org.gridsuite.network.map.dto.utils.ElementUtils.toMapDataCurrentLimits;


/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
@SuperBuilder
@Getter
public abstract class AbstractLineInfos extends ElementInfos {

    @Getter
    @Setter
    protected static class LineInfos {
        CurrentLimitsData currentLimits1;
        CurrentLimitsData currentLimits2;
        BranchStatus<Line> branchStatus;
    }

    protected static AbstractLineInfos.LineInfos getLinesInfos(Line line) {
        AbstractLineInfos.LineInfos lineInfos = new AbstractLineInfos.LineInfos();
        line.getCurrentLimits1().ifPresent(limits1 -> {
            lineInfos.setCurrentLimits1(toMapDataCurrentLimits(limits1));
        });
        line.getCurrentLimits2().ifPresent(limits2 -> {
            lineInfos.setCurrentLimits1(toMapDataCurrentLimits(limits2));
        });
        BranchStatus<Line> branchStatus = line.getExtension(BranchStatus.class);
        if (branchStatus != null) {
            lineInfos.setBranchStatus(branchStatus);
        }

        return lineInfos;
    }

    public static ElementInfos toData(Identifiable<?> identifiable, InfoType dataType) {
        switch (dataType) {
            case TAB:
                return LineTabInfos.toData(identifiable);
            case FORM:
                return LineFormInfos.toData(identifiable);
            case MAP:
                return LineMapInfos.toData(identifiable);
            case LIST:
                return LineListInfos.toData(identifiable);
            case TOOLTIP:
                return LineTooltipInfos.toData(identifiable);
            default:
                throw new UnsupportedOperationException("TODO");
        }
    }
}
