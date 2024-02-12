package com.vaadin.hilla.route;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.RouteParameterData;
import com.vaadin.flow.server.RouteRegistry;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.communication.IndexHtmlRequestListener;
import com.vaadin.flow.server.communication.IndexHtmlResponse;
import com.vaadin.hilla.route.records.ServerViewInfo;
import org.jsoup.nodes.DataNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Index HTML request listener for extracting server-side views and adding them
 * to index.html response.
 */
@Component
public class RouteExtractionIndexHtmlRequestListener
        implements IndexHtmlRequestListener {
    protected static final String SCRIPT_STRING = "window.Vaadin = window.Vaadin ?? {}; "
            + " window.Vaadin.server = window.Vaadin.server ?? {}; "
            + " window.Vaadin.server.views = %s;";
    private static final Logger LOGGER = LoggerFactory
            .getLogger(RouteExtractionIndexHtmlRequestListener.class);
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void modifyIndexHtmlResponse(IndexHtmlResponse response) {
        final List<ServerViewInfo> serverViews = new ArrayList<>();
        extractServerViews(serverViews);

        if (serverViews.isEmpty()) {
            return;
        }
        try {
            final String viewsJson = mapper.writeValueAsString(serverViews);
            final String script = SCRIPT_STRING.formatted(viewsJson);
            response.getDocument().head().appendElement("script")
                    .appendChild(new DataNode(script));
        } catch (IOException e) {
            LOGGER.warn("Failed to write server views to index response", e);
        }

    }

    protected void extractServerViews(final List<ServerViewInfo> serverViews) {
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

                final ServerViewInfo serverViewInfo = new ServerViewInfo(url,
                        title, hasMandatoryParam);
                serverViews.add(serverViewInfo);
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

}
