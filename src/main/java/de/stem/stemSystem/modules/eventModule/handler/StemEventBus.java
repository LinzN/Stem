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

package de.stem.stemSystem.modules.eventModule.handler;


import de.stem.stemSystem.STEMSystemApp;
import de.stem.stemSystem.modules.eventModule.StemEvent;

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
                    STEMSystemApp.LOGGER.ERROR("Method " + m + " in class " + listener.getClass() + " annotated with " + annotation + " does not have single argument");
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
        STEMSystemApp.LOGGER.DEBUG("Fire event " + event.getName());
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
                            STEMSystemApp.LOGGER.ERROR(e);
                        }
                    }
                }
            }
        }
    }
}
