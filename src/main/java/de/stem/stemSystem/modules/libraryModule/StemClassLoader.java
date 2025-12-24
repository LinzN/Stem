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

import java.net.URL;
import java.net.URLClassLoader;

public class StemClassLoader extends URLClassLoader {
    public StemClassLoader() {
        super(new URL[]{}, ClassLoader.getSystemClassLoader());
        STEMSystemApp.LOGGER.CORE("Loading custom classloader for common jar libraries");
    }

    @Override
    public void addURL(URL url) {
        super.addURL(url);
    }
}
