/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */

package com.vaadin.hilla.internal.hotswap;

import jakarta.annotation.PostConstruct;

import java.io.IOException;
import java.util.Optional;

import com.vaadin.hilla.EndpointCodeGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.internal.BrowserLiveReload;

class EndpointHotSwapListener implements HotSwapListener {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(EndpointHotSwapListener.class);

    private final EndpointHotSwapService endpointHotSwapService;

    public EndpointHotSwapListener(
            EndpointHotSwapService endpointHotSwapService) {
        this.endpointHotSwapService = endpointHotSwapService;
    }

    @PostConstruct
    private void registerForHotSwapChanges() {
        endpointHotSwapService.addHotSwapListener(this);
    }

    @Override
    public void endpointChanged(EndpointChangedEvent event) {
        try {
            EndpointCodeGenerator.getInstance().update();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        reload(event.browserLiveReload());
    }

    private void reload(BrowserLiveReload browserLiveReload) {

        Optional.ofNullable(browserLiveReload).ifPresent(liveReload -> {
            LOGGER.debug("Reloading the browser after endpoint(s) changes...");
            liveReload.reload();
        });
    }

}
