/*
 * Copyright (c) 2026 MirraNET, Niklas Linz. All rights reserved.
 *
 * This file is part of the MirraNET project and is licensed under the
 * GNU Lesser General Public License v3.0 (LGPLv3).
 *
 * You may use, distribute and modify this code under the terms
 * of the LGPLv3 license. You should have received a copy of the
 * license along with this file. If not, see <https://www.gnu.org/licenses/lgpl-3.0.html>
 * or contact: niklas.linz@mirranet.de
 */

package de.linzn.stem.api.script;

import java.util.List;

/**
 * Result of a script run.
 *
 * @param exitCode exit code of the script, -1 if it could not be run
 * @param output   combined stdout and stderr lines
 */
public record ScriptResult(int exitCode, List<String> output) {

    public ScriptResult {
        output = List.copyOf(output);
    }

    /**
     * @return true if the script exited with code 0
     */
    public boolean isSuccess() {
        return exitCode == 0;
    }
}
