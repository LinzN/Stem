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


public class ComplianceModule extends AbstractModule {

    private final STEMApp stemApp;
    public static File complianceDirectory = new File("compliance");
    private FileConfiguration fileConfiguration;
    private ArrayList<ComplianceCheck> complianceChecks;


    public ComplianceModule(STEMApp stemApp) {
        this.stemApp = stemApp;
        this.init();
        this.initConfig();
        this.readDefaultComplianceChecks();
    }

    private void init() {
        if (!complianceDirectory.exists()) {
            complianceDirectory.mkdir();
        }
        this.complianceChecks = new ArrayList<>();
    }

    private void initConfig() {
        this.fileConfiguration = YamlConfiguration.loadConfiguration(new File("module_compliance.yml"));
        this.fileConfiguration.save();
    }

    @Override
    public void onShutdown() {
        for(ComplianceCheck complianceCheck : this.complianceChecks){
            complianceCheck.unregister();
        }
        this.complianceChecks.clear();
    }


    private void readDefaultComplianceChecks() {
        for (final File file : complianceDirectory.listFiles()) {
            try {
                this.registerComplianceCheck(file, this.getModulePlugin());
            } catch (InvalidComplianceException e){
                STEMApp.LOGGER.ERROR(e);
            }
        }
    }

    public void registerComplianceCheck(File file, STEMPlugin stemPlugin){
        if (!file.getName().endsWith(".yml")) {
            throw new InvalidComplianceException("Invalid complianceCheckFile extension for " + file.getName() + "!");
        }

        FileConfiguration complianceConfig = YamlConfiguration.loadConfiguration(file);
        String complianceID = complianceConfig.getString("complianceID");
        String script = complianceConfig.getString("script");
        boolean informUser = complianceConfig.getBoolean("informUser", false);
        String cronTime = complianceConfig.getString("cronTime");
        List<Integer> runningCodes = complianceConfig.getIntegerList("runningCodes");
        List<Integer> errorCodes = complianceConfig.getIntegerList("errorCodes");
        ComplianceCheck complianceCheck = new ComplianceCheck(complianceID, script, cronTime, runningCodes, errorCodes, stemPlugin, this.stemApp);
        complianceCheck.register();
        STEMApp.LOGGER.CONFIG("Register complianceCheck for " + complianceID);
        this.complianceChecks.add(complianceCheck);
    }

    public void unregisterComplianceCheck(String complianceID){
        ComplianceCheck complianceCheck = getComplianceCheck(complianceID);
        if(complianceCheck != null){
            complianceCheck.unregister();
        }
    }

    public ArrayList<ComplianceCheck> getComplianceChecks() {
        return complianceChecks;
    }

    public ComplianceCheck getComplianceCheck(String complianceID){
        for (ComplianceCheck complianceCheck : this.complianceChecks){
            if(complianceCheck.getComplianceID().equalsIgnoreCase(complianceID)){
                return complianceCheck;
            }
        }
        return null;
    }
}
