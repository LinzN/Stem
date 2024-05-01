package de.stem.stemSystem.modules.cloudModule;

import org.aarboard.nextcloud.api.NextcloudConnector;
import org.aarboard.nextcloud.api.filesharing.SharePermissions;
import org.aarboard.nextcloud.api.filesharing.ShareType;

public class CloudFile {
    private final NextcloudConnector nextcloudConnector;
    private final String path;

    CloudFile(NextcloudConnector nextcloudConnector, String path) {
        this.nextcloudConnector = nextcloudConnector;
        this.path = path;
    }

    public String createPublicShareLink() {
        return this.nextcloudConnector.doShare(this.path, ShareType.PUBLIC_LINK, null, false, null, new SharePermissions(SharePermissions.SingleRight.READ)).getUrl();
    }

    public boolean hasShare() {
        return !this.nextcloudConnector.getShares(this.path, false, false).isEmpty();
    }

}
