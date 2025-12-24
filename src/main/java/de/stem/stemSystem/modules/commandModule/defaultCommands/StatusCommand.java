/*
 * Copyright (c) 2025 MirraNET, Niklas Linz. All rights reserved.
 *
 * This file is part of the MirraNET project and is licensed under the
 * GNU Lesser General Public License v3.0 (LGPLv3).
 *
 * You may use, distribute and modify this code under the terms
 * of the LGPLv3 license. You should have received a copy of the
 * license along with this file. If not, see <https://www.gnu.org/licenses/lgpl-3.0.html>
 * or contact: niklas.linz@mirranet.de
 */

package de.stem.stemSystem.modules.commandModule.defaultCommands;

import de.stem.stemSystem.STEMSystemApp;
import de.stem.stemSystem.modules.commandModule.ICommand;
import de.stem.stemSystem.modules.pluginModule.STEMPlugin;
import de.stem.stemSystem.taskManagment.AbstractCallback;
import de.stem.stemSystem.taskManagment.TaskMeta;
import de.stem.stemSystem.utils.JavaUtils;

import java.util.HashMap;
import java.util.HashSet;

public class StatusCommand implements ICommand {

    @Override
    public boolean executeTerminal(String[] args) {
        STEMSystemApp.LOGGER.LIVE("Version: " + JavaUtils.getVersion());
        HashMap<AbstractCallback, STEMPlugin> listener = STEMSystemApp.getInstance().getCallBackService().getCallbackListeners();
        STEMSystemApp.LOGGER.LIVE("Active Callbacks: (" + listener.size() + ")");
        for (AbstractCallback abstractCallback : listener.keySet()) {
            STEMSystemApp.LOGGER.LIVE("#Callback: " + abstractCallback.getClass().getSimpleName() + " from plugin: " + listener.get(abstractCallback).getPluginName() + " with taskId: " + abstractCallback.taskId);
        }

        HashSet<TaskMeta> tasks = STEMSystemApp.getInstance().getScheduler().getTasks();
        STEMSystemApp.LOGGER.LIVE("Active Scheduled Tasks: (" + tasks.size() + ")");
        for (TaskMeta task : tasks) {
            STEMSystemApp.LOGGER.LIVE("#TaskId: " + task.getTaskId() + " from owner: " + task.getOwner().getPluginName());
        }
        return true;
    }

}
