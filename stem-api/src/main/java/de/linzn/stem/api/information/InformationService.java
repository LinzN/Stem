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

package de.linzn.stem.api.information;

import de.linzn.stem.modules.informationModule.AiTextEngine;
import de.linzn.stem.modules.informationModule.InformationBlock;

import java.util.List;
import java.util.Optional;

/**
 * Information blocks are status messages shown on displays and in clients.
 */
public interface InformationService {

    /**
     * Queue a block for display.
     *
     * @return id assigned to the block
     */
    long queue(InformationBlock block);

    /**
     * @return active blocks, newest first
     */
    List<InformationBlock> getActive();

    /**
     * @return archived (expired) blocks, newest first
     */
    List<InformationBlock> getArchived();

    /**
     * @return block with the given id, if still known
     */
    Optional<InformationBlock> get(long id);

    /**
     * Register the AI text engine used to generate long descriptions. Replaces any existing engine.
     */
    void registerAiTextEngine(AiTextEngine engine);

    /**
     * Generate text with the registered AI text engine. Returns the input if no engine is available or it fails.
     */
    String generateText(String input);
}
