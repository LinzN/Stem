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
    private final Map<Object, Map<Class<StemEvent>, Method>> activeListener;


    public StemEventBus() {
        this.activeListener = new HashMap<>();
    }

    /**
     * Gets methods and event class in a listener
     *
     * @param listener Listener to check if a method has an annotation
     * @return Map with event class and methods for this listener
     */
    private Map<Class<StemEvent>, Method> findHandlers(Object listener) {
        Map<Class<StemEvent>, Method> methods = new HashMap<>();

        for (Method m : listener.getClass().getDeclaredMethods()) {
            StemEventHandler annotation = m.getAnnotation(StemEventHandler.class);
            if (annotation != null) {
                Class<?>[] params = m.getParameterTypes();
                if (params.length != 1) {
                    STEMSystemApp.LOGGER.ERROR("Method " + m + " in class " + listener.getClass() + " annotated with " + annotation + " does not have single argument");
                    continue;
                }
                Class<StemEvent> iEvent = (Class<StemEvent>) params[0];
                methods.put(iEvent, m);
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
        Map<Class<StemEvent>, Method> handler = findHandlers(classInstance);
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
        for (Object classInstance : this.activeListener.keySet()) {
            Map<Class<StemEvent>, Method> handler = this.activeListener.get(classInstance);
            for (Class<StemEvent> stemEventClass : handler.keySet()) {
                if (stemEventClass.equals(event.getClass())) {
                    /* do some stuff */
                    callMethod(event, handler.get(stemEventClass), classInstance);
                }
            }
        }
    }
}
