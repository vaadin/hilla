package com.vaadin.hilla.route;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.RouteParameterData;
import com.vaadin.flow.server.RouteRegistry;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.communication.IndexHtmlRequestListener;
import com.vaadin.flow.server.communication.IndexHtmlResponse;
import com.vaadin.hilla.route.records.ClientViewConfig;
import org.jsoup.nodes.DataNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Index HTML request listener for extracting server-side and client-side views
 * and in dev mode adding them to unified index.html.
 */
@Component
public class RouteUnifyingIndexHtmlRequestListener
        implements IndexHtmlRequestListener {
    static final String SCRIPT_STRING = """
            window.Vaadin = window.Vaadin || {};
            window.Vaadin.views = %s;
            """;
    private static final Logger LOGGER = LoggerFactory
            .getLogger(RouteUnifyingIndexHtmlRequestListener.class);
    private final ObjectMapper mapper = new ObjectMapper();
    private final ClientRouteRegistry clientRouteRegistry;

    /**
     * Default constructor for autowiring the client route registry.
     *
     * @param clientRouteRegistry
     *            - registry of client side routes
     */
    @Autowired
    public RouteUnifyingIndexHtmlRequestListener(
            ClientRouteRegistry clientRouteRegistry) {
        this.clientRouteRegistry = clientRouteRegistry;
    }

    @Override
    public void modifyIndexHtmlResponse(IndexHtmlResponse response) {
        final List<AvailableView> availableViews = new ArrayList<>();
        getClientViews(availableViews);
        extractServerViews(availableViews);

        if (isDevMode()) {
            if (availableViews.isEmpty()) {
                return;
            }

            try {
                final String viewsJson = mapper
                        .writeValueAsString(availableViews);
                final String script = SCRIPT_STRING.formatted(viewsJson);
                response.getDocument().head().appendElement("script")
                        .appendChild(new DataNode(script));
            } catch (IOException e) {
                LOGGER.warn("Failed to write views to dev mode", e);
            }
        }
    }

    private void getClientViews(List<AvailableView> availableViews) {
        final List<ClientViewConfig> allRoutes = clientRouteRegistry
                .getAllRoutes();
        allRoutes.forEach(route -> availableViews
                .add(new AvailableView(route.route(), true, route.title(),
                        route.hasMandatoryParams(), route.getOther())));
    }

    protected void extractServerViews(
            final List<AvailableView> availableViews) {
        final RouteRegistry registry = VaadinService.getCurrent().getRouter()
                .getRegistry();
        registry.getRegisteredRoutes().forEach(serverView -> {
            final Class<? extends com.vaadin.flow.component.Component> viewClass = serverView
                    .getNavigationTarget();
            boolean hasMandatoryParam = !isParametersOptional(
                    serverView.getRouteParameters());
            final String targetUrl = serverView.getTemplate();
            if (targetUrl != null) {
                final String url = "/" + targetUrl;

                final String title;
                PageTitle pageTitle = viewClass.getAnnotation(PageTitle.class);
                if (pageTitle != null) {
                    title = pageTitle.value();
                } else {
                    title = serverView.getNavigationTarget().getSimpleName();
                }

                availableViews.add(new AvailableView(url, false, title,
                        hasMandatoryParam, Map.of()));
            }
        });
    }

    private boolean isParametersOptional(
            Map<String, RouteParameterData> routeParameters) {
        if (routeParameters == null || routeParameters.isEmpty()) {
            return true;
        }
        return routeParameters.values().stream()
                .allMatch(params -> params.getTemplate().endsWith("?")
                        || params.getTemplate().endsWith("*"));
    }

    private boolean isDevMode() {
        VaadinService vaadinService = VaadinService.getCurrent();
        return (vaadinService != null && !vaadinService
                .getDeploymentConfiguration().isProductionMode());
    }

    protected record AvailableView(String route, boolean clientSide, String title, boolean hasMandatoryParam,
                                   Map<String, Object> other) {
        public AvailableView {
            if (other == null) {
                other = new HashMap<>();
            }
        }
    }

}
