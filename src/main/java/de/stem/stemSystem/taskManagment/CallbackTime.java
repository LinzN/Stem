/*
 * Copyright (C) 2020. Niklas Linz - All Rights Reserved
 * You may use, distribute and modify this code under the
 * terms of the LGPLv3 license, which unfortunately won't be
 * written for another century.
 *
 * You should have received a copy of the LGPLv3 license with
 * this file. If not, please write to: niklas.linz@enigmar.de
 *
 */

package de.stem.stemSystem.taskManagment;

import java.util.concurrent.TimeUnit;

public class CallbackTime {
    int delay;
    int period;
    TimeUnit timeUnit;

    int days;
    int hours;
    int minutes;
    boolean daily;

    boolean fixedTask;

    public CallbackTime(int delay, int period, TimeUnit timeUnit) {
        this.delay = delay;
        this.period = period;
        this.timeUnit = timeUnit;
        this.fixedTask = false;
    }

    public CallbackTime(int days, int hours, int minutes, boolean daily) {
        this.days = days;
        this.hours = hours;
        this.minutes = minutes;
        this.daily = daily;
        this.fixedTask = true;
    }

}
