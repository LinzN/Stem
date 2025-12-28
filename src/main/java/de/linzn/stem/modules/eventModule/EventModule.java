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

package de.linzn.stem.modules.eventModule;

import de.linzn.stem.STEMApp;
import de.linzn.stem.modules.AbstractModule;
import de.linzn.stem.modules.eventModule.handler.StemEventBus;
import de.linzn.stem.modules.eventModule.listener.StemStartupListener;

public class EventModule extends AbstractModule {

    private StemEventBus stemEventBus;

    public EventModule(STEMApp stemApp) {
        super(stemApp);
        this.stemEventBus = new StemEventBus();
        this.stemEventBus.register(new StemStartupListener());
    }

    public StemEventBus getStemEventBus() {
        return this.stemEventBus;
    }

    @Override
    public void onShutdown() {

    }

}
