/*
 * Copyright 2000-2021 Vaadin Ltd.
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

package com.vaadin.fusion.startup;

import java.util.ServiceLoader;
import java.util.stream.StreamSupport;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.fusion.auth.CsrfIndexHtmlRequestListener;

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