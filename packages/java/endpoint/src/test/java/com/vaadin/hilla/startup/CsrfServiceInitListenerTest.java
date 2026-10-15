/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */

package com.vaadin.hilla.startup;

import java.util.ServiceLoader;
import java.util.stream.StreamSupport;

import com.vaadin.hilla.auth.CsrfIndexHtmlRequestListener;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServiceInitListener;

public class CsrfServiceInitListenerTest {
    private CsrfServiceInitListener csrfServiceInitListener;
    private ServiceInitEvent event;

    @Before
    public void setup() {
        csrfServiceInitListener = new CsrfServiceInitListener();
        event = new ServiceInitEvent(Mockito.mock(VaadinService.class));
    }

    @Test
    public void should_addCsrfIndexHtmlRequestListener() {
        Assert.assertFalse("Unexpected CsrfIndexHtmlRequestListener added",
                eventHasAddedCsrfIndexHtmlRequestListener(event));
        csrfServiceInitListener.serviceInit(event);
        Assert.assertTrue(
                "Expected event to have CsrfIndexHtmlRequestListener added",
                eventHasAddedCsrfIndexHtmlRequestListener(event));
    }

    @Test
    public void should_beConfiguredForServiceLoader() {
        ServiceLoader<VaadinServiceInitListener> listenersLoader = ServiceLoader
                .load(VaadinServiceInitListener.class);
        Assert.assertTrue(
                "Expected a CsrfServiceInitListener confired as "
                        + "VaadinServiceInitListener service provider",
                StreamSupport.stream(listenersLoader.spliterator(), true)
                        .anyMatch(
                                listener -> listener instanceof CsrfServiceInitListener));
    }

    private boolean eventHasAddedCsrfIndexHtmlRequestListener(
            ServiceInitEvent event) {
        return event.getAddedIndexHtmlRequestListeners().anyMatch(
                indexHtmlRequestListener -> indexHtmlRequestListener instanceof CsrfIndexHtmlRequestListener);
    }
}
