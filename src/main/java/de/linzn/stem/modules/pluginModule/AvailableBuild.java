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
import de.linzn.stem.taskManagment.SchedulerService;
import de.linzn.stem.utils.JavaUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class AvailableBuild {
    private final STEMPlugin stemPlugin;
    private final PluginModule pluginModule;
    private final boolean isCustom;
    private boolean updateAvailable;
    private int updateAvailableBuildId;
    private AtomicBoolean locked;
    private boolean isStemFramework;

    public AvailableBuild(STEMPlugin stemPlugin, PluginModule pluginModule) {
        this.stemPlugin = stemPlugin;
        this.pluginModule = pluginModule;
        this.isCustom = this.stemPlugin.getBuildJobName().equalsIgnoreCase("CUSTOM") || this.stemPlugin.getBuildNumber().equalsIgnoreCase("SNAPSHOT") || this.stemPlugin.getBuildNumber().equalsIgnoreCase("IDEA");
        this.isStemFramework = this.stemPlugin instanceof SchedulerService.DefaultSTEMPlugin;
        this.updateAvailable = false;
        this.updateAvailableBuildId = -1;
    }

    public void check() {
        if (!this.isCustom) {
            int fileBuildId = Integer.parseInt(this.stemPlugin.getBuildNumber());
            int latestBuildId = this.getJenkinsNewestJobId(this.stemPlugin.getBuildJobName());

            String fileBuiltRevision = getJenkinsBuiltRevision(this.stemPlugin.getBuildJobName(), fileBuildId);
            String latestBuiltRevision = getJenkinsBuiltRevision(this.stemPlugin.getBuildJobName(), latestBuildId);

            if (!fileBuiltRevision.equalsIgnoreCase(latestBuiltRevision) && latestBuildId > fileBuildId) {
                this.updateAvailable = true;
                this.updateAvailableBuildId = latestBuildId;
            } else {
                this.updateAvailable = false;
                this.updateAvailableBuildId = -1;
            }
        }
    }

    public boolean update() {

        if (this.locked == null || !this.locked.get()) {
            JSONObject artifactData = this.getArtifactData(this.stemPlugin.getBuildJobName(), this.updateAvailableBuildId);

            JSONArray artifacts = artifactData.getJSONArray("artifacts");
            String baseUrl = artifactData.getString("url");

            for (int i = 0; i < artifacts.length(); i++) {
                JSONObject artifact = artifacts.getJSONObject(i);
                String fileName = artifact.getString("fileName");
                String relPath = artifact.getString("relativePath");
                String downloadUrl = baseUrl + "artifact/" + relPath;

                try {
                    if (fileName.endsWith(".jar")) {
                        downloadArtifact(downloadUrl, this.stemPlugin.getFilePath());
                    } else if (fileName.equalsIgnoreCase("libraries.zip") || fileName.equalsIgnoreCase("coreDependencies.zip")) {
                        if (this.isStemFramework) {
                            Path zipFile = Paths.get(new File(this.stemPlugin.getTempFolder(), "core.zip").getAbsolutePath());
                            downloadArtifact(downloadUrl, zipFile);
                            if(zipFile.toFile().exists()){
                                Path rootPath = this.stemPlugin.getDataFolder().toPath();
                                File oldCores = new File(rootPath.toFile(), "core");
                                if(JavaUtils.deleteFolder(oldCores)) {
                                    try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipFile))) {
                                        ZipEntry entry;
                                        while ((entry = zis.getNextEntry()) != null) {
                                            if (entry.isDirectory()) continue;
                                            Path newFile = rootPath.resolve(entry.getName());
                                            Files.createDirectories(newFile.getParent()).toFile();
                                            STEMApp.LOGGER.INFO("Copy core library file " + newFile.toFile().getName() + " for STEM framework");
                                            Files.copy(zis, newFile, StandardCopyOption.REPLACE_EXISTING);
                                        }
                                    }
                                } else {
                                    STEMApp.LOGGER.ERROR("Something went wrong while removing old core!");
                                }
                            }
                        } else {
                            downloadArtifact(downloadUrl, Paths.get(new File(this.stemPlugin.getDataFolder(), fileName).getAbsolutePath()));
                        }
                    }
                    locked = new AtomicBoolean(true);
                } catch (IOException e) {
                    locked = new AtomicBoolean(false);
                    STEMApp.LOGGER.ERROR(e);
                }
            }
            return this.locked.get();
        }
        return true;
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

    public boolean locked() {
        if (this.locked != null) {
            return this.locked.get();
        }
        return false;
    }

    public STEMPlugin getStemPlugin() {
        return stemPlugin;
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

    private void downloadArtifact(String artifactUrl, Path destination) throws IOException {
        try (InputStream in = new URL(artifactUrl).openStream()) {
            Files.copy(in, destination, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private JSONObject getArtifactData(String jobName, int jobId) {
        JSONObject jsonObject = null;
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
        } catch (IOException e) {
            STEMApp.LOGGER.ERROR(e);
        }
        return jsonObject;
    }
}
