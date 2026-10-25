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
import de.linzn.stem.api.data.DataService;
import de.linzn.stem.api.data.SqlFunction;
import de.linzn.stem.modules.databaseModule.DataContainer;
import de.linzn.stem.modules.databaseModule.DatabaseModule;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;

final class DataServiceAdapter implements DataService {
    private final STEMApp stemApp;

    DataServiceAdapter(STEMApp stemApp) {
        this.stemApp = stemApp;
    }

    @Override
    public Optional<DataContainer> get(String key) {
        return Optional.ofNullable(this.stemApp.getDatabaseModule().getData(key));
    }

    @Override
    public void set(DataContainer container) {
        this.stemApp.getDatabaseModule().setData(Objects.requireNonNull(container, "container"));
    }

    @Override
    public <T> T withConnection(SqlFunction<T> work) throws SQLException {
        DatabaseModule module = this.stemApp.getDatabaseModule();
        Connection connection = module.getConnection();
        if (connection == null) {
            throw new SQLException("No database connection available");
        }
        try {
            return work.apply(connection);
        } finally {
            module.releaseConnection(connection);
        }
    }
}
