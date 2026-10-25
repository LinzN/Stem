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

import de.linzn.openJL.system.FileSystem;
import de.linzn.stem.STEMApp;
import de.linzn.stem.taskManagment.SchedulerService;
import de.linzn.stem.utils.SafeFiles;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Checks the MirraNET Jenkins server for a newer build of the framework or a plugin and downloads it.
 * All requests use timeouts, every stream is closed, and downloads are written atomically so an
 * interrupted download never leaves a broken jar behind.
 */
public class AvailableBuild {
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);
    private static final Duration DOWNLOAD_TIMEOUT = Duration.ofMinutes(10);
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(CONNECT_TIMEOUT)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private final STEMPlugin stemPlugin;
    private final PluginModule pluginModule;
    private final boolean isCustom;
    private final AtomicBoolean locked = new AtomicBoolean(false);
    private final boolean isStemFramework;
    private volatile boolean updateAvailable;
    private volatile int updateAvailableBuildId;

    public AvailableBuild(STEMPlugin stemPlugin, PluginModule pluginModule) {
        this.stemPlugin = stemPlugin;
        this.pluginModule = pluginModule;
        this.isCustom = isCustomBuild(stemPlugin);
        this.isStemFramework = this.stemPlugin instanceof SchedulerService.DefaultSTEMPlugin;
        this.updateAvailable = false;
        this.updateAvailableBuildId = -1;
    }

    private static boolean isCustomBuild(STEMPlugin plugin) {
        String jobName = plugin.getBuildJobName();
        String buildNumber = plugin.getBuildNumber();
        if (jobName == null || buildNumber == null) {
            return true;
        }
        if (jobName.equalsIgnoreCase("CUSTOM") || buildNumber.equalsIgnoreCase("SNAPSHOT") || buildNumber.equalsIgnoreCase("IDEA")) {
            return true;
        }
        try {
            Integer.parseInt(buildNumber);
            return false;
        } catch (NumberFormatException e) {
            return true;
        }
    }

    public void check() {
        if (this.isCustom) {
            return;
        }
        int fileBuildId = Integer.parseInt(this.stemPlugin.getBuildNumber());
        int latestBuildId = this.getJenkinsNewestJobId(this.stemPlugin.getBuildJobName());
        if (latestBuildId <= fileBuildId) {
            this.updateAvailable = false;
            this.updateAvailableBuildId = -1;
            return;
        }
        String fileBuiltRevision = getJenkinsBuiltRevision(this.stemPlugin.getBuildJobName(), fileBuildId);
        String latestBuiltRevision = getJenkinsBuiltRevision(this.stemPlugin.getBuildJobName(), latestBuildId);
        if (!fileBuiltRevision.equalsIgnoreCase(latestBuiltRevision)) {
            this.updateAvailable = true;
            this.updateAvailableBuildId = latestBuildId;
        } else {
            this.updateAvailable = false;
            this.updateAvailableBuildId = -1;
        }
    }

    public boolean update() {
        if (this.locked.get()) {
            return true;
        }
        if (!this.updateAvailable || this.updateAvailableBuildId < 0) {
            return false;
        }
        JSONObject artifactData = this.getArtifactData(this.stemPlugin.getBuildJobName(), this.updateAvailableBuildId);
        if (artifactData == null) {
            STEMApp.LOGGER.ERROR("No artifact data for " + this.stemPlugin.getPluginName() + " build #" + this.updateAvailableBuildId);
            return false;
        }
        JSONArray artifacts = artifactData.optJSONArray("artifacts");
        String baseUrl = artifactData.optString("url", null);
        if (artifacts == null || baseUrl == null) {
            STEMApp.LOGGER.ERROR("Invalid artifact data for " + this.stemPlugin.getPluginName());
            return false;
        }
        if (!baseUrl.endsWith("/")) {
            baseUrl = baseUrl + "/";
        }

        boolean success = true;
        for (int i = 0; i < artifacts.length(); i++) {
            JSONObject artifact = artifacts.optJSONObject(i);
            if (artifact == null) {
                continue;
            }
            String fileName = artifact.optString("fileName", "");
            String relPath = artifact.optString("relativePath", null);
            if (relPath == null) {
                continue;
            }
            String downloadUrl = baseUrl + "artifact/" + relPath;
            if (isSecondaryJar(fileName)) {
                /* sources/javadoc jars and the stem-api jar (shipped inside coreDependencies.zip) */
                continue;
            }
            try {
                if (fileName.endsWith(".jar")) {
                    downloadArtifact(downloadUrl, this.stemPlugin.getFilePath());
                } else if (fileName.equalsIgnoreCase("libraries.zip") || fileName.equalsIgnoreCase("coreDependencies.zip")) {
                    if (this.isStemFramework) {
                        this.updateCoreLibraries(downloadUrl);
                    } else {
                        downloadArtifact(downloadUrl, new File(this.stemPlugin.getDataFolder(), fileName).toPath());
                    }
                }
            } catch (IOException e) {
                success = false;
                STEMApp.LOGGER.ERROR(e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        this.locked.set(success);
        return success;
    }

    private boolean isSecondaryJar(String fileName) {
        if (!fileName.endsWith(".jar")) {
            return false;
        }
        if (fileName.endsWith("-sources.jar") || fileName.endsWith("-javadoc.jar")) {
            return true;
        }
        return this.isStemFramework && fileName.startsWith("stem-api");
    }

    private void updateCoreLibraries(String downloadUrl) throws IOException, InterruptedException {
        Path zipFile = new File(this.stemPlugin.getTempFolder(), "core.zip").toPath();
        downloadArtifact(downloadUrl, zipFile);
        Path rootPath = this.stemPlugin.getDataFolder().toPath();
        File oldCores = new File(rootPath.toFile(), "core");
        if (!FileSystem.deleteFolder(oldCores)) {
            throw new IOException("Something went wrong while removing old core!");
        }
        SafeFiles.unzip(zipFile, rootPath, newFile ->
                STEMApp.LOGGER.INFO("Copy core library file " + newFile.getFileName() + " for STEM framework"));
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
        return this.locked.get();
    }

    public STEMPlugin getStemPlugin() {
        return stemPlugin;
    }

    private String jenkinsJobUrl(String jobName) {
        String base = this.pluginModule.jenkinsURL;
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base + "/job/" + jobName + "/";
    }

    private JSONObject fetchJson(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url)).timeout(REQUEST_TIMEOUT).GET().build();
            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                STEMApp.LOGGER.ERROR("Jenkins request failed with HTTP " + response.statusCode() + ": " + url);
                return null;
            }
            return new JSONObject(response.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } catch (Exception e) {
            STEMApp.LOGGER.ERROR("Jenkins request failed: " + url + " (" + e + ")");
            return null;
        }
    }

    private String getJenkinsBuiltRevision(String jobName, int jobId) {
        JSONObject jsonObject = fetchJson(jenkinsJobUrl(jobName) + jobId + "/api/json");
        if (jsonObject == null) {
            return "error";
        }
        JSONArray actions = jsonObject.optJSONArray("actions");
        if (actions == null) {
            return "error";
        }
        for (int i = 0; i < actions.length(); i++) {
            JSONObject action = actions.optJSONObject(i);
            if (action == null) {
                continue;
            }
            JSONObject revision = action.optJSONObject("lastBuiltRevision");
            if (revision != null) {
                return revision.optString("SHA1", "error");
            }
        }
        return "error";
    }

    private int getJenkinsNewestJobId(String jobName) {
        JSONObject jsonObject = fetchJson(jenkinsJobUrl(jobName) + "lastSuccessfulBuild/api/json");
        if (jsonObject == null || !jsonObject.has("id")) {
            return 0;
        }
        try {
            return Integer.parseInt(jsonObject.optString("id", "0"));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void downloadArtifact(String artifactUrl, Path destination) throws IOException, InterruptedException {
        if (destination == null) {
            throw new IOException("No destination for download " + artifactUrl);
        }
        HttpRequest request = HttpRequest.newBuilder(URI.create(artifactUrl)).timeout(DOWNLOAD_TIMEOUT).GET().build();
        HttpResponse<InputStream> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());
        try (InputStream in = response.body()) {
            if (response.statusCode() != 200) {
                throw new IOException("Download failed with HTTP " + response.statusCode() + ": " + artifactUrl);
            }
            SafeFiles.writeAtomically(in, destination);
        }
    }

    private JSONObject getArtifactData(String jobName, int jobId) {
        return fetchJson(jenkinsJobUrl(jobName) + jobId + "/api/json");
    }
}
