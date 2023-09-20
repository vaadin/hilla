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

import com.vaadin.flow.internal.BrowserLiveReload;
import com.vaadin.flow.shared.Registration;

import java.nio.file.Path;

/**
 * The service interface that defines the API for EndpointHotSwapService
 * implementations.
 */
public interface EndpointHotSwapService {

    /**
     * The method that starts the process for monitoring the changes in
     * endpoints. The strategy for detecting the changes can be Polling,
     * Watching the File System changes, etc. No matter what the strategy for
     * detecting the changes is, instances of {@code EndpointChangedEvent}
     * should be fired upon detecting changes in the endpoints and associated
     * classes.
     *
     * @param buildDir
     *            the path to project's build directory which is 'target' in
     *            standard Maven projects and 'build' in standard Gradle
     *            projects.
     * @param browserLiveReload
     *            the BrowserLiveReload object which is used to reload the
     *            browser after any endpoint changes are detected.
     *
     * @see EndpointHotSwapService#addHotSwapListener(HotSwapListener)
     */
    void monitorChanges(Path buildDir, BrowserLiveReload browserLiveReload);

    /**
     * The method that enables registering {@code HotSwapListener}s to be
     * notified whenever an {@code EndpointChangedEvent} is fired.
     *
     * @param listener
     *            an instance of HotSwapListener that implements the
     *            {@code endpointChanged} method that will be called when an
     *            {@code EndpointChangedEvent} is fired.
     * @return the Listener Registration object that enables the cally to
     *         deregister the listener.
     */
    Registration addHotSwapListener(HotSwapListener listener);
}
