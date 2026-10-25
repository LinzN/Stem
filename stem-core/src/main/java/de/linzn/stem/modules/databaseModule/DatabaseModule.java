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

package de.linzn.stem.modules.databaseModule;

import de.linzn.simplyConfiguration.FileConfiguration;
import de.linzn.simplyConfiguration.provider.YamlConfiguration;
import de.linzn.simplyDatabase.DatabaseProvider;
import de.linzn.simplyDatabase.provider.MySQLProvider;
import de.linzn.simplyDatabase.provider.SQLiteProvider;
import de.linzn.stem.STEMApp;
import de.linzn.stem.modules.AbstractModule;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;
import java.util.concurrent.locks.ReentrantLock;

public class DatabaseModule extends AbstractModule {
    // Define variables
    private final DatabaseProvider databaseProvider;
    /* Serializes writes (SQLite allows only one writer at a time) */
    private final ReentrantLock writeLock = new ReentrantLock();

    private FileConfiguration fileConfiguration;

    private String provider;
    private String hostname;
    private int port;
    private String database;
    private String username;
    private String password;


    /* Create class instance */
    public DatabaseModule(STEMApp stemApp) {
        super(stemApp);
        this.initConfig();
        if (this.provider.equalsIgnoreCase("mysql")) {
            this.databaseProvider = new MySQLProvider(this.hostname, this.port, this.username, this.password, this.database);
        } else {
            this.databaseProvider = new SQLiteProvider("STEM.db");
        }
        this.createDataTable();
    }

    /* Return a new database connection. Callers must release it with releaseConnection. */
    public Connection getConnection() {
        return this.databaseProvider.getConnection();
    }

    /* Release a database connection; null is ignored */
    public void releaseConnection(Connection connection) {
        if (connection != null) {
            this.databaseProvider.releaseConnection(connection);
        }
    }

    private void createDataTable() {
        Connection con = this.getConnection();
        if (con == null) {
            STEMApp.LOGGER.ERROR("No database connection available!");
            return;
        }
        try (Statement st = con.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS `data` (`key` VARCHAR(191) NOT NULL PRIMARY KEY, `jsonData` LONGTEXT)");
        } catch (SQLException e) {
            STEMApp.LOGGER.ERROR(e);
        } finally {
            this.releaseConnection(con);
        }
    }

    public DataContainer getData(String name) {
        if (name == null) {
            return null;
        }
        String key = name.toLowerCase(Locale.ROOT);
        Connection con = this.getConnection();
        if (con == null) {
            STEMApp.LOGGER.ERROR("No database connection available!");
            return null;
        }
        try (PreparedStatement st = con.prepareStatement("SELECT `jsonData` FROM `data` WHERE `key` = ?")) {
            st.setString(1, key);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    DataContainer dataContainer = new DataContainer(key);
                    dataContainer.setDataObject(rs.getString("jsonData"));
                    return dataContainer;
                }
            }
        } catch (SQLException | RuntimeException e) {
            STEMApp.LOGGER.ERROR(e);
        } finally {
            this.releaseConnection(con);
        }
        return null;
    }

    /**
     * Store a data container. Existing data with the same key is replaced.
     *
     * @param dataContainer container to store
     */
    public void setData(DataContainer dataContainer) {
        if (dataContainer == null || dataContainer.name == null || dataContainer.getJSON() == null) {
            STEMApp.LOGGER.ERROR("Invalid data container, nothing stored");
            return;
        }
        Connection con = this.getConnection();
        if (con == null) {
            STEMApp.LOGGER.ERROR("No database connection available!");
            return;
        }
        this.writeLock.lock();
        try (PreparedStatement update = con.prepareStatement("UPDATE `data` SET `jsonData` = ? WHERE `key` = ?")) {
            String json = dataContainer.getJSONString();
            update.setString(1, json);
            update.setString(2, dataContainer.name);
            if (update.executeUpdate() == 0) {
                try (PreparedStatement insert = con.prepareStatement("INSERT INTO `data` (`key`, `jsonData`) VALUES (?, ?)")) {
                    insert.setString(1, dataContainer.name);
                    insert.setString(2, json);
                    insert.executeUpdate();
                }
            }
        } catch (SQLException e) {
            STEMApp.LOGGER.ERROR(e);
        } finally {
            this.writeLock.unlock();
            this.releaseConnection(con);
        }
    }

    private void initConfig() {
        this.fileConfiguration = YamlConfiguration.loadConfiguration(new File("module_database.yml"));
        this.provider = this.fileConfiguration.getString("provider", "sqlite");
        this.hostname = this.fileConfiguration.getString("sqlHostname", "127.0.0.1");
        this.port = this.fileConfiguration.getInt("sqlPort", 3306);
        this.database = this.fileConfiguration.getString("sqlDatabaseName", "stem_db");
        this.username = this.fileConfiguration.getString("sqlUserName", "stem");
        this.password = this.fileConfiguration.getString("sqlPassword", "test123");

        this.fileConfiguration.save();
    }

    @Override
    public void onShutdown() {

    }
}
