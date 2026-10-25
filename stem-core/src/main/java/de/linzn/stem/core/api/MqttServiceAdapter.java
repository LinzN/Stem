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
import de.linzn.stem.api.mqtt.MqttHandler;
import de.linzn.stem.api.mqtt.MqttService;
import de.linzn.stem.api.mqtt.MqttSubscription;
import de.linzn.stem.modules.mqttModule.MqttModule;
import de.linzn.stem.modules.pluginModule.STEMPlugin;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.Objects;

final class MqttServiceAdapter implements MqttService {
    private final STEMApp stemApp;
    private final STEMPlugin owner;

    MqttServiceAdapter(STEMApp stemApp, STEMPlugin owner) {
        this.stemApp = stemApp;
        this.owner = owner;
    }

    private MqttModule module() {
        return this.stemApp.getMqttModule();
    }

    @Override
    public boolean isConnected() {
        return module().isConnected();
    }

    @Override
    public boolean publish(String topic, byte[] payload, int qos, boolean retained) {
        if (qos < 0 || qos > 2) {
            throw new IllegalArgumentException("qos must be 0, 1 or 2");
        }
        MqttMessage message = new MqttMessage(Objects.requireNonNull(payload, "payload"));
        message.setQos(qos);
        message.setRetained(retained);
        return module().publish(Objects.requireNonNull(topic, "topic"), message);
    }

    @Override
    public MqttSubscription subscribe(String topicFilter, MqttHandler handler) {
        Objects.requireNonNull(topicFilter, "topicFilter");
        Objects.requireNonNull(handler, "handler");
        IMqttMessageListener listener = (topic, message) -> handler.onMessage(topic, message.getPayload());
        if (!module().subscribe(topicFilter, listener, owner)) {
            throw new IllegalStateException("MQTT subscription for " + topicFilter + " failed");
        }
        return new MqttSubscription() {
            @Override
            public String getTopic() {
                return topicFilter;
            }

            @Override
            public void unsubscribe() {
                module().unsubscribe(topicFilter, listener);
            }
        };
    }
}
