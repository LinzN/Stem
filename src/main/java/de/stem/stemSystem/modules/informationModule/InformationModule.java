/*
 * Copyright (C) 2021. Niklas Linz - All Rights Reserved
 * You may use, distribute and modify this code under the
 * terms of the LGPLv3 license, which unfortunately won't be
 * written for another century.
 *
 * You should have received a copy of the LGPLv3 license with
 * this file. If not, please write to: niklas.linz@enigmar.de
 *
 */

package de.stem.stemSystem.modules.informationModule;

import de.stem.stemSystem.STEMSystemApp;
import de.stem.stemSystem.modules.AbstractModule;
import de.stem.stemSystem.modules.informationModule.events.InformationEvent;
import de.stem.stemSystem.modules.informationModule.listener.DefaultInformationBlockListener;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;

public class InformationModule extends AbstractModule {
    private final STEMSystemApp stemSystemApp;

    private final HashMap<Long, InformationBlock> allInformationBlocks;
    private final LinkedList<InformationBlock> informationQueue;
    private final LinkedList<InformationBlock> activeInformationBlocks;
    private final LinkedList<InformationBlock> archivedInformationBlocks;
    private boolean moduleAlive;

    private AiTextEngine aiTextEngine;

    private long notificationCount;


    public InformationModule(STEMSystemApp stemSystemApp) {
        this.stemSystemApp = stemSystemApp;
        this.allInformationBlocks = new HashMap<>();
        this.informationQueue = new LinkedList<>();
        this.activeInformationBlocks = new LinkedList<>();
        this.archivedInformationBlocks = new LinkedList<>();
        this.notificationCount = 1;
        this.moduleAlive = true;
        this.stemSystemApp.getEventModule().getStemEventBus().register(new DefaultInformationBlockListener());
        startNotificationModule();
    }


    private void startNotificationModule() {
        this.stemSystemApp.getScheduler().runTask(this.getModulePlugin(), this::run);
    }

    public void stopNotificationModule() {
        this.moduleAlive = false;
    }

    @Override
    public void onShutdown() {
        this.stopNotificationModule();
    }

    private void run() {
        moduleAlive = true;
        while (moduleAlive) {
            if (!informationQueue.isEmpty()) {
                InformationBlock informationBlock = informationQueue.removeFirst();

                if (informationBlock != null) {
                    activeInformationBlocks.addLast(informationBlock);
                    InformationEvent informationEvent = new InformationEvent(informationBlock);
                    this.stemSystemApp.getEventModule().getStemEventBus().fireEvent(informationEvent);
                }
            }

            if (!activeInformationBlocks.isEmpty()) {
                InformationBlock activeInformationBlock = activeInformationBlocks.removeFirst();

                if (activeInformationBlock.isActive()) {
                    this.activeInformationBlocks.addLast(activeInformationBlock);
                } else {
                    this.archivedInformationBlocks.addLast(activeInformationBlock);
                }
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException ignored) {
            }
        }
    }


    public long queueInformationBlock(InformationBlock informationBlock) {
        long notificationId = notificationCount++;
        informationBlock.setId(notificationId);
        this.allInformationBlocks.put(notificationId, informationBlock);
        this.informationQueue.add(informationBlock);
        return notificationId;
    }

    public ArrayList<InformationBlock> getActiveInformationBlocks() {
        ArrayList<InformationBlock> list = new ArrayList<>(this.activeInformationBlocks);
        list.sort(new BlockComparator());
        return list;
    }

    public ArrayList<InformationBlock> getArchivedInformationBlocks() {
        ArrayList<InformationBlock> list = new ArrayList<>(this.archivedInformationBlocks);
        list.sort(new BlockComparator());
        return list;
    }

    public InformationBlock getInformationBlockById(long id) {
        return this.allInformationBlocks.get(id);
    }

    public void registerAiTextEngine(AiTextEngine aiTextEngine){
        this.aiTextEngine = aiTextEngine;
    }

    public String runAiTextEngine(String input){
        if(this.aiTextEngine != null){
            return this.aiTextEngine.aiResponse(input);
        }
        return input;
    }

    private static class BlockComparator implements Comparator<InformationBlock> {

        @Override
        public int compare(InformationBlock o1, InformationBlock o2) {
            Long io1 = o1.getId();
            Long io2 = o2.getId();
            return io2.compareTo(io1);
        }
    }
}
