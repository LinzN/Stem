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

package de.linzn.stem.modules.stemLinkModule.mask;

import de.linzn.stem.STEMApp;
import de.linzn.stem.modules.stemLinkModule.StemLinkModule;
import de.linzn.stemLink.components.IStemLinkWrapper;

import java.util.logging.Level;

public class StemLinkWrapper implements IStemLinkWrapper {

    private final StemLinkModule stemLinkModule;

    public StemLinkWrapper(StemLinkModule stemLinkModule) {
        this.stemLinkModule = stemLinkModule;
    }

    /**
     * StemLink uses this for its long-running accept and connection loops as well as for every received
     * packet. Each runnable gets its own virtual thread, so connections never occupy a shared pool and
     * any number of clients can be served.
     */
    @Override
    public void runThread(Runnable runnable) {
        Thread.ofVirtual().name("STEM-StemLink-", 0).start(() -> {
            try {
                runnable.run();
            } catch (Exception e) {
                STEMApp.LOGGER.ERROR(e);
            } catch (Throwable throwable) {
                STEMApp.LOGGER.ERROR("Uncaught error in StemLink thread: " + throwable);
            }
        });
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
