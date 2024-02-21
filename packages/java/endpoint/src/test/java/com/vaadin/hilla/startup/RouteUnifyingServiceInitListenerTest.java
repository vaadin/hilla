package com.vaadin.hilla.startup;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.hilla.route.ClientRouteRegistry;
import com.vaadin.hilla.route.RouteExtractionIndexHtmlRequestListener;
import com.vaadin.hilla.route.records.ClientViewConfig;
import com.vaadin.hilla.route.records.RouteParamType;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;

public class RouteUnifyingServiceInitListenerTest {

    private RouteUnifyingServiceInitListener routeUnifyingServiceInitListener;
    private ServiceInitEvent event;
    private ClientRouteRegistry clientRouteRegistry;

    @Before
    public void setup() {
        clientRouteRegistry = new ClientRouteRegistry();
        routeUnifyingServiceInitListener = new RouteUnifyingServiceInitListener(
                Mockito.mock(RouteExtractionIndexHtmlRequestListener.class),
                clientRouteRegistry);
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

    @Test
    public void should_extractClientViews() {
        routeUnifyingServiceInitListener.registerClientRoutes();
        List<ClientViewConfig> allRoutes = clientRouteRegistry.getAllRoutes();

        MatcherAssert.assertThat(allRoutes, Matchers.hasSize(8));
        MatcherAssert.assertThat(allRoutes.get(0).title(),
                Matchers.is("About"));
        MatcherAssert.assertThat(allRoutes.get(4).other().get("unknown"),
                Matchers.notNullValue());
        MatcherAssert.assertThat(allRoutes.get(5).routeParameters(),
                Matchers.is(Map.of(":user?", RouteParamType.OPTIONAL)));
        MatcherAssert.assertThat(allRoutes.get(6).routeParameters(),
                Matchers.is(Map.of(":user", RouteParamType.REQUIRED)));
        MatcherAssert.assertThat(allRoutes.get(7).routeParameters(),
                Matchers.is(Map.of("wildcard", RouteParamType.WILDCARD)));

    }

    private boolean eventHasAddedRouteIndexHtmlRequestListener(
            ServiceInitEvent event) {
        return event.getAddedIndexHtmlRequestListeners().anyMatch(
                indexHtmlRequestListener -> indexHtmlRequestListener instanceof RouteExtractionIndexHtmlRequestListener);
    }
}
