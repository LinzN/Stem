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

package de.linzn.stem.api;

import de.linzn.stem.api.cloud.CloudService;
import de.linzn.stem.api.command.CommandService;
import de.linzn.stem.api.data.DataService;
import de.linzn.stem.api.event.EventService;
import de.linzn.stem.api.health.HealthService;
import de.linzn.stem.api.information.InformationService;
import de.linzn.stem.api.mqtt.MqttService;
import de.linzn.stem.api.notification.NotificationService;
import de.linzn.stem.api.plugin.PluginService;
import de.linzn.stem.api.scheduler.TaskScheduler;
import de.linzn.stem.api.script.ScriptService;
import de.linzn.stem.api.system.SystemService;
import de.linzn.stem.modules.pluginModule.STEMPlugin;

/**
 * The STEM plugin API. Obtain it with {@link STEMPlugin#getStemApi()}.
 * <p>
 * An API instance is bound to one owner plugin. Registrations made through it belong to that plugin and
 * are removed automatically when the plugin is disabled. All services are thread-safe.
 */
public interface StemApi {

    /**
     * @return the plugin this API instance is bound to
     */
    STEMPlugin owner();

    /**
     * @return logger that prefixes every message with the owner's name
     */
    StemLogger logger();

    /**
     * @return task scheduling (async, core thread, delayed, repeating, daily, cron)
     */
    TaskScheduler scheduler();

    /**
     * @return event listener registration and event firing
     */
    EventService events();

    /**
     * @return console command registration
     */
    CommandService commands();

    /**
     * @return MQTT publishing and subscriptions
     */
    MqttService mqtt();

    /**
     * @return notifications pushed to connected clients and the console
     */
    NotificationService notifications();

    /**
     * @return information blocks (status messages shown on displays and in clients)
     */
    InformationService information();

    /**
     * @return system health checks
     */
    HealthService health();

    /**
     * @return key/value JSON storage and raw database access
     */
    DataService data();

    /**
     * @return execution of STEM scripts
     */
    ScriptService scripts();

    /**
     * @return Nextcloud file access
     */
    CloudService cloud();

    /**
     * @return information about loaded plugins
     */
    PluginService plugins();

    /**
     * @return version, uptime and runtime information
     */
    SystemService system();
}
