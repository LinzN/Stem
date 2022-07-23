package de.stem.stemSystem.modules.informationModule.events;

import de.stem.stemSystem.modules.eventModule.StemEvent;
import de.stem.stemSystem.modules.informationModule.InformationBlock;

public class InformationEvent implements StemEvent {

    private final InformationBlock informationBlock;

    public InformationEvent(InformationBlock informationBlock){
        this.informationBlock = informationBlock;
    }

    public InformationBlock getInformationBlock() {
        return informationBlock;
    }
}
