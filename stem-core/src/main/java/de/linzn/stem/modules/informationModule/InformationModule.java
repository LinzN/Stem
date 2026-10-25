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

package de.linzn.stem.modules.informationModule;

import de.linzn.stem.STEMApp;
import de.linzn.stem.modules.AbstractModule;
import de.linzn.stem.modules.informationModule.events.InformationEvent;
import de.linzn.stem.modules.informationModule.listener.DefaultInformationBlockListener;

import de.linzn.stem.modules.pluginModule.PluginModule;
import de.linzn.stem.modules.pluginModule.STEMPlugin;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class InformationModule extends AbstractModule {
    /* Maximum number of archived (expired) blocks kept in memory */
    private static final int MAX_ARCHIVED_BLOCKS = 500;
    private static final long SWEEP_INTERVAL_MILLIS = 1000;

    private final ConcurrentHashMap<Long, InformationBlock> allInformationBlocks;
    private final LinkedBlockingQueue<InformationBlock> informationQueue;
    /* Guarded by blockLock */
    private final ArrayList<InformationBlock> activeInformationBlocks;
    /* Guarded by blockLock */
    private final ArrayDeque<InformationBlock> archivedInformationBlocks;
    private final Object blockLock = new Object();
    private final AtomicLong notificationCount;
    private volatile boolean moduleAlive;
    private volatile Thread workerThread;
    private volatile AiTextEngine aiTextEngine;
    private volatile STEMPlugin aiTextEngineOwner;

    public InformationModule(STEMApp stemApp) {
        super(stemApp);
        this.allInformationBlocks = new ConcurrentHashMap<>();
        this.informationQueue = new LinkedBlockingQueue<>();
        this.activeInformationBlocks = new ArrayList<>();
        this.archivedInformationBlocks = new ArrayDeque<>();
        this.notificationCount = new AtomicLong(1);
        this.moduleAlive = true;
        this.getStemApp().getEventModule().getStemEventBus().register(new DefaultInformationBlockListener(), this.getModulePlugin());
        startNotificationModule();
    }

    private void startNotificationModule() {
        this.workerThread = Thread.ofVirtual().name("STEM-Information").start(this::run);
    }

    public void stopNotificationModule() {
        this.moduleAlive = false;
        Thread thread = this.workerThread;
        if (thread != null) {
            thread.interrupt();
        }
    }

    @Override
    public void onShutdown() {
        this.stopNotificationModule();
    }

    private void run() {
        long nextSweep = System.currentTimeMillis() + SWEEP_INTERVAL_MILLIS;
        while (moduleAlive) {
            try {
                long wait = Math.max(1, nextSweep - System.currentTimeMillis());
                InformationBlock informationBlock = informationQueue.poll(wait, TimeUnit.MILLISECONDS);
                if (informationBlock != null) {
                    synchronized (this.blockLock) {
                        this.activeInformationBlocks.add(informationBlock);
                    }
                    InformationEvent informationEvent = new InformationEvent(informationBlock);
                    this.getStemApp().getEventModule().getStemEventBus().fireEvent(informationEvent);
                }
            } catch (InterruptedException e) {
                continue;
            } catch (Exception e) {
                STEMApp.LOGGER.ERROR(e);
            }
            if (System.currentTimeMillis() >= nextSweep) {
                this.sweepExpiredBlocks();
                nextSweep = System.currentTimeMillis() + SWEEP_INTERVAL_MILLIS;
            }
        }
    }

    /**
     * Move expired blocks to the archive and drop the oldest archived blocks beyond the limit.
     */
    private void sweepExpiredBlocks() {
        synchronized (this.blockLock) {
            Iterator<InformationBlock> iterator = this.activeInformationBlocks.iterator();
            while (iterator.hasNext()) {
                InformationBlock block = iterator.next();
                if (!block.isActive()) {
                    iterator.remove();
                    this.archivedInformationBlocks.addLast(block);
                }
            }
            while (this.archivedInformationBlocks.size() > MAX_ARCHIVED_BLOCKS) {
                InformationBlock removed = this.archivedInformationBlocks.removeFirst();
                this.allInformationBlocks.remove(removed.getId(), removed);
            }
        }
    }

    public long queueInformationBlock(InformationBlock informationBlock) {
        long notificationId = notificationCount.getAndIncrement();
        informationBlock.setId(notificationId);
        this.allInformationBlocks.put(notificationId, informationBlock);
        this.informationQueue.add(informationBlock);
        return notificationId;
    }

    public ArrayList<InformationBlock> getActiveInformationBlocks() {
        ArrayList<InformationBlock> list;
        synchronized (this.blockLock) {
            list = new ArrayList<>(this.activeInformationBlocks);
        }
        list.removeIf(block -> !block.isActive());
        list.sort(new BlockComparator());
        return list;
    }

    public ArrayList<InformationBlock> getArchivedInformationBlocks() {
        ArrayList<InformationBlock> list;
        synchronized (this.blockLock) {
            list = new ArrayList<>(this.archivedInformationBlocks);
        }
        list.sort(new BlockComparator());
        return list;
    }

    public InformationBlock getInformationBlockById(long id) {
        return this.allInformationBlocks.get(id);
    }

    public void registerAiTextEngine(AiTextEngine aiTextEngine) {
        this.registerAiTextEngine(aiTextEngine, aiTextEngine == null ? null : PluginModule.findOwner(aiTextEngine.getClass()));
    }

    /**
     * Register the AI text engine for a plugin
     *
     * @param aiTextEngine engine to use
     * @param owner        owning plugin, may be null for core engines
     */
    public void registerAiTextEngine(AiTextEngine aiTextEngine, STEMPlugin owner) {
        this.aiTextEngineOwner = aiTextEngine == null ? null : owner;
        this.aiTextEngine = aiTextEngine;
    }

    /**
     * Remove the AI text engine if it belongs to the given plugin
     *
     * @param owner plugin to check
     */
    public void unregisterAiTextEngine(STEMPlugin owner) {
        if (owner != null && this.aiTextEngineOwner == owner) {
            this.aiTextEngine = null;
            this.aiTextEngineOwner = null;
            STEMApp.LOGGER.DEBUG("AiTextEngine unregister from " + owner.getPluginName());
        }
    }

    public String runAiTextEngine(String input) {
        AiTextEngine engine = this.aiTextEngine;
        if (engine != null) {
            try {
                String response = engine.aiResponse(input);
                return response != null ? response : input;
            } catch (Exception e) {
                STEMApp.LOGGER.ERROR("AiTextEngine failed, using plain text");
                STEMApp.LOGGER.ERROR(e);
            }
        }
        return input;
    }

    private static class BlockComparator implements Comparator<InformationBlock> {

        @Override
        public int compare(InformationBlock o1, InformationBlock o2) {
            return Long.compare(o2.getId(), o1.getId());
        }
    }
}
