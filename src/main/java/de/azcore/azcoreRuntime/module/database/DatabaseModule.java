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

package de.azcore.azcoreRuntime.module.database;

import de.azcore.azcoreRuntime.AZCoreRuntimeApp;
import de.azcore.azcoreRuntime.internal.containers.DataContainer;
import de.azcore.azcoreRuntime.internal.containers.Module;

import java.sql.*;

public class DatabaseModule extends Module {
    // Define variables
    private AZCoreRuntimeApp azCoreRuntime;
    private String url;
    private String username;
    private String password;

    /* Create class instance */
    public DatabaseModule(AZCoreRuntimeApp azCoreRuntime) {
        this.azCoreRuntime = azCoreRuntime;
        this.url = "jdbc:mysql://" + this.azCoreRuntime.getAppConfigurationModule().sqlHostName + ":" + this.azCoreRuntime.getAppConfigurationModule().sqlPort + "/"
                + this.azCoreRuntime.getAppConfigurationModule().sqlDatabaseName;
        this.username = this.azCoreRuntime.getAppConfigurationModule().sqlUserName;
        this.password = this.azCoreRuntime.getAppConfigurationModule().sqlPassword;
        this.releaseConnection(getConnection());
    }


    /* Return a new mysql connection */
    public Connection getConnection() {
        try {
            return DriverManager.getConnection(this.url, this.username, this.password);
        } catch (SQLException e) {
            e.printStackTrace();
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
}
