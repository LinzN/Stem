package de.stem.stemSystem.modules.complianceModule;

import de.linzn.simplyConfiguration.FileConfiguration;
import de.linzn.simplyConfiguration.provider.YamlConfiguration;
import de.stem.stemSystem.STEMSystemApp;
import de.stem.stemSystem.modules.AbstractModule;
import de.stem.stemSystem.modules.pluginModule.STEMPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class ComplianceModule extends AbstractModule {

    private final STEMSystemApp stemSystemApp;
    public static File complianceDirectory = new File("compliance");
    private FileConfiguration fileConfiguration;
    private ArrayList<ComplianceCheck> complianceChecks;


    public ComplianceModule(STEMSystemApp stemSystemApp) {
        this.stemSystemApp = stemSystemApp;
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
                STEMSystemApp.LOGGER.ERROR(e);
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
        ComplianceCheck complianceCheck = new ComplianceCheck(complianceID, script, cronTime, runningCodes, errorCodes, stemPlugin, this.stemSystemApp);
        complianceCheck.register();
        STEMSystemApp.LOGGER.CONFIG("Register complianceCheck for " + complianceID);
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
