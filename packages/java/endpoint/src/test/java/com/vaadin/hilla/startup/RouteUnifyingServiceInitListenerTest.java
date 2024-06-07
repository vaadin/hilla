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
import com.vaadin.hilla.route.RouteUnifyingIndexHtmlRequestListener;

public class RouteUnifyingServiceInitListenerTest {

    private RouteUnifyingServiceInitListener routeUnifyingServiceInitListener;
    private ServiceInitEvent event;
    private DeploymentConfiguration mockDeploymentConfiguration;
    private final RouteUnifyingConfigurationProperties routeUnifyingConfigurationProperties = new RouteUnifyingConfigurationProperties();

    @Rule
    public TemporaryFolder projectRoot = new TemporaryFolder();

    @Before
    public void setup() throws IOException {
        routeUnifyingConfigurationProperties
                .setExposeServerRoutesToClient(true);
        routeUnifyingServiceInitListener = new RouteUnifyingServiceInitListener(
                routeUnifyingConfigurationProperties, null, null);
        VaadinService mockVaadinService = Mockito.mock(VaadinService.class);
        mockDeploymentConfiguration = Mockito
                .mock(DeploymentConfiguration.class);
        Mockito.when(mockVaadinService.getDeploymentConfiguration())
                .thenReturn(mockDeploymentConfiguration);
        event = new ServiceInitEvent(mockVaadinService);

        var frontendGeneratedDir = projectRoot.newFolder("frontend/generated");
        Mockito.when(mockDeploymentConfiguration.getFrontendFolder())
                .thenReturn(frontendGeneratedDir.getParentFile());
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

    @Test
    public void should_registerClientRoutes_when_in_prodMode_and_react_is_enabled() {
        Mockito.when(mockDeploymentConfiguration.isReactEnabled())
                .thenReturn(true);
        Mockito.when(mockDeploymentConfiguration.isProductionMode())
                .thenReturn(true);

        routeUnifyingServiceInitListener.serviceInit(event);
        Assert.assertTrue(
                "RouteUnifyingIndexHtmlRequestListener was not registered",
                event.getAddedIndexHtmlRequestListeners().anyMatch(
                        indexHtmlRequestListener -> indexHtmlRequestListener instanceof RouteUnifyingIndexHtmlRequestListener));
    }

    @Test
    public void should_registerClientRoutes_when_in_devMode_and_react_is_enabled() {
        Mockito.when(mockDeploymentConfiguration.isReactEnabled())
                .thenReturn(true);
        Mockito.when(mockDeploymentConfiguration.isProductionMode())
                .thenReturn(false);
        routeUnifyingServiceInitListener.serviceInit(event);
        Assert.assertTrue(
                "RouteUnifyingIndexHtmlRequestListener was not registered",
                event.getAddedIndexHtmlRequestListeners().anyMatch(
                        indexHtmlRequestListener -> indexHtmlRequestListener instanceof RouteUnifyingIndexHtmlRequestListener));
    }

    private boolean hasRouteUnifyingIndexHtmlRequestListenerAdded(
            ServiceInitEvent event) {
        return event.getAddedIndexHtmlRequestListeners().anyMatch(
                indexHtmlRequestListener -> indexHtmlRequestListener instanceof RouteUnifyingIndexHtmlRequestListener);
    }
}
