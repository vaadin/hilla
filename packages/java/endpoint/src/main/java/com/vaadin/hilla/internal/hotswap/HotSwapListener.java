/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
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
