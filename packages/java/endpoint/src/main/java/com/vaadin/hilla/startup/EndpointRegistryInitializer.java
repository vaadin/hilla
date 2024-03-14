package com.vaadin.hilla.startup;

import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.hilla.EndpointController;
import com.vaadin.hilla.engine.EngineConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URL;

@Component
public class EndpointRegistryInitializer implements VaadinServiceInitListener {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(EndpointRegistryInitializer.class);

    private static final String OPEN_API_PROD_RESOURCE_PATH = '/'
            + EngineConfiguration.OPEN_API_PATH;

    private final EndpointController endpointController;

    public EndpointRegistryInitializer(EndpointController endpointController) {
        this.endpointController = endpointController;
    }

    @Override
    public void serviceInit(ServiceInitEvent event) {
        var deploymentConfig = event.getSource().getDeploymentConfiguration();
        var openApiResource = getOpenApiAsResource(deploymentConfig);
        endpointController.registerEndpoints(openApiResource);
    }

    private URL getOpenApiAsResource(DeploymentConfiguration deploymentConfig) {
        if (deploymentConfig.isProductionMode()) {
            return getClass().getResource(OPEN_API_PROD_RESOURCE_PATH);
        }
        var openApiPathInDevMode = deploymentConfig.getProjectFolder().toPath()
                .resolve(deploymentConfig.getBuildFolder())
                .resolve(EngineConfiguration.OPEN_API_PATH);
        try {
            return openApiPathInDevMode.toFile().exists()
                    ? openApiPathInDevMode.toUri().toURL()
                    : null;
        } catch (MalformedURLException e) {
            LOGGER.debug(String.format(
                    "%s Mode: Path %s to resource %s seems to be malformed/could not be parsed. ",
                    deploymentConfig.getMode(), openApiPathInDevMode.toUri(),
                    EngineConfiguration.OPEN_API_PATH), e);
            return null;
        }
    }
}
