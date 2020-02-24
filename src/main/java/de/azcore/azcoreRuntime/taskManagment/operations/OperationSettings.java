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

package de.azcore.azcoreRuntime.taskManagment.operations;

import de.azcore.azcoreRuntime.AppLogger;

import java.util.HashMap;


public class OperationSettings {
    private HashMap<String, Object> data;

    public OperationSettings() {
        this.data = new HashMap<>();
    }

    public void addSetting(String key, Object object) {
        this.data.put(key.toLowerCase(), object);
    }

    public Object getSetting(String key) {
        if (!this.data.containsKey(key.toLowerCase())) {
            AppLogger.logger("Error in OperationSettings. No entry named " + key, true);
        }
        return this.data.get(key.toLowerCase());
    }

    public String getStringSetting(String key) {
        return (String) this.getSetting(key);
    }

    public Boolean getBooleanSetting(String key) {
        return (Boolean) this.getSetting(key);
    }

    public int getIntSetting(String key) {
        return (int) this.getSetting(key);
    }
}
