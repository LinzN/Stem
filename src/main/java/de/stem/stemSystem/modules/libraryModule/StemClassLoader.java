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

package de.stem.stemSystem.modules.libraryModule;

import de.stem.stemSystem.STEMSystemApp;

import java.net.URL;
import java.net.URLClassLoader;

public class StemClassLoader extends URLClassLoader {
    public StemClassLoader() {
        super(new URL[]{}, ClassLoader.getSystemClassLoader());
        STEMSystemApp.LOGGER.CORE("Loading custom classloader for library and plugin support");
    }

    @Override
    public void addURL(URL url) {
        super.addURL(url);
    }
}
