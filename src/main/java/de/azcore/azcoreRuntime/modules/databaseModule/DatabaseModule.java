/*
 * Copyright (C) 2020. Niklas Linz - All Rights Reserved
 * You may use, distribute and modify this code under the
 * terms of the LGPLv3 license, which unfortunately won't be
 * written for another century.
 *
 * You should have received a copy of the LGPLv3 license with
 * this file. If not, please write to: niklas.linz@enigmar.de
 *
 */

package de.azcore.azcoreRuntime.modules.databaseModule;

import de.azcore.azcoreRuntime.AZCoreRuntimeApp;
import de.azcore.azcoreRuntime.AppLogger;
import de.azcore.azcoreRuntime.modules.AbstractModule;
import de.azcore.azcoreRuntime.utils.Color;
import de.linzn.simplyConfiguration.FileConfiguration;
import de.linzn.simplyConfiguration.provider.YamlConfiguration;

import java.io.File;
import java.sql.*;

public class DatabaseModule extends AbstractModule {
    // Define variables
    private AZCoreRuntimeApp azCoreRuntime;

    private FileConfiguration fileConfiguration;

    private String url;
    private String username;
    private String password;


    /* Create class instance */
    public DatabaseModule(AZCoreRuntimeApp azCoreRuntime) {
        this.initConfig();
        this.azCoreRuntime = azCoreRuntime;
        Connection connection = getConnection();
        if (connection != null) {
            releaseConnection(connection);
        }
    }


    /* Return a new mysql connection */
    public Connection getConnection() {
        try {
            return DriverManager.getConnection(this.url, this.username, this.password);
        } catch (SQLException e) {
            if (AppLogger.getVerbose()) {
                e.printStackTrace();
            }
            AppLogger.logger(Color.RED + "MySQL connection is invalid!" + Color.RESET, false, false);
        }
        return null;
    }

    /* Clear an mysql connection*/
    public void releaseConnection(Connection connection) {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
            e.printStackTrace();
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
            e.printStackTrace();
        }
    }

    private void initConfig() {
        this.fileConfiguration = YamlConfiguration.loadConfiguration(new File("module_database.yml"));

        this.url = "jdbc:mysql://" + this.fileConfiguration.getString("sqlHostname", "127.0.0.1") + ":" + this.fileConfiguration.getInt("sqlPort", 3306) + "/"
                + this.fileConfiguration.getString("sqlDatabaseName", "azcore_db");
        this.username = this.fileConfiguration.getString("sqlUserName", "azcore");
        this.password = this.fileConfiguration.getString("sqlPassword", "test123");

        this.fileConfiguration.save();
    }
}
