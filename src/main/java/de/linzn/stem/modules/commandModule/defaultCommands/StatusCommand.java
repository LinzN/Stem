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

package de.linzn.stem.modules.commandModule.defaultCommands;

import de.linzn.stem.STEMApp;
import de.linzn.stem.modules.commandModule.ICommand;
import de.linzn.stem.modules.pluginModule.STEMPlugin;
import de.linzn.stem.taskManagment.AbstractCallback;
import de.linzn.stem.taskManagment.TaskMeta;
import de.linzn.stem.utils.JavaUtils;

import java.util.HashMap;
import java.util.HashSet;

public class StatusCommand implements ICommand {

    @Override
    public boolean executeTerminal(String[] args) {
        STEMApp.LOGGER.LIVE("Version: " + JavaUtils.getVersion());
        HashMap<AbstractCallback, STEMPlugin> listener = STEMApp.getInstance().getCallBackService().getCallbackListeners();
        STEMApp.LOGGER.LIVE("Active Callbacks: (" + listener.size() + ")");
        for (AbstractCallback abstractCallback : listener.keySet()) {
            STEMApp.LOGGER.LIVE("#Callback: " + abstractCallback.getClass().getSimpleName() + " from plugin: " + listener.get(abstractCallback).getPluginName() + " with taskId: " + abstractCallback.taskId);
        }

        HashSet<TaskMeta> tasks = STEMApp.getInstance().getScheduler().getTasks();
        STEMApp.LOGGER.LIVE("Active Scheduled Tasks: (" + tasks.size() + ")");
        for (TaskMeta task : tasks) {
            STEMApp.LOGGER.LIVE("#TaskId: " + task.getTaskId() + " from owner: " + task.getOwner().getPluginName());
        }
        return true;
    }

}
