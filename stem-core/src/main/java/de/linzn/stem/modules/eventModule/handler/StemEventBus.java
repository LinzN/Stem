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

package de.linzn.stem.modules.eventModule.handler;

import de.linzn.stem.STEMApp;
import de.linzn.stem.modules.eventModule.StemEvent;
import de.linzn.stem.modules.pluginModule.PluginModule;
import de.linzn.stem.modules.pluginModule.STEMPlugin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Thread-safe event bus.
 * <p>
 * Handlers are resolved once at registration time and indexed by event class and priority, so firing an
 * event does no reflection lookups. Registration and firing may happen concurrently from any thread.
 * Every listener is tied to an owner plugin so it can be removed when the plugin is disabled.
 */
public class StemEventBus {
    private static final StemEventPriority[] PRIORITIES = StemEventPriority.values();
    private static final Handler[] NO_HANDLERS = new Handler[0];

    /* All registered listeners in registration order. Guarded by "this". */
    private final List<Registration> registrations = new ArrayList<>();
    /* Immutable index: event class -> handlers per priority ordinal. Rebuilt on every change. */
    private volatile Map<Class<?>, Handler[][]> index = Map.of();

    public StemEventBus() {
    }

    /**
     * Gets methods and event class in a listener
     *
     * @param listener Listener to check if a method has an annotation
     * @return list of handlers of this listener
     */
    private static List<Handler> findHandlers(Object listener) {
        List<Handler> handlers = new ArrayList<>();
        for (Method m : listener.getClass().getDeclaredMethods()) {
            StemEventHandler annotation = m.getAnnotation(StemEventHandler.class);
            if (annotation == null) {
                continue;
            }
            Class<?>[] params = m.getParameterTypes();
            if (params.length != 1 || !StemEvent.class.isAssignableFrom(params[0])) {
                STEMApp.LOGGER.ERROR("Method " + m + " in class " + listener.getClass() + " annotated with " + annotation + " does not have a single StemEvent argument");
                continue;
            }
            if (Modifier.isStatic(m.getModifiers())) {
                STEMApp.LOGGER.ERROR("Method " + m + " in class " + listener.getClass() + " is static and can not be used as event handler");
                continue;
            }
            if (!m.trySetAccessible()) {
                STEMApp.LOGGER.ERROR("Method " + m + " in class " + listener.getClass() + " is not accessible");
                continue;
            }
            handlers.add(new Handler(listener, m, params[0], annotation.priority()));
        }
        return handlers;
    }

    private static STEMPlugin resolveOwner(Object classInstance) {
        return PluginModule.findOwner(classInstance.getClass());
    }

    /**
     * Call a listener method
     *
     * @param event   event to call
     * @param handler handler to invoke
     */
    private static void callMethod(StemEvent event, Handler handler) {
        try {
            handler.method.invoke(handler.instance, event);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            logError(handler, cause);
        } catch (Throwable e) {
            logError(handler, e);
        }
    }

    private static void logError(Handler handler, Throwable throwable) {
        STEMApp.LOGGER.ERROR("Error in event handler " + handler.method.getDeclaringClass().getName() + "#" + handler.method.getName());
        if (throwable instanceof Exception exception) {
            STEMApp.LOGGER.ERROR(exception);
        } else {
            STEMApp.LOGGER.ERROR(throwable.toString());
        }
    }

    /**
     * Register a new Event listener classInstance. The owner plugin is detected automatically.
     *
     * @param classInstance Event listener classInstance to register
     */
    public void register(Object classInstance) {
        this.register(classInstance, resolveOwner(classInstance));
    }

    /**
     * Register a new Event listener classInstance for a plugin
     *
     * @param classInstance Event listener classInstance to register
     * @param owner         plugin owning this listener, may be null for core listeners
     */
    public void register(Object classInstance, STEMPlugin owner) {
        if (classInstance == null) {
            return;
        }
        List<Handler> handlers = findHandlers(classInstance);
        synchronized (this) {
            this.registrations.removeIf(registration -> registration.instance == classInstance);
            this.registrations.add(new Registration(classInstance, owner, handlers));
            this.rebuildIndex();
        }
    }

    /**
     * Unregister a event listener classInstance
     *
     * @param classInstance Event listener classInstance to unregister
     */
    public void unregister(Object classInstance) {
        synchronized (this) {
            if (this.registrations.removeIf(registration -> registration.instance == classInstance)) {
                this.rebuildIndex();
            }
        }
    }

    /**
     * Unregister all event listeners of a plugin
     *
     * @param owner plugin to remove all listeners for
     * @return number of removed listeners
     */
    public int unregisterAll(STEMPlugin owner) {
        if (owner == null) {
            return 0;
        }
        synchronized (this) {
            int before = this.registrations.size();
            this.registrations.removeIf(registration -> registration.owner == owner);
            int removed = before - this.registrations.size();
            if (removed > 0) {
                this.rebuildIndex();
            }
            return removed;
        }
    }

    /* Must be called while holding the lock */
    private void rebuildIndex() {
        Map<Class<?>, List<List<Handler>>> builder = new HashMap<>();
        for (Registration registration : this.registrations) {
            for (Handler handler : registration.handlers) {
                List<List<Handler>> perPriority = builder.computeIfAbsent(handler.eventClass, _ -> {
                    List<List<Handler>> lists = new ArrayList<>(PRIORITIES.length);
                    for (int i = 0; i < PRIORITIES.length; i++) {
                        lists.add(new ArrayList<>());
                    }
                    return lists;
                });
                perPriority.get(handler.priority.ordinal()).add(handler);
            }
        }
        Map<Class<?>, Handler[][]> newIndex = new HashMap<>();
        for (Map.Entry<Class<?>, List<List<Handler>>> entry : builder.entrySet()) {
            Handler[][] arrays = new Handler[PRIORITIES.length][];
            for (int i = 0; i < PRIORITIES.length; i++) {
                List<Handler> list = entry.getValue().get(i);
                arrays[i] = list.isEmpty() ? NO_HANDLERS : list.toArray(NO_HANDLERS);
            }
            newIndex.put(entry.getKey(), arrays);
        }
        this.index = Map.copyOf(newIndex);
    }

    /**
     * Call all listeners of the event. Handlers whose parameter type is {@link StemEvent} itself receive every
     * event (for example to log events or to build event based automations); they run after the handlers of the
     * same priority that are registered for the concrete event class.
     *
     * @param event StemEvent to call in classInstance
     */
    public void fireEvent(final StemEvent event) {
        if (event == null) {
            return;
        }
        STEMApp.LOGGER.DEBUG("Fire event " + event.getName());
        Map<Class<?>, Handler[][]> current = this.index;
        Handler[][] handlers = current.get(event.getClass());
        Handler[][] catchAll = event.getClass() == StemEvent.class ? null : current.get(StemEvent.class);
        if (handlers == null && catchAll == null) {
            return;
        }
        for (StemEventPriority priority : PRIORITIES) {
            if (priority != StemEventPriority.CANCELED && event.isCanceled()) {
                continue;
            }
            if (handlers != null) {
                fireEventPriority(event, handlers[priority.ordinal()]);
            }
            if (catchAll != null) {
                fireEventPriority(event, catchAll[priority.ordinal()]);
            }
        }
    }

    private void fireEventPriority(StemEvent event, Handler[] handlers) {
        for (Handler handler : handlers) {
            callMethod(event, handler);
        }
    }

    private record Handler(Object instance, Method method, Class<?> eventClass, StemEventPriority priority) {
    }

    private record Registration(Object instance, STEMPlugin owner, List<Handler> handlers) {
    }
}
