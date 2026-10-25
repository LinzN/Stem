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
import de.linzn.stem.api.StemApi;
import de.linzn.stem.api.StemLogger;
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
 * API instance bound to one owner plugin. Services are thin adapters onto the STEM modules and pass the
 * owner along, so every registration can be removed when the plugin is disabled.
 */
final class StemApiImpl implements StemApi {
    private final STEMPlugin owner;
    private final StemLogger logger;
    private final TaskScheduler scheduler;
    private final EventService events;
    private final CommandService commands;
    private final MqttService mqtt;
    private final NotificationService notifications;
    private final InformationService information;
    private final HealthService health;
    private final DataService data;
    private final ScriptService scripts;
    private final CloudService cloud;
    private final PluginService plugins;
    private final SystemService system;

    StemApiImpl(STEMApp stemApp, STEMPlugin owner) {
        this.owner = owner;
        this.logger = new PluginLogger(owner);
        this.scheduler = new TaskSchedulerAdapter(stemApp, owner);
        this.events = new EventServiceAdapter(stemApp, owner);
        this.commands = new CommandServiceAdapter(stemApp, owner);
        this.mqtt = new MqttServiceAdapter(stemApp, owner);
        this.notifications = new NotificationServiceAdapter(stemApp, owner);
        this.information = new InformationServiceAdapter(stemApp, owner);
        this.health = new HealthServiceAdapter(stemApp);
        this.data = new DataServiceAdapter(stemApp);
        this.scripts = new ScriptServiceAdapter(stemApp, owner);
        this.cloud = new CloudServiceAdapter(stemApp);
        this.plugins = new PluginServiceAdapter(stemApp);
        this.system = new SystemServiceAdapter(stemApp);
    }

    @Override
    public STEMPlugin owner() {
        return owner;
    }

    @Override
    public StemLogger logger() {
        return logger;
    }

    @Override
    public TaskScheduler scheduler() {
        return scheduler;
    }

    @Override
    public EventService events() {
        return events;
    }

    @Override
    public CommandService commands() {
        return commands;
    }

    @Override
    public MqttService mqtt() {
        return mqtt;
    }

    @Override
    public NotificationService notifications() {
        return notifications;
    }

    @Override
    public InformationService information() {
        return information;
    }

    @Override
    public HealthService health() {
        return health;
    }

    @Override
    public DataService data() {
        return data;
    }

    @Override
    public ScriptService scripts() {
        return scripts;
    }

    @Override
    public CloudService cloud() {
        return cloud;
    }

    @Override
    public PluginService plugins() {
        return plugins;
    }

    @Override
    public SystemService system() {
        return system;
    }
}
