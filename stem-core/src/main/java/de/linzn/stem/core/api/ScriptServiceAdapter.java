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

package de.linzn.stem.core.api;

import de.linzn.stem.STEMApp;
import de.linzn.stem.api.script.ScriptFailedException;
import de.linzn.stem.api.script.ScriptResult;
import de.linzn.stem.api.script.ScriptService;
import de.linzn.stem.modules.pluginModule.STEMPlugin;
import de.linzn.stem.modules.scriptModule.ScriptManager;
import de.linzn.stem.modules.scriptModule.StemScript;
import de.linzn.stem.modules.scriptModule.exceptions.ScriptException;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

final class ScriptServiceAdapter implements ScriptService {
    private final STEMApp stemApp;
    private final STEMPlugin owner;

    ScriptServiceAdapter(STEMApp stemApp, STEMPlugin owner) {
        this.stemApp = stemApp;
        this.owner = owner;
    }

    @Override
    public ScriptResult run(String scriptName, Map<String, String> parameters, Duration timeout) throws ScriptFailedException {
        Objects.requireNonNull(scriptName, "scriptName");
        long timeoutMillis = timeout == null ? TimeUnit.MINUTES.toMillis(60) : Math.max(1, timeout.toMillis());
        ScriptManager scriptManager = this.stemApp.getScriptManager();
        try {
            StemScript script;
            int slash = scriptName.lastIndexOf('/');
            if (slash > 0) {
                script = scriptManager.getStemScript(scriptName.substring(0, slash), scriptName.substring(slash + 1));
            } else {
                script = scriptManager.getStemScript(scriptName);
            }
            if (parameters != null) {
                parameters.forEach(script::addScriptParameter);
            }
            script.start();
            script.await((int) Math.min(Integer.MAX_VALUE, timeoutMillis), TimeUnit.MILLISECONDS);
            return new ScriptResult(script.exitCode(), script.getOutput_lines());
        } catch (ScriptException e) {
            throw new ScriptFailedException("Script " + scriptName + " failed: " + e.getMessage(), e);
        }
    }

    @Override
    public CompletableFuture<ScriptResult> runAsync(String scriptName, Map<String, String> parameters, Duration timeout) {
        CompletableFuture<ScriptResult> future = new CompletableFuture<>();
        this.stemApp.getScheduler().runTask(owner, () -> {
            try {
                future.complete(this.run(scriptName, parameters, timeout));
            } catch (Throwable throwable) {
                future.completeExceptionally(throwable);
            }
        });
        return future;
    }
}
