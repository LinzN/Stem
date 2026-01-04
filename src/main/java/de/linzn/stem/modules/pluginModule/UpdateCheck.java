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

package de.linzn.stem.modules.pluginModule;

import de.linzn.stem.STEMApp;

import java.util.HashSet;
import java.util.Set;

public class UpdateCheck {
    private final PluginModule pluginModule;
    private final Set<AvailableBuild> availableUpdates;

    public UpdateCheck(PluginModule pluginModule) {
        this.pluginModule = pluginModule;
        this.availableUpdates = new HashSet<>();
    }

    public boolean checkForUpdates() {
        this.availableUpdates.clear();
        STEMPlugin stemDefaultPlugin = STEMApp.getInstance().getScheduler().getDefaultSystemPlugin();
        AvailableBuild availableFrameworkBuild = new AvailableBuild(stemDefaultPlugin, this.pluginModule);
        if (availableFrameworkBuild.isCustom()) {
            STEMApp.LOGGER.WARNING("STEM Framework is running a custom build! No update check available!");
        } else {
            availableFrameworkBuild.check();
            if (availableFrameworkBuild.hasUpdateAvailable()) {
                this.availableUpdates.add(availableFrameworkBuild);
                STEMApp.LOGGER.CONFIG("There is a new build #" + availableFrameworkBuild.getUpdateAvailableBuildId() + " available for STEM Framework");
            } else {
                STEMApp.LOGGER.INFO("The current build #" + availableFrameworkBuild.getFileBuildId() + " for STEM Framework is up to date.");
            }
        }

        for (STEMPlugin stemPlugin : this.pluginModule.getLoadedPlugins()) {
            STEMApp.LOGGER.INFO("Checking updates for plugin " + stemPlugin.getPluginName());

            AvailableBuild availableBuild = new AvailableBuild(stemPlugin, this.pluginModule);
            if (availableBuild.isCustom()) {
                STEMApp.LOGGER.WARNING("The plugin " + stemPlugin.getPluginName() + " is running a custom build. No update check available.");
            } else {
                availableBuild.check();
                if (availableBuild.hasUpdateAvailable()) {
                    this.availableUpdates.add(availableBuild);
                    STEMApp.LOGGER.CONFIG("There is a new build #" + availableBuild.getUpdateAvailableBuildId() + " available for plugin " + stemPlugin.getPluginName());
                } else {
                    STEMApp.LOGGER.INFO("The current build #" + availableBuild.getFileBuildId() + " for plugin " + stemPlugin.getPluginName() + " is up to date.");
                }
            }
        }
        return !this.availableUpdates.isEmpty();
    }

    public int upgradeAvailableBuilds() {
        int updatesDone = 0;
        for (AvailableBuild availableBuild : this.availableUpdates) {
            if (!availableBuild.locked()) {
                if (availableBuild.update()) {
                    STEMApp.LOGGER.CONFIG("Plugin #" + availableBuild.getStemPlugin().getPluginName() + " updated to newest version! Please reboot STEM Framework!");
                    updatesDone++;
                } else {
                    STEMApp.LOGGER.ERROR("Plugin #" + availableBuild.getStemPlugin().getPluginName() + " updated failed!");
                }
            } else {
                STEMApp.LOGGER.WARNING("Plugin #" + availableBuild.getStemPlugin().getPluginName() + " is locked. Pending reboot!");
            }
        }
        return updatesDone;
    }
}
