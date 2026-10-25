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

package de.linzn.stem.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Drains the output of a process on a virtual thread while the process runs.
 * <p>
 * Reading only after waitFor() blocks a process as soon as its pipe buffer (~64 KB) is full. This class
 * reads continuously, closes the stream at the end and keeps at most {@link #MAX_LINES} lines in memory.
 */
public final class ProcessOutput {
    public static final int MAX_LINES = 10_000;

    private final List<String> lines = new ArrayList<>();
    private final Thread readerThread;
    private boolean truncated = false;

    private ProcessOutput(Process process, String name) {
        InputStream inputStream = process.getInputStream();
        this.readerThread = Thread.ofVirtual().name("STEM-ProcessOutput-" + name).start(() -> this.read(inputStream));
    }

    /**
     * Start draining stdout of a process. Start the process with redirectErrorStream(true) to capture stderr too.
     *
     * @param process process to read from
     * @param name    name used for the reader thread
     * @return output collector
     */
    public static ProcessOutput drain(Process process, String name) {
        return new ProcessOutput(process, name);
    }

    /**
     * Close all streams of a process.
     *
     * @param process process to close
     */
    public static void closeStreams(Process process) {
        if (process == null) {
            return;
        }
        closeQuietly(process.getOutputStream());
        closeQuietly(process.getInputStream());
        closeQuietly(process.getErrorStream());
    }

    private static void closeQuietly(AutoCloseable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (Exception ignored) {
        }
    }

    private void read(InputStream inputStream) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                synchronized (this) {
                    if (this.lines.size() < MAX_LINES) {
                        this.lines.add(line);
                    } else {
                        this.truncated = true;
                    }
                }
            }
        } catch (IOException ignored) {
            /* stream closed because the process was destroyed */
        }
    }

    /**
     * Wait until all output has been read.
     *
     * @param timeout maximum time to wait
     * @param unit    time unit
     */
    public void awaitCompletion(long timeout, TimeUnit unit) {
        try {
            this.readerThread.join(unit.toMillis(timeout));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * @return copy of the collected lines
     */
    public synchronized List<String> getLines() {
        List<String> copy = new ArrayList<>(this.lines);
        if (this.truncated) {
            copy.add("[output truncated after " + MAX_LINES + " lines]");
        }
        return copy;
    }
}
