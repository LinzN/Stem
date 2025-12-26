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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class StemEventBus {
    private final Map<Object, Map<Method, Class<StemEvent>>> activeListener;


    public StemEventBus() {
        this.activeListener = new HashMap<>();
    }

    /**
     * Gets methods and event class in a listener
     *
     * @param listener Listener to check if a method has an annotation
     * @return Map with event class and methods for this listener
     */
    private Map<Method, Class<StemEvent>> findHandlers(Object listener) {
        Map<Method, Class<StemEvent>> methods = new HashMap<>();

        for (Method m : listener.getClass().getDeclaredMethods()) {
            StemEventHandler annotation = m.getAnnotation(StemEventHandler.class);
            if (annotation != null) {
                Class<?>[] params = m.getParameterTypes();
                if (params.length != 1) {
                    STEMApp.LOGGER.ERROR("Method " + m + " in class " + listener.getClass() + " annotated with " + annotation + " does not have single argument");
                    continue;
                }
                Class<StemEvent> iEvent = (Class<StemEvent>) params[0];
                methods.put(m, iEvent);
            }
        }
        return methods;
    }


    /**
     * Call a listener method
     *
     * @param event         IEvent to call
     * @param method        Method in listener
     * @param classInstance classInstance object which contains the method to call
     */
    private void callMethod(StemEvent event, Method method, Object classInstance) {
        try {
            method.invoke(classInstance, event);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }


    /**
     * Register a new Event listener classInstance
     *
     * @param classInstance Event listener classInstance to register
     */
    public void register(Object classInstance) {
        Map<Method, Class<StemEvent>> handler = findHandlers(classInstance);
        this.activeListener.put(classInstance, handler);
    }


    /**
     * Unregister a event listener classInstance
     *
     * @param classInstance Event listener classInstance to unregister
     */
    public void unregister(Object classInstance) {
        activeListener.remove(classInstance);
    }

    /**
     * Call all listener with the IEvent
     *
     * @param event StemEvent to call in classInstance
     */
    public void fireEvent(final StemEvent event) {
        STEMApp.LOGGER.DEBUG("Fire event " + event.getName());
        if (!event.isCanceled()) {
            fireEventPriority(event, StemEventPriority.HIGH);
        }
        if (!event.isCanceled()) {
            fireEventPriority(event, StemEventPriority.NORMAL);
        }
        if (!event.isCanceled()) {
            fireEventPriority(event, StemEventPriority.LOW);
        }
        fireEventPriority(event, StemEventPriority.CANCELED);
    }

    private void fireEventPriority(StemEvent event, StemEventPriority stemEventPriority) {
        for (Object classInstance : this.activeListener.keySet()) {
            Map<Method, Class<StemEvent>> handler = this.activeListener.get(classInstance);
            for (Method method : handler.keySet()) {
                Class<StemEvent> stemEventClass = handler.get(method);
                if (stemEventClass.equals(event.getClass())) {
                    StemEventHandler annotation = method.getAnnotation(StemEventHandler.class);
                    StemEventPriority priority = annotation.priority();
                    if (priority == stemEventPriority) {
                        try {
                            callMethod(event, method, classInstance);
                        } catch (Exception e) {
                            STEMApp.LOGGER.ERROR(e);
                        }
                    }
                }
            }
        }
    }
}
