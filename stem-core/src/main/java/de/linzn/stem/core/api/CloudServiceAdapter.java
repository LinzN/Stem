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

package de.linzn.stem.core.api;

import de.linzn.stem.STEMApp;
import de.linzn.stem.api.cloud.CloudFileHandle;
import de.linzn.stem.api.cloud.CloudService;
import de.linzn.stem.modules.cloudModule.CloudModule;

import java.io.File;
import java.util.Optional;

final class CloudServiceAdapter implements CloudService {
    private final STEMApp stemApp;

    CloudServiceAdapter(STEMApp stemApp) {
        this.stemApp = stemApp;
    }

    private CloudModule module() {
        return this.stemApp.getCloudModule();
    }

    @Override
    public boolean isEnabled() {
        CloudModule module = module();
        return module != null && module.isEnabled();
    }

    @Override
    public Optional<CloudFileHandle> getFile(String absolutePath) {
        return isEnabled() ? Optional.ofNullable(module().getCloudFile(absolutePath)) : Optional.empty();
    }

    @Override
    public Optional<CloudFileHandle> upload(File file, String folder, String fileName) {
        return isEnabled() ? Optional.ofNullable(module().uploadFile(file, folder, fileName)) : Optional.empty();
    }

    @Override
    public Optional<CloudFileHandle> uploadWithRandomName(File file, String folder) {
        return isEnabled() ? Optional.ofNullable(module().uploadFileRandomName(file, folder)) : Optional.empty();
    }
}
