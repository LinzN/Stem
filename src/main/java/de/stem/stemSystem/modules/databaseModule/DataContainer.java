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

package de.stem.stemSystem.modules.databaseModule;

import org.json.JSONObject;

public class DataContainer {

    public String name;
    private JSONObject dataObject;

    public DataContainer(String name) {
        this.name = name.toLowerCase();
    }

    public void setDataObject(JSONObject jsonObject) {
        this.dataObject = jsonObject;
    }

    public void setDataObject(String jsonString) {
        this.dataObject = new JSONObject(jsonString);
    }

    public String getJSONString() {
        return this.dataObject.toString();
    }

    public JSONObject getJSON() {
        return this.dataObject;
    }
}
