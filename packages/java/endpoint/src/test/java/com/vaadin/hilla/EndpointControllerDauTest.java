package com.vaadin.hilla;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.server.Constants;
import com.vaadin.flow.server.VaadinContext;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinRequestInterceptor;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinServletService;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.dau.DauEnforcementException;
import com.vaadin.flow.server.dau.EnforcementNotificationMessages;
import com.vaadin.hilla.auth.CsrfChecker;
import com.vaadin.hilla.parser.jackson.JacksonObjectMapperFactory;
import com.vaadin.pro.licensechecker.dau.EnforcementException;

import static com.vaadin.flow.server.dau.DAUUtils.ENFORCEMENT_EXCEPTION_KEY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

/**
 * Ensures that DAU tracking and enforcement is applied in Hilla, by calling
 * Flow start/end request hooks.
 */
public class EndpointControllerDauTest {

    EndpointController controller;

    @Before
    public void setUp() {
        ServletContext servletContext = Mockito.mock(ServletContext.class);
        CsrfChecker csrfChecker = new CsrfChecker(servletContext);
        csrfChecker.setCsrfProtection(false);
        EndpointRegistry endpointRegistry = new EndpointRegistry(
                new EndpointNameChecker());
        ApplicationContext appCtx = Mockito.mock(ApplicationContext.class);
        EndpointInvoker endpointInvoker = new EndpointInvoker(appCtx,
                new JacksonObjectMapperFactory.Json(),
                new ExplicitNullableTypeChecker(), servletContext,
                endpointRegistry);
        controller = new EndpointController(appCtx, endpointRegistry,
                endpointInvoker, csrfChecker);
    }

    @Test
    public void serveEndpoint_vaadinRequestStartEndHooksInvoked() {
        MockVaadinService vaadinService = new MockVaadinService();
        when(vaadinService.getDeploymentConfiguration().isProductionMode())
                .thenReturn(true);
        when(vaadinService.getDeploymentConfiguration()
                .getBooleanProperty(Constants.DAU_TOKEN, false))
                .thenReturn(true);
        controller.vaadinService = vaadinService;

        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        when(request.getHeader("X-CSRF-Token")).thenReturn("Vaadin Fusion");
        controller.serveEndpoint("TEST", "test", null, request, response);

        Mockito.verify(vaadinService.testInterceptor).requestStart(
                any(VaadinRequest.class), any(VaadinResponse.class));
        Mockito.verify(vaadinService.testInterceptor)
                .requestEnd(any(VaadinRequest.class), isNull(), isNull());
    }

    @Test
    public void serveEndpoint_dauEnforcement_serviceUnavailableResponse()
            throws JsonProcessingException {
        MockVaadinService vaadinService = new MockVaadinService();
        when(vaadinService.getDeploymentConfiguration().isProductionMode())
                .thenReturn(true);
        when(vaadinService.getDeploymentConfiguration()
                .getBooleanProperty(Constants.DAU_TOKEN, false))
                .thenReturn(true);
        controller.vaadinService = vaadinService;

        Map<String, Object> attributes = new HashMap<>();
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        when(request.getHeader("X-CSRF-Token")).thenReturn("Vaadin Fusion");
        doAnswer(i -> attributes.put(i.getArgument(0), i.getArgument(1)))
                .when(request).setAttribute(anyString(), any());
        when(request.getAttribute(anyString()))
                .then(i -> attributes.get(i.<String> getArgument(0)));

        Mockito.doAnswer(i -> {
            request.setAttribute(ENFORCEMENT_EXCEPTION_KEY,
                    new EnforcementException("STOP"));
            return null;
        }).when(vaadinService.testInterceptor).requestStart(
                any(VaadinRequest.class), any(VaadinResponse.class));

        ResponseEntity<String> responseEntity = controller.serveEndpoint("TEST",
                "test", null, request, response);

        Mockito.verify(vaadinService.testInterceptor).requestStart(
                any(VaadinRequest.class), any(VaadinResponse.class));
        Mockito.verify(vaadinService.testInterceptor)
                .requestEnd(any(VaadinRequest.class), isNull(), isNull());

        Assert.assertEquals("Expected 503 response for blocked request",
                HttpStatus.SERVICE_UNAVAILABLE, responseEntity.getStatusCode());
        ObjectNode jsonNodes = new ObjectMapper()
                .readValue(responseEntity.getBody(), ObjectNode.class);
        EnforcementNotificationMessages expectedError = EnforcementNotificationMessages.DEFAULT;
        assertEquals(DauEnforcementException.class.getName(),
                jsonNodes.get("type").asText());
        assertEquals(expectedError.caption(),
                jsonNodes.get("message").asText());
        ObjectNode errorDetails = (ObjectNode) jsonNodes.get("detail");
        assertEquals(expectedError.caption(),
                errorDetails.get("caption").asText());
        assertEquals(expectedError.message(),
                errorDetails.get("message").asText());
        if (expectedError.details() != null) {
            assertEquals(expectedError.details(),
                    errorDetails.get("details").asText());
        } else {
            assertTrue(errorDetails.get("details").isNull());
        }
        if (expectedError.url() != null) {
            assertEquals(expectedError.details(),
                    errorDetails.get("url").asText());
        } else {
            assertTrue(errorDetails.get("url").isNull());
        }
    }

    private static class MockVaadinService extends VaadinServletService {

        private final VaadinRequestInterceptor testInterceptor = Mockito
                .mock(VaadinRequestInterceptor.class);
        private final VaadinContext vaadinContext = Mockito
                .mock(VaadinContext.class);
        private final DeploymentConfiguration deploymentConfiguration = Mockito
                .mock(DeploymentConfiguration.class);

        @Override
        public void requestStart(VaadinRequest request,
                VaadinResponse response) {
            testInterceptor.requestStart(request, response);
        }

        @Override
        public void requestEnd(VaadinRequest request, VaadinResponse response,
                VaadinSession session) {
            testInterceptor.requestEnd(request, response, session);
        }

        @Override
        public VaadinContext getContext() {
            return vaadinContext;
        }

        @Override
        public DeploymentConfiguration getDeploymentConfiguration() {
            return deploymentConfiguration;
        }
    }
}
