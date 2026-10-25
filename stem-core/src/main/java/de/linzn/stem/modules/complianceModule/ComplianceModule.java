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

package de.linzn.stem.modules.complianceModule;

import de.linzn.simplyConfiguration.FileConfiguration;
import de.linzn.simplyConfiguration.provider.YamlConfiguration;
import de.linzn.stem.STEMApp;
import de.linzn.stem.modules.AbstractModule;
import de.linzn.stem.modules.pluginModule.STEMPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


public class ComplianceModule extends AbstractModule {

    public static File complianceDirectory = new File("compliance");
    private final CopyOnWriteArrayList<ComplianceCheck> complianceChecks;
    private FileConfiguration fileConfiguration;

    public ComplianceModule(STEMApp stemApp) {
        super(stemApp);
        this.complianceChecks = new CopyOnWriteArrayList<>();
        this.init();
        this.initConfig();
        this.readDefaultComplianceChecks();
    }

    private void init() {
        if (!complianceDirectory.exists() && !complianceDirectory.mkdirs()) {
            STEMApp.LOGGER.ERROR("Could not create compliance directory " + complianceDirectory.getAbsolutePath());
        }
    }

    private void initConfig() {
        this.fileConfiguration = YamlConfiguration.loadConfiguration(new File("module_compliance.yml"));
        this.fileConfiguration.save();
    }

    @Override
    public void onShutdown() {
        for (ComplianceCheck complianceCheck : this.complianceChecks) {
            complianceCheck.unregister();
        }
        this.complianceChecks.clear();
    }

    private void readDefaultComplianceChecks() {
        File[] files = complianceDirectory.listFiles();
        if (files == null) {
            return;
        }
        for (final File file : files) {
            if (file.isDirectory()) {
                continue;
            }
            try {
                this.registerComplianceCheck(file, this.getModulePlugin());
            } catch (Exception e) {
                STEMApp.LOGGER.ERROR("Invalid compliance check " + file.getName() + ": " + e.getMessage());
            }
        }
    }

    public void registerComplianceCheck(File file, STEMPlugin stemPlugin) {
        if (!file.getName().endsWith(".yml")) {
            throw new InvalidComplianceException("Invalid complianceCheckFile extension for " + file.getName() + "!");
        }
        FileConfiguration complianceConfig = YamlConfiguration.loadConfiguration(file);
        String complianceID = complianceConfig.getString("complianceID");
        String script = complianceConfig.getString("script");
        String cronTime = complianceConfig.getString("cronTime");
        if (complianceID == null || script == null || cronTime == null) {
            throw new InvalidComplianceException("Missing complianceID, script or cronTime in " + file.getName() + "!");
        }
        if (this.getComplianceCheck(complianceID) != null) {
            throw new InvalidComplianceException("Compliance check " + complianceID + " is already registered!");
        }
        List<Integer> runningCodes = complianceConfig.getIntegerList("runningCodes");
        List<Integer> errorCodes = complianceConfig.getIntegerList("errorCodes");

        ComplianceCheck complianceCheck = new ComplianceCheck(complianceID, script, cronTime, runningCodes, errorCodes, stemPlugin, this.getStemApp());
        complianceCheck.register();
        STEMApp.LOGGER.CONFIG("Register complianceCheck for " + complianceID);
        this.complianceChecks.add(complianceCheck);
    }

    public void unregisterComplianceCheck(String complianceID) {
        ComplianceCheck complianceCheck = getComplianceCheck(complianceID);
        if (complianceCheck != null) {
            complianceCheck.unregister();
            this.complianceChecks.remove(complianceCheck);
        }
    }

    /**
     * Unregister all compliance checks of a plugin
     *
     * @param stemPlugin plugin to remove the compliance checks for
     */
    public void unregisterComplianceChecks(STEMPlugin stemPlugin) {
        for (ComplianceCheck complianceCheck : this.complianceChecks) {
            if (complianceCheck.getStemPlugin() == stemPlugin) {
                complianceCheck.unregister();
                this.complianceChecks.remove(complianceCheck);
            }
        }
    }

    /**
     * @return snapshot of all registered compliance checks
     */
    public ArrayList<ComplianceCheck> getComplianceChecks() {
        return new ArrayList<>(complianceChecks);
    }

    public ComplianceCheck getComplianceCheck(String complianceID) {
        if (complianceID == null) {
            return null;
        }
        for (ComplianceCheck complianceCheck : this.complianceChecks) {
            if (complianceCheck.getComplianceID().equalsIgnoreCase(complianceID)) {
                return complianceCheck;
            }
        }
        return null;
    }
}
