package com.vaadin.hilla.startup;

import java.io.IOException;

import com.vaadin.hilla.route.RouteUnifyingConfigurationProperties;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.hilla.route.ClientRouteRegistry;
import com.vaadin.hilla.route.RouteUnifyingIndexHtmlRequestListener;

public class RouteUnifyingServiceInitListenerTest {

    private RouteUnifyingServiceInitListener routeUnifyingServiceInitListener;
    private ServiceInitEvent event;
    private ClientRouteRegistry clientRouteRegistry;
    private DeploymentConfiguration mockDeploymentConfiguration;
    private final RouteUnifyingConfigurationProperties routeUnifyingConfigurationProperties = new RouteUnifyingConfigurationProperties();

    @Rule
    public TemporaryFolder projectRoot = new TemporaryFolder();

    @Before
    public void setup() throws IOException {
        clientRouteRegistry = new ClientRouteRegistry();
        routeUnifyingConfigurationProperties
                .setExposeServerRoutesToClient(true);
        routeUnifyingServiceInitListener = new RouteUnifyingServiceInitListener(
                clientRouteRegistry, routeUnifyingConfigurationProperties);
        VaadinService mockVaadinService = Mockito.mock(VaadinService.class);
        mockDeploymentConfiguration = Mockito
                .mock(DeploymentConfiguration.class);
        Mockito.when(mockVaadinService.getDeploymentConfiguration())
                .thenReturn(mockDeploymentConfiguration);
        Mockito.when(mockDeploymentConfiguration.isProductionMode())
                .thenReturn(true);
        event = new ServiceInitEvent(mockVaadinService);
    }

    @Test
    public void should_addRouteIndexHtmlRequestListener_when_react_is_enabled() {
        Mockito.when(mockDeploymentConfiguration.isReactEnabled())
                .thenReturn(true);

        Assert.assertFalse("Unexpected RouteUnifyingServiceInitListener added",
                hasRouteUnifyingIndexHtmlRequestListenerAdded(event));
        routeUnifyingServiceInitListener.serviceInit(event);
        Assert.assertTrue(
                "Expected to have RouteUnifyingServiceInitListener added",
                hasRouteUnifyingIndexHtmlRequestListenerAdded(event));
    }

    @Test
    public void should_not_addRouteIndexHtmlRequestListener_when_react_is_not_enabled() {
        Mockito.when(mockDeploymentConfiguration.isReactEnabled())
                .thenReturn(false);

        routeUnifyingServiceInitListener.serviceInit(event);
        Assert.assertFalse(
                "RouteIndexHtmlRequestListener added unexpectedly when React is not enabled",
                hasRouteUnifyingIndexHtmlRequestListenerAdded(event));
    }

    private boolean hasRouteUnifyingIndexHtmlRequestListenerAdded(
            ServiceInitEvent event) {
        return event.getAddedIndexHtmlRequestListeners().anyMatch(
                indexHtmlRequestListener -> indexHtmlRequestListener instanceof RouteUnifyingIndexHtmlRequestListener);
    }
}
