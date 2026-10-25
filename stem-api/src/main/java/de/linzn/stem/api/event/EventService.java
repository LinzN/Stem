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

package de.linzn.stem.api.event;

import de.linzn.stem.modules.eventModule.StemEvent;

/**
 * Event listener registration and event firing.
 * <p>
 * A listener is any object with methods annotated with
 * {@link de.linzn.stem.modules.eventModule.handler.StemEventHandler} that take exactly one event argument.
 * Handlers are called for the exact event class. Exceptions in handlers are logged and do not affect other handlers.
 */
public interface EventService {

    /**
     * Register a listener for the owner plugin. Registering the same object again replaces its handlers.
     */
    void register(Object listener);

    /**
     * Unregister a listener.
     */
    void unregister(Object listener);

    /**
     * Fire an event synchronously on the calling thread.
     */
    void fire(StemEvent event);
}
