package com.vaadin.hilla.route;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.server.RouteRegistry;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.communication.IndexHtmlRequestListener;
import com.vaadin.flow.server.communication.IndexHtmlResponse;
import com.vaadin.hilla.route.records.ViewConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Index HTML request listener for extracting server-side and client-side views
 * and in dev mode adding them to unified index.html.
 */
@Component
public class RouteUnifyingIndexHtmlRequestListener
        implements IndexHtmlRequestListener {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(RouteUnifyingIndexHtmlRequestListener.class);
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void modifyIndexHtmlResponse(IndexHtmlResponse response) {
        final List<AvailableView> availableViews = new ArrayList<>();
        extractClientViews(availableViews);
        extractServerViews(availableViews);

        if (isDevMode()) {
            if (availableViews.isEmpty()) {
                return;
            }

            try {
                final String viewsJson = mapper
                        .writeValueAsString(availableViews);
                response.getDocument().head().appendElement("script")
                        .text("window.Vaadin.views = " + viewsJson);
            } catch (IOException e) {
                LOGGER.warn("Failed to write views to dev mode", e);
            }
        }
    }

    protected void extractServerViews(
            final List<AvailableView> availableViews) {
        final RouteRegistry registry = VaadinService.getCurrent().getRouter()
                .getRegistry();
        registry.getRegisteredRoutes().forEach(serverView -> {
            final Class<? extends com.vaadin.flow.component.Component> viewClass = serverView
                    .getNavigationTarget();
            try {
                final Optional<String> targetUrl = registry
                        .getTargetUrl(viewClass);
                if (targetUrl.isPresent()) {
                    final String url = "/" + targetUrl.get();

                    final String title;
                    PageTitle pageTitle = viewClass
                            .getAnnotation(PageTitle.class);
                    if (pageTitle != null) {
                        title = pageTitle.value();
                    } else {
                        title = serverView.getNavigationTarget()
                                .getSimpleName();
                    }

                    availableViews.add(
                            new AvailableView(url, false, title, Map.of()));
                }
            } catch (IllegalArgumentException e) {
                LOGGER.debug("Only supporting Flow views without parameters",
                        e);
            }
        });
    }

    protected void extractClientViews(
            final List<AvailableView> availableViews) {
        try {
            final URL source = getClass()
                    .getResource("/META-INF/VAADIN/views.json");
            Map<String, ViewConfig> clientViews = new HashMap<>();
            if (source != null) {
                clientViews = mapper.readValue(source, new TypeReference<>() {
                });
            }

            clientViews.forEach((route, clientView) -> {
                String title = clientView.title();
                if (title.isBlank()) {
                    title = clientView.route();
                }

                availableViews.add(new AvailableView(route, true, title,
                        clientView.other()));
            });
        } catch (IOException e) {
            LOGGER.warn("Failed extract client views from views.json", e);
        }
    }

    private boolean isDevMode() {
        VaadinService vaadinService = VaadinService.getCurrent();
        return (vaadinService != null && !vaadinService
                .getDeploymentConfiguration().isProductionMode());
    }

    protected record AvailableView(String route, boolean clientSide, String title,
                                   Map<String, Object> other) {
        public AvailableView {
            if (other == null) {
                other = new HashMap<>();
            }
        }
    }

}
