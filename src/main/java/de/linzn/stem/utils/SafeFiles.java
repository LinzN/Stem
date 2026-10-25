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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * File helpers that protect against path traversal and partially written files.
 */
public final class SafeFiles {

    private SafeFiles() {
    }

    /**
     * Resolve a zip entry name inside a target directory and reject entries that would escape it
     * ("zip slip", e.g. "../../etc/passwd").
     *
     * @param targetDir directory the entry is extracted to
     * @param entryName name of the zip entry
     * @return resolved path inside the target directory
     * @throws IOException if the entry points outside the target directory
     */
    public static Path resolveInside(Path targetDir, String entryName) throws IOException {
        Path normalizedTarget = targetDir.toAbsolutePath().normalize();
        Path resolved = normalizedTarget.resolve(entryName).normalize();
        if (!resolved.startsWith(normalizedTarget) || resolved.equals(normalizedTarget)) {
            throw new IOException("Blocked zip entry outside of target directory: " + entryName);
        }
        return resolved;
    }

    /**
     * Extract a zip file into a directory. Entries pointing outside the directory are rejected.
     *
     * @param zipFile   zip file to extract
     * @param targetDir target directory
     * @param listener  called for every extracted file, may be null
     * @throws IOException if reading or writing fails or an entry is unsafe
     */
    public static void unzip(Path zipFile, Path targetDir, ExtractListener listener) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                Path newFile = resolveInside(targetDir, entry.getName());
                Files.createDirectories(newFile.getParent());
                if (listener != null) {
                    listener.onExtract(newFile);
                }
                Files.copy(zis, newFile, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    /**
     * Write a stream to a file atomically: data goes to a temp file in the same directory first and is
     * moved over the destination only when complete. A failed download never leaves a broken file.
     *
     * @param in          source stream
     * @param destination destination file
     * @throws IOException if writing fails
     */
    public static void writeAtomically(InputStream in, Path destination) throws IOException {
        Path directory = destination.toAbsolutePath().getParent();
        Files.createDirectories(directory);
        Path tempFile = Files.createTempFile(directory, destination.getFileName().toString(), ".part");
        try {
            Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
            try {
                Files.move(tempFile, destination, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(tempFile, destination, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @FunctionalInterface
    public interface ExtractListener {
        void onExtract(Path file);
    }
}
