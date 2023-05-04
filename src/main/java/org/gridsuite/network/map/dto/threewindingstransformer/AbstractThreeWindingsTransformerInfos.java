/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.dto.threewindingstransformer;

import com.powsybl.iidm.network.CurrentLimits;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.gridsuite.network.map.dto.ElementInfos;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
@SuperBuilder
@Getter
public abstract class AbstractThreeWindingsTransformerInfos extends ElementInfos {
    public static ElementInfos toData(Identifiable<?> identifiable, InfoType dataType) {
        switch (dataType) {
            case LIST:
                return ThreeWindingsTransformerListInfos.toData(identifiable);
            case TAB:
                return ThreeWindingsTransformerTabInfos.toData(identifiable);
            default:
                throw new UnsupportedOperationException("TODO");
        }
    }

    protected static void mapThreeWindingsTransformerPermanentLimits(
            ThreeWindingsTransformerTabInfos.ThreeWindingsTransformerTabInfosBuilder builder,
            ThreeWindingsTransformer transformer) {
        CurrentLimits limits1 = transformer.getLeg1().getCurrentLimits().orElse(null);
        CurrentLimits limits2 = transformer.getLeg2().getCurrentLimits().orElse(null);
        CurrentLimits limits3 = transformer.getLeg3().getCurrentLimits().orElse(null);
        if (limits1 != null && !Double.isNaN(limits1.getPermanentLimit())) {
            builder.permanentLimit1(limits1.getPermanentLimit());
        }
        if (limits2 != null && !Double.isNaN(limits2.getPermanentLimit())) {
            builder.permanentLimit2(limits2.getPermanentLimit());
        }
        if (limits3 != null && !Double.isNaN(limits3.getPermanentLimit())) {
            builder.permanentLimit3(limits3.getPermanentLimit());
        }
    }

    protected static void mapThreeWindingsTransformerRatioTapChangers(
            ThreeWindingsTransformerTabInfos.ThreeWindingsTransformerTabInfosBuilder builder,
            ThreeWindingsTransformer transformer) {
        ThreeWindingsTransformer.Leg leg1 = transformer.getLeg1();
        ThreeWindingsTransformer.Leg leg2 = transformer.getLeg2();
        ThreeWindingsTransformer.Leg leg3 = transformer.getLeg3();
        if (leg1.hasRatioTapChanger()) {
            builder.ratioTapChanger1(toMapData(leg1.getRatioTapChanger()))
                    .loadTapChanging1Capabilities(leg1.getRatioTapChanger().hasLoadTapChangingCapabilities())
                    .regulatingRatio1(leg1.getRatioTapChanger().isRegulating());
            if (!Double.isNaN(leg1.getRatioTapChanger().getTargetV())) {
                builder.targetV1(leg1.getRatioTapChanger().getTargetV());
            }
        }
        if (leg2.hasRatioTapChanger()) {
            builder.ratioTapChanger2(toMapData(leg2.getRatioTapChanger()))
                    .loadTapChanging2Capabilities(leg2.getRatioTapChanger().hasLoadTapChangingCapabilities())
                    .regulatingRatio2(leg2.getRatioTapChanger().isRegulating());
            if (!Double.isNaN(leg2.getRatioTapChanger().getTargetV())) {
                builder.targetV2(leg2.getRatioTapChanger().getTargetV());
            }
        }
        if (leg3.hasRatioTapChanger()) {
            builder.ratioTapChanger3(toMapData(leg3.getRatioTapChanger()))
                    .loadTapChanging3Capabilities(leg3.getRatioTapChanger().hasLoadTapChangingCapabilities())
                    .regulatingRatio3(leg3.getRatioTapChanger().isRegulating());
            if (!Double.isNaN(leg3.getRatioTapChanger().getTargetV())) {
                builder.targetV3(leg3.getRatioTapChanger().getTargetV());
            }
        }
        if (leg1.hasPhaseTapChanger()) {
            builder.phaseTapChanger1(toMapData(leg1.getPhaseTapChanger()))
                    .regulatingMode1(leg1.getPhaseTapChanger().getRegulationMode().name())
                    .regulatingPhase1(leg1.getPhaseTapChanger().isRegulating());
            if (!Double.isNaN(leg1.getPhaseTapChanger().getRegulationValue())) {
                builder.regulatingValue1(leg1.getPhaseTapChanger().getRegulationValue());
            }
        }
        if (leg2.hasPhaseTapChanger()) {
            builder.phaseTapChanger2(toMapData(leg2.getPhaseTapChanger()))
                    .regulatingMode2(leg2.getPhaseTapChanger().getRegulationMode().name())
                    .regulatingPhase2(leg2.getPhaseTapChanger().isRegulating());
            if (!Double.isNaN(leg2.getPhaseTapChanger().getRegulationValue())) {
                builder.regulatingValue2(leg2.getPhaseTapChanger().getRegulationValue());
            }
        }
        if (leg3.hasPhaseTapChanger()) {
            builder.phaseTapChanger3(toMapData(leg3.getPhaseTapChanger()))
                    .regulatingMode3(leg3.getPhaseTapChanger().getRegulationMode().name())
                    .regulatingPhase3(leg3.getPhaseTapChanger().isRegulating());
            if (!Double.isNaN(leg3.getPhaseTapChanger().getRegulationValue())) {
                builder.regulatingValue3(leg3.getPhaseTapChanger().getRegulationValue());
            }
        }
    }
}
