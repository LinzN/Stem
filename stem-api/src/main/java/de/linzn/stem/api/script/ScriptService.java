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

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Runs STEM scripts (*.stemsh files in the scripts directory).
 */
public interface ScriptService {

    /**
     * Run a script and wait for it to finish.
     *
     * @param scriptName script name without extension; may contain a sub directory ("network/restart")
     * @param parameters script parameters (passed as "-name value")
     * @param timeout    maximum run time; the script is killed afterwards
     * @return result of the run
     * @throws de.linzn.stem.api.script.ScriptFailedException if the script can not be found, is invalid or times out
     */
    ScriptResult run(String scriptName, Map<String, String> parameters, Duration timeout) throws ScriptFailedException;

    /**
     * Run a script asynchronously.
     */
    CompletableFuture<ScriptResult> runAsync(String scriptName, Map<String, String> parameters, Duration timeout);
}
