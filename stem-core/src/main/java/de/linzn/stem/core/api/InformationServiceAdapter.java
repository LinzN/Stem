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
import de.linzn.stem.api.information.InformationService;
import de.linzn.stem.modules.informationModule.AiTextEngine;
import de.linzn.stem.modules.informationModule.InformationBlock;
import de.linzn.stem.modules.informationModule.InformationModule;
import de.linzn.stem.modules.pluginModule.STEMPlugin;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

final class InformationServiceAdapter implements InformationService {
    private final STEMApp stemApp;
    private final STEMPlugin owner;

    InformationServiceAdapter(STEMApp stemApp, STEMPlugin owner) {
        this.stemApp = stemApp;
        this.owner = owner;
    }

    private InformationModule module() {
        return this.stemApp.getInformationModule();
    }

    @Override
    public long queue(InformationBlock block) {
        return module().queueInformationBlock(Objects.requireNonNull(block, "block"));
    }

    @Override
    public List<InformationBlock> getActive() {
        return List.copyOf(module().getActiveInformationBlocks());
    }

    @Override
    public List<InformationBlock> getArchived() {
        return List.copyOf(module().getArchivedInformationBlocks());
    }

    @Override
    public Optional<InformationBlock> get(long id) {
        return Optional.ofNullable(module().getInformationBlockById(id));
    }

    @Override
    public void registerAiTextEngine(AiTextEngine engine) {
        module().registerAiTextEngine(engine, owner);
    }

    @Override
    public String generateText(String input) {
        InformationModule module = module();
        return module == null ? input : module.runAiTextEngine(input);
    }
}
