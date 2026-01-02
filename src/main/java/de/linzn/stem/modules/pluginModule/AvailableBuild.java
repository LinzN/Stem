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
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

public class AvailableBuild {
    private final STEMPlugin stemPlugin;
    private final PluginModule pluginModule;
    private final boolean isCustom;
    private boolean updateAvailable;
    private int updateAvailableBuildId;

    public AvailableBuild(STEMPlugin stemPlugin, PluginModule pluginModule) {
        this.stemPlugin = stemPlugin;
        this.pluginModule = pluginModule;
        this.isCustom = this.stemPlugin.getBuildJobName().equalsIgnoreCase("CUSTOM") || this.stemPlugin.getBuildNumber().equalsIgnoreCase("SNAPSHOT") || this.stemPlugin.getBuildNumber().equalsIgnoreCase("IDEA");
        this.updateAvailable = false;
        this.updateAvailableBuildId = -1;
    }

    public void check() {
        if (!this.isCustom) {
            int fileBuildId = Integer.parseInt(this.stemPlugin.getBuildNumber());
            int latestBuildId = this.getJenkinsNewestJobId(this.stemPlugin.getBuildJobName());

            String fileBuiltRevision = getJenkinsBuiltRevision(this.stemPlugin.getBuildJobName(), fileBuildId);
            String latestBuiltRevision = getJenkinsBuiltRevision(this.stemPlugin.getBuildJobName(), latestBuildId);

            if (!fileBuiltRevision.equalsIgnoreCase(latestBuiltRevision) && fileBuildId < latestBuildId) {
                this.updateAvailable = true;
                this.updateAvailableBuildId = latestBuildId;
            }
            {
                this.updateAvailable = false;
                this.updateAvailableBuildId = -1;
            }
        }
    }

    public boolean update() {
        return false;
    }

    public boolean isCustom() {
        return this.isCustom;
    }

    public boolean hasUpdateAvailable() {
        return this.updateAvailable;
    }

    public int getUpdateAvailableBuildId() {
        return this.updateAvailableBuildId;
    }

    public String getFileBuildId() {
        return this.stemPlugin.getBuildNumber();
    }

    private String getJenkinsBuiltRevision(String jobName, int jobId) {
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
            STEMApp.LOGGER.ERROR(e);
            return "error";
        }
    }

    private int getJenkinsNewestJobId(String jobName) {
        JSONObject jsonObject;
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
            STEMApp.LOGGER.ERROR(e);
            return 0;
        }
        if (jsonObject.has("id")) {
            return jsonObject.getInt("id");
        } else {
            return 0;
        }
    }
}
