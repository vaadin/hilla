package com.vaadin.hilla.route;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.RouteData;
import com.vaadin.flow.router.Router;
import com.vaadin.flow.server.RouteRegistry;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.communication.IndexHtmlResponse;
import com.vaadin.hilla.route.records.ServerViewInfo;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.*;

public class RouteExtractionIndexHtmlRequestListenerTest {

    protected static final String SCRIPT_STRING = RouteExtractionIndexHtmlRequestListener.SCRIPT_STRING.replace("%s;", "");

    private final RouteExtractionIndexHtmlRequestListener requestListener
            = new RouteExtractionIndexHtmlRequestListener();
    private IndexHtmlResponse indexHtmlResponse;

    private VaadinService vaadinService;

    @Before
    public void setUp() {
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

        vaadinService = Mockito.mock(VaadinService.class);
        final Router router = Mockito.mock(Router.class);
        Mockito.when(vaadinService.getRouter()).thenReturn(router);
        Mockito.when(router.getRegistry()).thenReturn(registry);
    }

    @Test
    public void should_modifyIndexHtmlResponse() {
        try (MockedStatic<VaadinService> mocked = Mockito
                .mockStatic(VaadinService.class)) {
            mocked.when(VaadinService::getCurrent).thenReturn(vaadinService);

            requestListener.modifyIndexHtmlResponse(indexHtmlResponse);
        }
        Mockito.verify(indexHtmlResponse, Mockito.times(1)).getDocument();
        MatcherAssert.assertThat(
                indexHtmlResponse.getDocument().head().select("script"),
                Matchers.notNullValue());

        DataNode script = indexHtmlResponse.getDocument().head()
                .select("script").dataNodes().get(0);

        final String scriptText = script.getWholeData();
        MatcherAssert.assertThat(scriptText,
                Matchers.startsWith(SCRIPT_STRING));

        final String views = scriptText
                .substring(SCRIPT_STRING.length());
        final List<ServerViewInfo> viewsList;
        try {
            viewsList = new ObjectMapper().readValue(views,
                    new TypeReference<>() {
                    });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        MatcherAssert.assertThat(viewsList, Matchers.hasSize(2));
        MatcherAssert.assertThat(viewsList.get(0).title(),
                Matchers.is("Component"));
        MatcherAssert.assertThat(viewsList.get(1).title(),
                Matchers.is("RouteTarget"));
        MatcherAssert.assertThat(viewsList.get(0).route(),
                Matchers.is("/bar"));
    }

    @Test
    public void should_extractServerViews() {
        final List<ServerViewInfo> viewsList = new ArrayList<>();

        try (MockedStatic<VaadinService> mocked = Mockito
                .mockStatic(VaadinService.class)) {
            mocked.when(VaadinService::getCurrent).thenReturn(vaadinService);

            requestListener
                    .extractServerViews(viewsList);
        }
        MatcherAssert.assertThat(viewsList, Matchers.hasSize(2));
        MatcherAssert.assertThat(viewsList.get(0).title(),
                Matchers.is("Component"));
        MatcherAssert.assertThat(viewsList.get(1).title(),
                Matchers.is("RouteTarget"));
        MatcherAssert.assertThat(viewsList.get(0).route(),
                Matchers.is("/bar"));

    }

    @PageTitle("RouteTarget")
    private static class RouteTarget extends Component {
    }
}
