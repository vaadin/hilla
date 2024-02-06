package com.vaadin.hilla.route;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.RouteData;
import com.vaadin.flow.router.Router;
import com.vaadin.flow.server.RouteRegistry;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.communication.IndexHtmlResponse;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class RouteUnifyingIndexHtmlRequestListenerTest {

    private RouteUnifyingIndexHtmlRequestListener routeUnifyingIndexHtmlRequestListener;
    private IndexHtmlResponse indexHtmlResponse;

    private VaadinService vaadinService;

    @Before
    public void setUp() {
        routeUnifyingIndexHtmlRequestListener = new RouteUnifyingIndexHtmlRequestListener();

        final Document document = Mockito.mock(Document.class);
        final Element element = new Element("head");
        Mockito.when(document.head()).thenReturn(element);
        indexHtmlResponse = Mockito.mock(IndexHtmlResponse.class);
        Mockito.when(indexHtmlResponse.getDocument()).thenReturn(document);

        final List<RouteData> flowRegisteredRoutes = new ArrayList<>();
        final RouteData bar = new RouteData(Collections.emptyList(), "bar",
                Collections.emptyList(), Component.class,
                Collections.emptyList());
        flowRegisteredRoutes.add(bar);
        final RouteData foo = new RouteData(Collections.emptyList(), "foo",
                Collections.emptyList(), RouteTarget.class,
                Collections.emptyList());
        flowRegisteredRoutes.add(foo);

        final RouteRegistry registry = Mockito.mock(RouteRegistry.class);
        Mockito.when(registry.getRegisteredRoutes())
                .thenReturn(flowRegisteredRoutes);
        Mockito.when(registry.getTargetUrl(Mockito.any()))
                .thenReturn(Optional.of("foo"));

        vaadinService = Mockito.mock(VaadinService.class);
        final Router router = Mockito.mock(Router.class);
        Mockito.when(vaadinService.getRouter()).thenReturn(router);
        Mockito.when(router.getRegistry()).thenReturn(registry);

        // Mock developer mode
        final DeploymentConfiguration deploymentConfiguration = Mockito
                .mock(DeploymentConfiguration.class);
        Mockito.when(vaadinService.getDeploymentConfiguration())
                .thenReturn(deploymentConfiguration);
        Mockito.when(deploymentConfiguration.isProductionMode())
                .thenReturn(false);
    }

    @Test
    public void modifyIndexHtmlResponse() {

        try (MockedStatic<VaadinService> mocked = Mockito
                .mockStatic(VaadinService.class)) {
            mocked.when(VaadinService::getCurrent).thenReturn(vaadinService);

            routeUnifyingIndexHtmlRequestListener
                    .modifyIndexHtmlResponse(indexHtmlResponse);
        }
        Mockito.verify(indexHtmlResponse, Mockito.times(1)).getDocument();
        MatcherAssert.assertThat(
                indexHtmlResponse.getDocument().head().select("script"),
                Matchers.notNullValue());

        final String scriptText = indexHtmlResponse.getDocument().head()
                .select("script").text();
        MatcherAssert.assertThat(scriptText,
                Matchers.startsWith("window.Vaadin.views = "));

        final String views = scriptText
                .substring("window.Vaadin.views = ".length());
        List<RouteUnifyingIndexHtmlRequestListener.AvailableView> viewsList;
        try {
            viewsList = new ObjectMapper().readValue(views,
                    new TypeReference<>() {
                    });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        MatcherAssert.assertThat(viewsList, Matchers.hasSize(8));
        MatcherAssert.assertThat(viewsList.get(0).title(),
                Matchers.is("About"));
        MatcherAssert.assertThat(viewsList.get(0).clientSide(),
                Matchers.is(true));
        MatcherAssert.assertThat(viewsList.get(7).title(),
                Matchers.is("RouteTarget"));
        MatcherAssert.assertThat(viewsList.get(7).clientSide(),
                Matchers.is(false));
    }

    @Test
    public void extractServerViews() {
        final List<RouteUnifyingIndexHtmlRequestListener.AvailableView> availableViews = new ArrayList<>();

        try (MockedStatic<VaadinService> mocked = Mockito
                .mockStatic(VaadinService.class)) {
            mocked.when(VaadinService::getCurrent).thenReturn(vaadinService);

            routeUnifyingIndexHtmlRequestListener
                    .extractServerViews(availableViews);
        }
        MatcherAssert.assertThat(availableViews, Matchers.hasSize(2));
        MatcherAssert.assertThat(availableViews.get(0).title(),
                Matchers.is("Component"));
        MatcherAssert.assertThat(availableViews.get(1).title(),
                Matchers.is("RouteTarget"));
        MatcherAssert.assertThat(availableViews.get(0).route(),
                Matchers.is("/foo"));

    }

    @Test
    public void extractClientViews() {
        final List<RouteUnifyingIndexHtmlRequestListener.AvailableView> availableViews = new ArrayList<>();
        routeUnifyingIndexHtmlRequestListener
                .extractClientViews(availableViews);

        MatcherAssert.assertThat(availableViews, Matchers.hasSize(6));
        MatcherAssert.assertThat(availableViews.get(0).title(),
                Matchers.is("About"));
        MatcherAssert.assertThat(availableViews.get(5).other().get("unknown"),
                Matchers.notNullValue());
    }

    @PageTitle("RouteTarget")
    private class RouteTarget extends Component {
    }
}
