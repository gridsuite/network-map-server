/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.network.map.dto;

public interface View {
    interface ListView extends View {
    }

    interface MapView extends View {
    }

    interface TabView extends View {
    }

    interface FormView extends View {
    }

    static Class<? extends View> infoTypeToView(ElementInfos.InfoType infoType) {
        switch (infoType) {
            case LIST:
                return ListView.class;
            case FORM:
                return FormView.class;
            case MAP:
                return MapView.class;
            case TAB:
                return TabView.class;
            default:
                throw new UnsupportedOperationException("TODO");
        }
    }
}
