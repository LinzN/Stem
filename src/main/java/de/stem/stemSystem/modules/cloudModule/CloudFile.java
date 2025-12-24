/*
 * Copyright (c) 2025 MirraNET, Niklas Linz. All rights reserved.
 *
 * This file is part of the MirraNET project and is licensed under the
 * GNU Lesser General Public License v3.0 (LGPLv3).
 *
 * You may use, distribute and modify this code under the terms
 * of the LGPLv3 license. You should have received a copy of the
 * license along with this file. If not, see <https://www.gnu.org/licenses/lgpl-3.0.html>
 * or contact: niklas.linz@mirranet.de
 */

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
