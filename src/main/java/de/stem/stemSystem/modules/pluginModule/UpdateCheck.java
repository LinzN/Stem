package de.stem.stemSystem.modules.pluginModule;

import de.stem.stemSystem.STEMSystemApp;
import de.stem.stemSystem.utils.JavaUtils;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

public class UpdateCheck {
    private final PluginModule pluginModule;

    public UpdateCheck(PluginModule pluginModule) {
        this.pluginModule = pluginModule;
    }

    public void checkForUpdates(){
        checkForFrameworkUpdate();
        checkForPluginUpdates();
    }

    public void checkForFrameworkUpdate(){
        String stemBuildId = JavaUtils.getBuildNumber();

        if (stemBuildId.equalsIgnoreCase("IDEA")) {
            STEMSystemApp.LOGGER.WARNING("STEM Framework is using a custom build! No update check available!");
        } else {
            int currentId = Integer.parseInt(stemBuildId);
            int newestId = this.getNewestJobId("STEM");

            String newestBuiltRevision = getBuiltRevision("STEM", newestId);
            String currentBuiltRevision = getBuiltRevision("STEM", currentId);

            if (!newestBuiltRevision.equalsIgnoreCase(currentBuiltRevision)) {
                STEMSystemApp.LOGGER.CONFIG("A new build is available for STEM Framework. Current #" + currentId + " SHA " + currentBuiltRevision + " newest #" + newestId + " SHA " + newestBuiltRevision);
            }
        }
    }

    public void checkForPluginUpdates() {

        for (STEMPlugin stemPlugin : this.pluginModule.getLoadedPlugins()) {
            STEMSystemApp.LOGGER.INFO("Checking updates for plugin " + stemPlugin.getPluginName());
            String buildID = stemPlugin.getBuildNumber();
            String jobName = stemPlugin.getBuildJobName();
            if (jobName.equalsIgnoreCase("CUSTOM") || buildID.equalsIgnoreCase("SNAPSHOT")) {
                STEMSystemApp.LOGGER.WARNING("The current build #" + buildID + " for plugin " + stemPlugin.getPluginName() + " is a custom build. No update check available.");
            } else {
                int newestId = this.getNewestJobId(jobName);
                int currentId = Integer.parseInt(buildID);

                String newestBuiltRevision = getBuiltRevision(jobName, newestId);
                String currentBuiltRevision = getBuiltRevision(jobName, currentId);

                if (!newestBuiltRevision.equalsIgnoreCase(currentBuiltRevision)) {
                    STEMSystemApp.LOGGER.CONFIG("The current build #" + currentId + " with SHA " + currentBuiltRevision + " for plugin " + stemPlugin.getPluginName() + " is outdated. There is a new build #" + newestId + " with SHA " + newestBuiltRevision + " available.");
                } else {
                    STEMSystemApp.LOGGER.INFO("The current build #" + currentId + " for plugin " + stemPlugin.getPluginName() + " is up to date.");
                }
            }
        }
    }

    private String getBuiltRevision(String jobName, int jobId) {
        JSONObject jsonObject;
        try {
            URL url = new URL(this.pluginModule.jenkinsURL + "/job/" + jobName + "/" + jobId + "/api/json?pretty=true");
            InputStream input = url.openStream();
            InputStreamReader isr = new InputStreamReader(input);
            BufferedReader reader = new BufferedReader(isr);
            StringBuilder json = new StringBuilder();
            int c;
            while ((c = reader.read()) != -1) {
                json.append((char) c);
            }
            jsonObject = new JSONObject(json.toString());
            JSONObject revisionTab = jsonObject.getJSONArray("actions").getJSONObject(2);

            if (jsonObject.getJSONArray("actions").getJSONObject(2).isEmpty()) {
                revisionTab = jsonObject.getJSONArray("actions").getJSONObject(1);
            }
            return revisionTab.getJSONObject("lastBuiltRevision").getString("SHA1");
        } catch (Exception e) {
            STEMSystemApp.LOGGER.ERROR(e);
            return "error";
        }
    }

    private int getNewestJobId(String jobName) {
        JSONObject jsonObject = null;
        try {
            URL url = new URL(this.pluginModule.jenkinsURL + "/job/" + jobName + "/lastSuccessfulBuild/api/json?pretty=true");
            InputStream input = url.openStream();
            InputStreamReader isr = new InputStreamReader(input);
            BufferedReader reader = new BufferedReader(isr);
            StringBuilder json = new StringBuilder();
            int c;
            while ((c = reader.read()) != -1) {
                json.append((char) c);
            }
            jsonObject = new JSONObject(json.toString());
        } catch (IOException e) {
            STEMSystemApp.LOGGER.ERROR(e);
            return 0;
        }
        if (jsonObject.has("id")) {
            return jsonObject.getInt("id");
        } else {
            return 0;
        }
    }
}
