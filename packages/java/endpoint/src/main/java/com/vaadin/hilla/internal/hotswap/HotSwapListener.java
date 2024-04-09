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

package com.vaadin.hilla.internal.hotswap;

import com.vaadin.flow.internal.BrowserLiveReload;

import java.nio.file.Path;

/**
 * The interface that defines the contract for HotSwapListeners.
 */
public interface HotSwapListener {

    /**
     * The event object that is passed to {@code endpointChanged} method.
     * @param buildDir
     * @param browserLiveReload
     */
    record EndpointChangedEvent(Path buildDir, BrowserLiveReload browserLiveReload) {
    }

    /**
     * The method that is called when an {@code EndpointChangedEvent} is fired.
     */
    void endpointChanged(EndpointChangedEvent event);

}
