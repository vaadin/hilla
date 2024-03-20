package com.vaadin.hilla.startup;

import java.util.Map;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.hilla.route.ClientRouteRegistry;
import com.vaadin.hilla.route.RouteExtractionIndexHtmlRequestListener;
import com.vaadin.hilla.route.records.ClientViewConfig;
import com.vaadin.hilla.route.records.RouteParamType;

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
        Map<String, ClientViewConfig> allRoutes = clientRouteRegistry
                .getAllRoutes();

        MatcherAssert.assertThat(allRoutes, Matchers.aMapWithSize(12));
        MatcherAssert.assertThat(allRoutes.get("/about").getTitle(),
                Matchers.is("About"));
        MatcherAssert.assertThat(allRoutes.get("/profile/friends/list")
                .getOther().get("unknown"), Matchers.notNullValue());
        MatcherAssert.assertThat(
                allRoutes.get("/profile/friends/:user?/edit")
                        .getRouteParameters(),
                Matchers.is(Map.of(":user?", RouteParamType.OPTIONAL)));
        MatcherAssert.assertThat(
                allRoutes.get("/profile/friends/:user").getRouteParameters(),
                Matchers.is(Map.of(":user", RouteParamType.REQUIRED)));
        MatcherAssert.assertThat(
                allRoutes.get("/profile/messages/*").getRouteParameters(),
                Matchers.is(Map.of("wildcard", RouteParamType.WILDCARD)));

    }

    private boolean eventHasAddedRouteIndexHtmlRequestListener(
            ServiceInitEvent event) {
        return event.getAddedIndexHtmlRequestListeners().anyMatch(
                indexHtmlRequestListener -> indexHtmlRequestListener instanceof RouteExtractionIndexHtmlRequestListener);
    }
}
