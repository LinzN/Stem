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

package de.linzn.stem.modules.cloudModule;

import de.linzn.simplyConfiguration.FileConfiguration;
import de.linzn.simplyConfiguration.provider.YamlConfiguration;
import de.linzn.stem.STEMApp;
import de.linzn.stem.modules.AbstractModule;
import org.aarboard.nextcloud.api.NextcloudConnector;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.util.UUID;


public class CloudModule extends AbstractModule {

    private FileConfiguration fileConfiguration;
    private NextcloudConnector nextcloudConnector;

    private boolean isEnabled;
    private String cloudURL;
    private int cloudPort;
    private String cloudUser;
    private String cloudPassToken;

    public CloudModule(STEMApp stemApp) {
        super(stemApp);
        this.initConfig();
        if (this.isEnabled) {
            this.initCloudSetup();
        }
    }

    private void initConfig() {
        this.fileConfiguration = YamlConfiguration.loadConfiguration(new File("module_cloud.yml"));
        this.isEnabled = this.fileConfiguration.getBoolean("enabled", true);
        this.cloudURL = this.fileConfiguration.getString("url", "cloud.de");
        this.cloudPort = this.fileConfiguration.getInt("port", 443);
        this.cloudUser = this.fileConfiguration.getString("user", "testuser");
        this.cloudPassToken = this.fileConfiguration.getString("passToken", "GeheimesPW");
        this.fileConfiguration.save();
    }

    private void initCloudSetup() {
        this.nextcloudConnector = new NextcloudConnector(this.cloudURL, true, cloudPort, cloudUser, cloudPassToken);
    }

    public CloudFile getCloudFile(String absoluteFilePath) {
        if (this.nextcloudConnector.fileExists(absoluteFilePath)) {
            return new CloudFile(this.nextcloudConnector, absoluteFilePath);
        }
        return null;
    }

    public CloudFile uploadFileRandomName(File file, String absoluteFolderPath) {
        String filename = UUID.randomUUID() + "." + FilenameUtils.getExtension(file.getPath());
        return uploadFile(file, absoluteFolderPath, filename);
    }

    public CloudFile uploadFile(File file, String absoluteFolderPath, String cloudFileName) {
        String path = absoluteFolderPath + cloudFileName;

        if (!this.nextcloudConnector.fileExists(path)) {
            this.nextcloudConnector.uploadFile(file, path);
            return new CloudFile(this.nextcloudConnector, path);
        }
        return null;
    }

    @Override
    public void onShutdown() {
        try {
            this.nextcloudConnector.shutdown();
        } catch (IOException e) {
            STEMApp.LOGGER.ERROR(e);
        }
    }

}
