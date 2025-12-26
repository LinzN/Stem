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

package de.linzn.stem.modules.informationModule.events;

import de.linzn.stem.modules.eventModule.StemEvent;
import de.linzn.stem.modules.informationModule.InformationBlock;

public class InformationEvent implements StemEvent {

    private final InformationBlock informationBlock;

    public InformationEvent(InformationBlock informationBlock) {
        this.informationBlock = informationBlock;
    }

    public InformationBlock getInformationBlock() {
        return informationBlock;
    }
}
