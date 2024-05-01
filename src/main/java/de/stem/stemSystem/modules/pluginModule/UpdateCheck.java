package de.stem.stemSystem.modules.pluginModule;

import de.stem.stemSystem.STEMSystemApp;
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

    public void checkForUpdates() {
        for (STEMPlugin stemPlugin : this.pluginModule.getLoadedPlugins()) {
            String buildID = stemPlugin.getBuildNumber();
            String jobName = stemPlugin.getBuildJobName();
            if (jobName.equalsIgnoreCase("CUSTOM") || buildID.equalsIgnoreCase("SNAPSHOT")) {
                STEMSystemApp.LOGGER.WARNING("The current build #" + buildID + " for plugin " + stemPlugin.getPluginName() + " is a custom build. No update check available.");
            } else {
                int newestId = this.getNewestJobId(jobName);
                int jobId = Integer.parseInt(buildID);
                if (jobId < newestId) {
                    STEMSystemApp.LOGGER.CONFIG("The current build #" + jobId + " for plugin " + stemPlugin.getPluginName() + " is outdated. There is a new build #" + newestId + " available");
                } else {
                    STEMSystemApp.LOGGER.INFO("The current build #" + jobId + " for plugin " + stemPlugin.getPluginName() + " is up to date.");
                }
            }
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
