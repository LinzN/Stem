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


public interface StemEvent {
    default boolean isCanceled() {
        return false;
    }

    default void cancel() {
        throw new IllegalArgumentException("Event cancel not supported");
    }

    default boolean isCancelable() {
        return false;
    }

    default String getName() {
        return this.getClass().getSimpleName();
    }
}
