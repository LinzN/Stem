/*
 * Copyright (C) 2020. Niklas Linz - All Rights Reserved
 * You may use, distribute and modify this code under the
 * terms of the LGPLv3 license, which unfortunately won't be
 * written for another century.
 *
 * You should have received a copy of the LGPLv3 license with
 * this file. If not, please write to: niklas.linz@enigmar.de
 *
 */

package de.azcore.azcoreRuntime.modules.commandModule.defaultCommands;

import de.azcore.azcoreRuntime.AZCoreRuntimeApp;
import de.azcore.azcoreRuntime.AppLogger;
import de.azcore.azcoreRuntime.modules.commandModule.ICommand;
import de.azcore.azcoreRuntime.modules.pluginModule.AZPlugin;
import de.azcore.azcoreRuntime.taskManagment.AZTask;
import de.azcore.azcoreRuntime.taskManagment.AbstractCallback;
import de.azcore.azcoreRuntime.utils.JavaUtils;

import java.util.HashMap;
import java.util.HashSet;

public class StatusCommand implements ICommand {

    @Override
    public boolean executeTerminal(String[] args) {
        AppLogger.logger("Version: " + JavaUtils.getVersion(), false);
        HashMap<AbstractCallback, AZPlugin> listener = AZCoreRuntimeApp.getInstance().getCallBackService().getCallbackListeners();
        AppLogger.logger("Active Callbacks: (" + listener.size() + ")", false);
        for (AbstractCallback abstractCallback : listener.keySet()) {
            AppLogger.logger("#Callback: " + abstractCallback.getClass().getSimpleName() + " from plugin: " + listener.get(abstractCallback).getPluginName() + " with taskId: " + abstractCallback.taskId, false);
        }

        HashSet<AZTask> tasks = AZCoreRuntimeApp.getInstance().getScheduler().getTasks();
        AppLogger.logger("Active Scheduled Tasks: (" + listener.size() + ")", false);
        for (AZTask task : tasks) {
            AppLogger.logger("#TaskId: " + task.getTaskId() + " from owner: " + task.getOwner().getPluginName(), false);
        }
        return true;
    }

}
