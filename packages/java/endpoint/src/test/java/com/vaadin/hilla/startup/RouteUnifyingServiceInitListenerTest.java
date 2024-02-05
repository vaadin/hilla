package com.vaadin.hilla.startup;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.hilla.route.RouteUnifyingIndexHtmlRequestListener;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class RouteUnifyingServiceInitListenerTest {

    private RouteUnifyingServiceInitListener routeUnifyingServiceInitListener;
    private ServiceInitEvent event;

    @Before
    public void setup() {
        routeUnifyingServiceInitListener = new RouteUnifyingServiceInitListener(
                Mockito.mock(RouteUnifyingIndexHtmlRequestListener.class));
        event = new ServiceInitEvent(Mockito.mock(VaadinService.class));
    }

    @Test
    public void should_addRouteIndexHtmlRequestListener() {
        Assert.assertFalse("Unexpected RouteIndexHtmlRequestListener added",
                eventHasAddedRouteIndexHtmlRequestListener(event));
        routeUnifyingServiceInitListener.serviceInit(event);
        Assert.assertTrue(
                "Expected event to have RouteIndexHtmlRequestListener added",
                eventHasAddedRouteIndexHtmlRequestListener(event));
    }

    private boolean eventHasAddedRouteIndexHtmlRequestListener(
            ServiceInitEvent event) {
        return event.getAddedIndexHtmlRequestListeners().anyMatch(
                indexHtmlRequestListener -> indexHtmlRequestListener instanceof RouteUnifyingIndexHtmlRequestListener);
    }
}
