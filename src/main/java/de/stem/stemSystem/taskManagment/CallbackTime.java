/*
 * Copyright (c) 2025 MirraNET, Niklas Linz. All rights reserved.
 *
 * This file is part of the MirraNET project and is licensed under the
 * GNU Lesser General Public License v3.0 (LGPLv3).
 *
 * You may use, distribute and modify this code under the terms
 * of the LGPLv3 license. You should have received a copy of the
 * license along with this file. If not, see <https://www.gnu.org/licenses/lgpl-3.0.html>
 * or contact: niklas.linz@mirranet.de
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
    String cronTask;

    boolean fixedTask;
    boolean isCron;

    public CallbackTime(int delay, int period, TimeUnit timeUnit) {
        this.delay = delay;
        this.period = period;
        this.timeUnit = timeUnit;
        this.fixedTask = false;
        this.isCron = false;
    }

    public CallbackTime(int days, int hours, int minutes, boolean daily) {
        this.days = days;
        this.hours = hours;
        this.minutes = minutes;
        this.daily = daily;
        this.fixedTask = true;
    }

    public CallbackTime(String cronTask){
        this.isCron = true;
        this.cronTask = cronTask;
    }
}
