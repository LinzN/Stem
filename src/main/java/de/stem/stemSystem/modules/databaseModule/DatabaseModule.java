/*
 * Copyright (C) 2021. Niklas Linz - All Rights Reserved
 * You may use, distribute and modify this code under the
 * terms of the LGPLv3 license, which unfortunately won't be
 * written for another century.
 *
 * You should have received a copy of the LGPLv3 license with
 * this file. If not, please write to: niklas.linz@enigmar.de
 *
 */

package de.stem.stemSystem.modules.databaseModule;

import de.linzn.simplyConfiguration.FileConfiguration;
import de.linzn.simplyConfiguration.provider.YamlConfiguration;
import de.linzn.simplyDatabase.DatabaseProvider;
import de.linzn.simplyDatabase.provider.SQLiteProvider;
import de.stem.stemSystem.STEMSystemApp;
import de.stem.stemSystem.modules.AbstractModule;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseModule extends AbstractModule {
    // Define variables
    private final STEMSystemApp stemSystemApp;
    private final DatabaseProvider databaseProvider;

    private FileConfiguration fileConfiguration;

    private String hostname;
    private int port;
    private String database;
    private String username;
    private String password;


    /* Create class instance */
    public DatabaseModule(STEMSystemApp stemSystemApp) {
        this.initConfig();
        //this.databaseProvider = new MySQLProvider(hostname, port, username, password, database);
        this.databaseProvider = new SQLiteProvider("STEM.db");
        this.stemSystemApp = stemSystemApp;
        Connection connection = getConnection();
        if (connection != null) {
            releaseConnection(connection);
        }
    }


    /* Return a new mysql connection */
    public Connection getConnection() {
        return this.databaseProvider.getConnection();
    }

    /* Clear an mysql connection*/
    public void releaseConnection(Connection connection) {
        this.databaseProvider.releaseConnection(connection);
    }


    public DataContainer getData(String name) {
        DataContainer dataContainer = null;
        try {
            Connection con = this.getConnection();
            Statement st = con.createStatement();
            String sqlquery = ("SELECT `jsonData` FROM `data` WHERE `key` = '" + name + "'");
            ResultSet rs = st.executeQuery(sqlquery);
            if (rs.next()) {
                String jsonData = rs.getString("jsonData");
                dataContainer = new DataContainer(name);
                dataContainer.setDataObject(jsonData);
            }
            this.releaseConnection(con);
        } catch (SQLException e) {
            STEMSystemApp.LOGGER.ERROR(e);
        }
        return dataContainer;
    }

    public void setData(DataContainer dataContainer) {
        try {
            Connection con = this.getConnection();
            Statement st = con.createStatement();
            String sqlquery = ("SELECT `jsonData` FROM `data` WHERE `key` = '" + dataContainer.name + "'");
            ResultSet rs = st.executeQuery(sqlquery);
            if (!rs.next()) {
                sqlquery = ("INSERT INTO `data` (key, jsonData) values ('" + dataContainer.name + "', '" + dataContainer.getJSONString() + "')");
                st.executeUpdate(sqlquery);
            }
            this.releaseConnection(con);
        } catch (SQLException e) {
            STEMSystemApp.LOGGER.ERROR(e);
        }
    }

    private void initConfig() {
        this.fileConfiguration = YamlConfiguration.loadConfiguration(new File("module_database.yml"));
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
