package de.stem.stemSystem.modules.cloudModule;

import org.aarboard.nextcloud.api.NextcloudConnector;
import org.aarboard.nextcloud.api.filesharing.SharePermissions;
import org.aarboard.nextcloud.api.filesharing.ShareType;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.UUID;

class CloudFile {
    private final NextcloudConnector nextcloudConnector;
    private final String path;

    public CloudFile(NextcloudConnector nextcloudConnector, String path) {
        this.nextcloudConnector = nextcloudConnector;
        this.path = path;
    }

    public String createPublicShareLink(String path) {
        return this.nextcloudConnector.doShare(path, ShareType.PUBLIC_LINK, null, false, null, new SharePermissions(SharePermissions.SingleRight.READ)).getUrl();
    }

    public boolean hasShare() {
        return !this.nextcloudConnector.getShares(this.path, false, false).isEmpty();
    }

    public static CloudFile getCloudFile(NextcloudConnector nextcloudConnector, String absoluteFilePath) {
        if (nextcloudConnector.fileExists(absoluteFilePath)) {
            return new CloudFile(nextcloudConnector, absoluteFilePath);
        }
        return null;
    }


    public static CloudFile uploadFileRandomName(NextcloudConnector nextcloudConnector, File file, String absoluteFolderPath) {
        String filename = UUID.randomUUID() + "." + FilenameUtils.getExtension(file.getPath());
        return uploadFile(nextcloudConnector, file, absoluteFolderPath, filename);
    }

    public static CloudFile uploadFile(NextcloudConnector nextcloudConnector, File file, String absoluteFolderPath, String cloudFileName) {
        String path = absoluteFolderPath + cloudFileName;

        if(!nextcloudConnector.fileExists(path)) {
            nextcloudConnector.uploadFile(file, path);
            return new CloudFile(nextcloudConnector, path);
        }
        return null;
    }

}
