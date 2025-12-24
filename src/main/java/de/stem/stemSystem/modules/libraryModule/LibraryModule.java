/*
 * Copyright (c) 2025 MirraNET, Niklas Linz. All rights reserved.
 *
 * This file is part of the MirraNET project and is licensed under the
 * GNU Lesser General Public License v3.0 (LGPLv3).
 *
 * You may use, distribute and modify this code under the terms
 * of the LGPLv3 license. You should have received a copy of the
 * license along with this file. If not, see <https://www.gnu.org/licenses/lgpl-3.0.html>
 * or contact: niklas.linz@mirranet.de
 */

package de.stem.stemSystem.modules.libraryModule;

import de.stem.stemSystem.STEMSystemApp;
import de.stem.stemSystem.modules.AbstractModule;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;

public class LibraryModule extends AbstractModule {
    // Define variables
    private final STEMSystemApp stemSystemApp;


    /* Create class instance */
    public LibraryModule(STEMSystemApp stemSystemApp) {
        this.stemSystemApp = stemSystemApp;
        this.stemSystemApp.setClassLoader(new StemClassLoader());

        try {
            loadJarFiles();
        } catch (IOException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            STEMSystemApp.LOGGER.ERROR(e);
        }
    }

    private void loadJarFiles() throws IOException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        File dependencyDirectory = new File("libraries");
        if (!dependencyDirectory.exists()) {
            dependencyDirectory.mkdirs();
        }
        File[] files = dependencyDirectory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().endsWith(".jar")) {
                    this.addJarFile(file);
                }
            }
        }
    }

    public synchronized void addJarFile(File jarFile) throws MalformedURLException {
        STEMSystemApp.getInstance().getStemClassLoader().addURL(jarFile.toURI().toURL());
        STEMSystemApp.LOGGER.DEBUG("LOAD LIBRARY FILE: " + jarFile.getName());
    }


    @Override
    public void onShutdown() {
        this.stemSystemApp.setClassLoader(null);
    }
}
