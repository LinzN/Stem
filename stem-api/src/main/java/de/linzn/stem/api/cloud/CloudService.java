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

package de.linzn.stem.api.cloud;

import java.io.File;
import java.util.Optional;

/**
 * Nextcloud file access. All methods return empty results when the cloud module is disabled.
 */
public interface CloudService {

    /**
     * @return true if the cloud module is enabled
     */
    boolean isEnabled();

    /**
     * @return the file if it exists
     */
    Optional<CloudFileHandle> getFile(String absolutePath);

    /**
     * Upload a file. Fails if a file with that name already exists.
     *
     * @param folder   absolute folder path ending with "/"
     * @param fileName target file name
     */
    Optional<CloudFileHandle> upload(File file, String folder, String fileName);

    /**
     * Upload a file under a random name, keeping its extension.
     */
    Optional<CloudFileHandle> uploadWithRandomName(File file, String folder);
}
