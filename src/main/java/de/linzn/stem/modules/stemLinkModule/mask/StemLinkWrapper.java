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

package de.linzn.stem.modules.stemLinkModule.mask;

import de.linzn.stemLink.components.IStemLinkWrapper;
import de.linzn.stem.STEMApp;
import de.linzn.stem.modules.stemLinkModule.StemLinkModule;

import java.util.logging.Level;

public class StemLinkWrapper implements IStemLinkWrapper {

    private final StemLinkModule stemLinkModule;

    public StemLinkWrapper(StemLinkModule stemLinkModule) {
        this.stemLinkModule = stemLinkModule;
    }

    @Override
    public void runThread(Runnable runnable) {
        STEMApp.getInstance().getScheduler().runTask(this.stemLinkModule.getModulePlugin(), runnable);
    }

    @Override
    public void log(Object s, Level level) {
        if (level == Level.INFO) {
            STEMApp.LOGGER.INFO(s);
        } else if (level == Level.FINE) {
            STEMApp.LOGGER.DEBUG(s);
        } else if (level == Level.WARNING) {
            STEMApp.LOGGER.WARNING(s);
        } else if (level == Level.CONFIG) {
            STEMApp.LOGGER.CONFIG(s);
        } else if (level == Level.SEVERE) {
            STEMApp.LOGGER.ERROR(s);
        } else {
            STEMApp.LOGGER.DEBUG(s);
        }

    }


}
