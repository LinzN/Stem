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

package de.linzn.stem.api.mqtt;

import java.nio.charset.StandardCharsets;

/**
 * MQTT access. Subscriptions are kept across reconnects and may be created before the broker is connected.
 */
public interface MqttService {

    /**
     * @return true if the broker connection is established
     */
    boolean isConnected();

    /**
     * Publish a message.
     *
     * @param qos      quality of service (0, 1 or 2)
     * @param retained retained flag
     * @return true if the message was handed to the broker connection
     */
    boolean publish(String topic, byte[] payload, int qos, boolean retained);

    /**
     * Publish a UTF-8 text message with QoS 1, not retained.
     */
    default boolean publish(String topic, String payload) {
        return publish(topic, payload.getBytes(StandardCharsets.UTF_8), 1, false);
    }

    /**
     * Subscribe to a topic filter for the owner plugin.
     */
    MqttSubscription subscribe(String topicFilter, MqttHandler handler);
}
