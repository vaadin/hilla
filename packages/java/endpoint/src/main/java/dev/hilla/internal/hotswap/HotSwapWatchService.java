/*
 * Copyright 2000-2023 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package dev.hilla.internal.hotswap;

import com.vaadin.flow.server.ExecutionFailedException;
import com.vaadin.flow.shared.Registration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.Executors;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.*;

final class HotSwapWatchService {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(HotSwapWatchService.class);

    private final WatchService watcher;

    private final Map<WatchKey, Path> keys;

    private boolean trace;

    private Path classesDir;

    private final Collection<HotSwapListener> hotSwapListeners = new ArrayList<>();

    HotSwapWatchService() {

        this.keys = new HashMap<>();
        try {
            this.watcher = FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Register the given directory, and all its sub-directories, with the
     * WatchService.
     */
    private void registerAll(final Path start) throws IOException {

        Files.walkFileTree(start, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir,
                    BasicFileAttributes attrs) throws IOException {
                register(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * Register the given directory with the WatchService
     */
    private void register(Path dir) throws IOException {
        WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE,
                ENTRY_MODIFY);
        if (trace) {
            Path prev = keys.get(key);
            if (prev == null) {
                debug("registering: %s %n", dir);
            } else {
                if (!dir.equals(prev)) {
                    debug("update: %s -> %s\n", prev, dir);
                }
            }
        }
        keys.put(key, dir);
    }

    @SuppressWarnings("unchecked")
    private static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>) event;
    }

    /**
     * Process all events for keys queued to the watcher
     */
    void watch(Path classesDir) {

        this.classesDir = classesDir;
        try {
            registerAll(classesDir);
            // enable trace after initial registration
            trace = true;
        } catch (IOException e) {
            error(String.format("Could not register the Watcher Service to %s",
                    classesDir.toString()));
            throw new RuntimeException(e);
        }

        Executors.newSingleThreadExecutor().execute(getWatchLoopRunnable());
    }

    private Runnable getWatchLoopRunnable() {
        return () -> {
            for (;;) {
                // wait for key to be signalled
                WatchKey key;
                try {
                    key = watcher.take();
                } catch (InterruptedException x) {
                    return;
                }

                Path dir = keys.get(key);
                if (dir == null) {
                    error("WatchKey not recognized!!");
                    continue;
                }

                var changedFiles = new HashSet<String>();

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();

                    if (kind == OVERFLOW) {
                        continue;
                    }

                    // Context for directory entry event is the path of the
                    // entry
                    WatchEvent<Path> ev = cast(event);
                    Path file = ev.context();
                    Path child = dir.resolve(file);

                    // if directory is created, then register it as well as its
                    // sub-directories
                    if (kind == ENTRY_CREATE) {
                        try {
                            if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
                                registerAll(child);
                            }
                        } catch (IOException e) {
                            error(String.format(
                                    "Could not register for the changes to the newly added files in %s"
                                            + " - Please restart the application to make sure all the classes are in sync.",
                                    child));
                            throw new RuntimeException(e);
                        }
                    }

                    String fileName = child.toString();

                    if (fileName.endsWith("openapi.json") || changedFiles
                            .stream().noneMatch(fileName::startsWith)) {
                        debug("%s: %s\n", event.kind().name(), child);
                        changedFiles.add(fileName);
                    }
                }

                try {
                    fireHotSwapEvent(changedFiles);
                } catch (ExecutionFailedException e) {
                    throw new RuntimeException(e);
                }

                // reset key and remove from set if directory no longer
                // accessible
                boolean valid = key.reset();
                if (!valid) {
                    keys.remove(key);

                    // all directories are inaccessible
                    if (keys.isEmpty()) {
                        break;
                    }
                }
            }
        };
    }

    private void fireHotSwapEvent(Collection<String> changedFiles)
            throws ExecutionFailedException {
        if (changedFiles.size() > 0) {
            boolean isOpenApiJsonChanged = changedFiles.stream()
                    .anyMatch(file -> file.endsWith("openapi.json"));
            var hotSwapType = isOpenApiJsonChanged
                    ? HotSwapEvent.Type.OPEN_API_JSON
                    : HotSwapEvent.Type.CLASSES;
            var hotSwapEvent = new HotSwapEvent(hotSwapType, classesDir);
            hotSwapListeners
                    .forEach(listener -> listener.onHotSwapEvent(hotSwapEvent));
        }
    }

    Registration addHotSwapListener(HotSwapListener listener) {
        return Registration.addAndRemove(this.hotSwapListeners, listener);
    }

    private void debug(String format, Object... params) {
        debug(String.format(format, params));
    }

    private void debug(String message) {
        LOGGER.debug("### Hilla classes Watch service > " + message);
    }

    private void error(String message) {
        LOGGER.error("### Hilla classes Watch service > " + message);
    }
}
