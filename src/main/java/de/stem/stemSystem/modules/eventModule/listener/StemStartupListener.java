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

package de.stem.stemSystem.modules.eventModule.listener;

import de.stem.stemSystem.STEMSystemApp;
import de.stem.stemSystem.modules.eventModule.events.StemStartupEvent;
import de.stem.stemSystem.modules.eventModule.handler.StemEventHandler;
import de.stem.stemSystem.modules.eventModule.handler.StemEventPriority;

public class StemStartupListener {

    @StemEventHandler(priority = StemEventPriority.NORMAL)
    public void onStartupTree(StemStartupEvent event) {
        STEMSystemApp.LOGGER.CORE("STEM-System startup finished in " + event.getStartupTime() + " ms.");
    }
}
