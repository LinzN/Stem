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

package de.linzn.stem;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class TestApp {

    public TestApp() {

    }

    static void main(String[] args) {
        new TestApp().test();
    }

    public void downloadArtifact(String artifactUrl, Path destination) throws IOException {
        try (InputStream in = new URL(artifactUrl).openStream()) {
            Files.copy(in, destination, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public void test() {
        JSONObject testObject = getJSONObjectData();
        try {
            collectDownloadData(testObject);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public JSONObject getJSONObjectData() {
        JSONObject jsonObject = null;
        try {
            URL url = new URL("https://builds.mirranet.de/job/Calendar/lastSuccessfulBuild/api/json?pretty=true");
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

    public void collectDownloadData(JSONObject jsonObject) throws IOException {
        File file = new File("testData");
        if (!file.exists()) {
            file.mkdirs();
        }

        JSONArray artifacts = jsonObject.getJSONArray("artifacts");
        String baseUrl = jsonObject.getString("url");
        for (int i = 0; i < artifacts.length(); i++) {
            JSONObject artifact = artifacts.getJSONObject(i);
            String fileName = artifact.getString("fileName");
            String relPath = artifact.getString("relativePath");
            String downloadUrl = baseUrl + "artifact/" + relPath;

            // Download JAR
            if (fileName.endsWith(".jar")) {
                downloadArtifact(downloadUrl, Paths.get(new File(file, fileName).getAbsolutePath()));
            }
            // Download libraries.zip, falls vorhanden
            else if (fileName.endsWith("libraries.zip")) {
                downloadArtifact(downloadUrl, Paths.get(new File(file, fileName).getAbsolutePath()));
            }
        }
    }
}
