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

package de.linzn.stem.core.api;

import de.linzn.stem.STEMApp;
import de.linzn.stem.api.event.EventService;
import de.linzn.stem.modules.eventModule.StemEvent;
import de.linzn.stem.modules.pluginModule.STEMPlugin;

final class EventServiceAdapter implements EventService {
    private final STEMApp stemApp;
    private final STEMPlugin owner;

    EventServiceAdapter(STEMApp stemApp, STEMPlugin owner) {
        this.stemApp = stemApp;
        this.owner = owner;
    }

    @Override
    public void register(Object listener) {
        this.stemApp.getEventModule().getStemEventBus().register(listener, owner);
    }

    @Override
    public void unregister(Object listener) {
        this.stemApp.getEventModule().getStemEventBus().unregister(listener);
    }

    @Override
    public void fire(StemEvent event) {
        this.stemApp.getEventModule().getStemEventBus().fireEvent(event);
    }
}
