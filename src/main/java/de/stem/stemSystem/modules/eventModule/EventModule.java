/*
 * Copyright (C) 2021. Niklas Linz - All Rights Reserved
 * You may use, distribute and modify this code under the
 * terms of the LGPLv3 license, which unfortunately won't be
 * written for another century.
 *
 * You should have received a copy of the LGPLv3 license with
 * this file. If not, please write to: niklas.linz@enigmar.de
 *
 */

package de.stem.stemSystem.modules.eventModule;

import de.stem.stemSystem.STEMSystemApp;
import de.stem.stemSystem.modules.AbstractModule;
import de.stem.stemSystem.modules.eventModule.handler.StemEventBus;
import de.stem.stemSystem.modules.eventModule.listener.StemStartupListener;

public class EventModule extends AbstractModule {

    private STEMSystemApp stemSystemApp;
    private StemEventBus stemEventBus;

    public EventModule(STEMSystemApp stemSystemApp) {
        this.stemSystemApp = stemSystemApp;
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
