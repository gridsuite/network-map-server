/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package org.gridsuite.network.map.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.extensions.ExtensionAdderProvider;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.network.store.iidm.impl.VoltageLevelImpl;
import com.powsybl.network.store.iidm.impl.extensions.AbstractIidmExtensionAdder;
import com.powsybl.network.store.model.ExtensionAttributes;
import com.powsybl.network.store.model.ExtensionLoader;
import com.rte_france.powsybl.iidm.network.extensions.api.VoltageLevelInvalidity;
import com.rte_france.powsybl.iidm.network.extensions.api.VoltageLevelInvalidityAdder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Kamil MARUT {@literal <kamil.marut at rte-france.com>}
 */
public final class DummyVoltageLevelInvalidity {

    private DummyVoltageLevelInvalidity() {
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DummyVoltageLevelInvalidityAttributes implements ExtensionAttributes {
        private VoltageLevelInvalidity.InvalidityType invalidityType;
    }

    public static class DummyVoltageLevelInvalidityImpl extends AbstractExtension<VoltageLevel> implements VoltageLevelInvalidity {

        public DummyVoltageLevelInvalidityImpl(VoltageLevel voltageLevel) {
            super(voltageLevel);
        }

        @Override
        public InvalidityType getInvalidityType() {
            DummyVoltageLevelInvalidityAttributes attributes = (DummyVoltageLevelInvalidityAttributes) ((VoltageLevelImpl) getExtendable())
                    .getResource()
                    .getAttributes()
                    .getExtensionAttributes()
                    .get(VoltageLevelInvalidity.NAME);
            return attributes.getInvalidityType();
        }
    }

    public static class DummyVoltageLevelInvalidityAdderImpl extends AbstractIidmExtensionAdder<VoltageLevel, VoltageLevelInvalidity> implements VoltageLevelInvalidityAdder {

        private VoltageLevelInvalidity.InvalidityType invalidityType;

        protected DummyVoltageLevelInvalidityAdderImpl(VoltageLevel extendable) {
            super(extendable);
        }

        @Override
        protected VoltageLevelInvalidity createExtension(VoltageLevel voltageLevel) {
            DummyVoltageLevelInvalidityAttributes attributes = new DummyVoltageLevelInvalidityAttributes(invalidityType);
            ((VoltageLevelImpl) voltageLevel).updateResourceWithoutNotification(res ->
                    res.getAttributes().getExtensionAttributes().put(VoltageLevelInvalidity.NAME, attributes));
            return new DummyVoltageLevelInvalidityImpl(voltageLevel);
        }

        @Override
        public VoltageLevelInvalidityAdder withInvalidityType(VoltageLevelInvalidity.InvalidityType invalidityType) {
            this.invalidityType = invalidityType;
            return this;
        }
    }

    @AutoService(ExtensionAdderProvider.class)
    public static class DummyVoltageLevelInvalidityAdderImplProvider
            implements ExtensionAdderProvider<VoltageLevel, VoltageLevelInvalidity, DummyVoltageLevelInvalidityAdderImpl> {

        @Override
        public String getImplementationName() {
            return "NetworkStore";
        }

        @Override
        public String getExtensionName() {
            return VoltageLevelInvalidity.NAME;
        }

        @Override
        public Class<? super DummyVoltageLevelInvalidityAdderImpl> getAdderClass() {
            return DummyVoltageLevelInvalidityAdderImpl.class;
        }

        @Override
        public DummyVoltageLevelInvalidityAdderImpl newAdder(VoltageLevel voltageLevel) {
            return new DummyVoltageLevelInvalidityAdderImpl(voltageLevel);
        }
    }

    @AutoService(ExtensionLoader.class)
    public static class DummyVoltageLevelInvalidityLoader
            implements ExtensionLoader<VoltageLevel, VoltageLevelInvalidity, DummyVoltageLevelInvalidityAttributes> {

        @Override
        public Extension<VoltageLevel> load(VoltageLevel voltageLevel) {
            return new DummyVoltageLevelInvalidityImpl(voltageLevel);
        }

        @Override
        public String getName() {
            return VoltageLevelInvalidity.NAME;
        }

        @Override
        public Class<VoltageLevelInvalidity> getType() {
            return VoltageLevelInvalidity.class;
        }

        @Override
        public Class<DummyVoltageLevelInvalidityAttributes> getAttributesType() {
            return DummyVoltageLevelInvalidityAttributes.class;
        }
    }
}
