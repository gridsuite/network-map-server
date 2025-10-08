/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package org.gridsuite.network.map;

/**
 * @author Achour Berrahma <achour.berrahma at rte-france.com>
 */
public final class MapperTestUtils {
    private MapperTestUtils() {
        throw new InstantiationError("Utility Class cannot be instantiated.");
    }

    /**
     * Check if the exception message indicates an unsupported InfoType.
     * Matches patterns like: "InfoType 'X' is not supported for Y elements"
     *
     * @param message the exception message to check
     * @return true if the message indicates an unsupported InfoType
     */
    public static boolean isUnsupportedInfoTypeMessage(final String message) {
        return message != null &&
                message.contains("InfoType") &&
                message.contains("is not supported for") &&
                message.contains("elements");
    }
}
