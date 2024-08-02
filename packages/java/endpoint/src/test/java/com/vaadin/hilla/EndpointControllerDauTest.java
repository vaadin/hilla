package com.vaadin.hilla;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

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

import com.vaadin.flow.server.VaadinContext;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinRequestInterceptor;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinServletService;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.dau.DauEnforcementException;
import com.vaadin.flow.server.dau.EnforcementNotificationMessages;
import com.vaadin.hilla.auth.CsrfChecker;
import com.vaadin.hilla.exception.EndpointValidationException;
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
        controller.vaadinService = vaadinService;

        controller.httpServletResponse = Mockito
                .mock(HttpServletResponse.class);

        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        when(request.getHeader("X-CSRF-Token")).thenReturn("Vaadin Fusion");
        controller.serveEndpoint("TEST", "test", null, request);

        Mockito.verify(vaadinService.testInterceptor).requestStart(
                any(VaadinRequest.class), any(VaadinResponse.class));
        Mockito.verify(vaadinService.testInterceptor)
                .requestEnd(any(VaadinRequest.class), isNull(), isNull());
    }

    @Test
    public void serveEndpoint_dauEnforcement_serviceUnavailableResponse()
            throws JsonProcessingException {
        MockVaadinService vaadinService = new MockVaadinService();
        controller.vaadinService = vaadinService;

        controller.httpServletResponse = Mockito
                .mock(HttpServletResponse.class);

        Map<String, Object> attributes = new HashMap<>();
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
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

        ResponseEntity<String> response = controller.serveEndpoint("TEST",
                "test", null, request);

        Mockito.verify(vaadinService.testInterceptor).requestStart(
                any(VaadinRequest.class), any(VaadinResponse.class));
        Mockito.verify(vaadinService.testInterceptor)
                .requestEnd(any(VaadinRequest.class), isNull(), isNull());

        Assert.assertEquals("Expected 503 response for blocked request",
                HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        ObjectNode jsonNodes = new ObjectMapper().readValue(response.getBody(),
                ObjectNode.class);
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
    }
}
