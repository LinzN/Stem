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

package de.linzn.stem.api.data;

import de.linzn.stem.modules.databaseModule.DataContainer;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Key/value JSON storage and raw database access. Keys are case-insensitive.
 */
public interface DataService {

    /**
     * @return stored data for the key, if present
     */
    Optional<DataContainer> get(String key);

    /**
     * Store data. Existing data with the same key is replaced.
     */
    void set(DataContainer container);

    /**
     * Store JSON data under a key. Existing data with the same key is replaced.
     */
    default void set(String key, JSONObject json) {
        DataContainer container = new DataContainer(key);
        container.setDataObject(json);
        set(container);
    }

    /**
     * Run work with a database connection. The connection is always released afterwards.
     *
     * @throws SQLException if no connection is available or the work fails
     */
    <T> T withConnection(SqlFunction<T> work) throws SQLException;
}
