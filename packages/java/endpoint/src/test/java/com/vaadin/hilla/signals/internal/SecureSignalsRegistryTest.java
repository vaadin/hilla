package com.vaadin.hilla.signals.internal;

import com.vaadin.hilla.AuthenticationUtil;
import com.vaadin.hilla.EndpointInvocationException;
import com.vaadin.hilla.EndpointInvoker;
import com.vaadin.hilla.EndpointRegistry;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import org.mockito.ArgumentMatchers;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SecureSignalsRegistryTest {

    @Test
    public void when_accessToEndpointIsAllowed_signalInstanceIsRegistered()
            throws Exception {
        com.vaadin.signals.Signal<?> signal = Mockito
                .mock(com.vaadin.signals.Signal.class);
        InternalSignal internalSignal = new InternalSignal(signal);
        EndpointInvoker invoker = mockEndpointInvokerThatGrantsAccess(signal);
        AtomicReference<SignalsRegistry> signalsRegistry = new AtomicReference<>();
        try (var dummy = Mockito.mockConstruction(SignalsRegistry.class,
                (mockSignalRegistry, context) -> {
                    when(mockSignalRegistry.get("clientSignalId"))
                            .thenReturn(internalSignal);
                    signalsRegistry.set(mockSignalRegistry);
                })) {
            SecureSignalsRegistry secureSignalsRegistry = new SecureSignalsRegistry(
                    invoker);
            secureSignalsRegistry.register("clientSignalId", "endpoint",
                    "method", null);
            verify(signalsRegistry.get(), times(1)).register(
                    ArgumentMatchers.eq("clientSignalId"),
                    ArgumentMatchers.argThat(arg -> {
                        if (!(arg instanceof InternalSignal))
                            return false;
                        try {
                            var f = arg.getClass().getDeclaredField("signal");
                            f.setAccessible(true);
                            return f.get(arg) == signal;
                        } catch (Exception e) {
                            return false;
                        }
                    }));
        }
    }

    @Test
    public void when_unsubscribedIsCalled_underlyingRegistryRemovesClientSignalToSignalMapping()
            throws Exception {
        com.vaadin.signals.Signal<?> signal = Mockito
                .mock(com.vaadin.signals.Signal.class);
        InternalSignal internalSignal = new InternalSignal(signal);
        EndpointInvoker invoker = mockEndpointInvokerThatGrantsAccess(signal);
        AtomicReference<SignalsRegistry> signalsRegistry = new AtomicReference<>();
        try (var dummy = Mockito.mockConstruction(SignalsRegistry.class,
                (mockSignalRegistry, context) -> {
                    when(mockSignalRegistry.get("clientSignalId"))
                            .thenReturn(internalSignal);
                    signalsRegistry.set(mockSignalRegistry);
                })) {
            SecureSignalsRegistry secureSignalsRegistry = new SecureSignalsRegistry(
                    invoker);
            secureSignalsRegistry.register("clientSignalId", "endpoint",
                    "method", null);
            secureSignalsRegistry.unsubscribe("clientSignalId");
            verify(signalsRegistry.get(), times(1))
                    .removeClientSignalToSignalMapping("clientSignalId");
        }
    }

    @Test
    public void when_accessToEndpointIsRejected_register_throws()
            throws Exception {
        EndpointInvoker invoker = mockEndpointInvokerThatDeniesAccess();
        SecureSignalsRegistry secureSignalsRegistry = new SecureSignalsRegistry(
                invoker);
        assertThrows(
                EndpointInvocationException.EndpointUnauthorizedException.class,
                () -> secureSignalsRegistry.register("clientSignalId",
                        "endpoint", "method", null));
    }

    @Test
    public void when_accessToEndpointIsAllowed_get_returnsSignal()
            throws Exception {
        com.vaadin.signals.Signal<?> signal = Mockito
                .mock(com.vaadin.signals.Signal.class);
        InternalSignal internalSignal = new InternalSignal(signal);
        EndpointInvoker invoker = mockEndpointInvokerThatGrantsAccess(signal);
        AtomicReference<SignalsRegistry> signalsRegistry = new AtomicReference<>();
        try (var dummy = Mockito.mockConstruction(SignalsRegistry.class,
                (mockSignalRegistry, context) -> {
                    when(mockSignalRegistry.get("clientSignalId"))
                            .thenReturn(internalSignal);
                    signalsRegistry.set(mockSignalRegistry);
                })) {
            SecureSignalsRegistry secureSignalsRegistry = new SecureSignalsRegistry(
                    invoker);
            secureSignalsRegistry.register("clientSignalId", "endpoint",
                    "method", null);
            InternalSignal result = (InternalSignal) secureSignalsRegistry
                    .get("clientSignalId");
            assertEquals(internalSignal, result);
            verify(signalsRegistry.get(), times(1)).get("clientSignalId");
        }
    }

    @Test
    public void when_accessToEndpointIsRejected_get_throws() throws Exception {
        EndpointInvoker invoker = mockEndpointInvokerThatDeniesAccess();
        SecureSignalsRegistry secureSignalsRegistry = new SecureSignalsRegistry(
                invoker);
        var endpointMethodsField = secureSignalsRegistry.getClass()
                .getDeclaredField("endpointMethods");
        endpointMethodsField.setAccessible(true);
        var endpointMethods = (Map<String, SecureSignalsRegistry.EndpointMethod>) endpointMethodsField
                .get(secureSignalsRegistry);
        endpointMethods.put("clientSignalId",
                new SecureSignalsRegistry.EndpointMethod("endpoint", "method"));
        assertThrows(
                EndpointInvocationException.EndpointUnauthorizedException.class,
                () -> secureSignalsRegistry.get("clientSignalId"));
    }

    @Test
    public void when_userAuthenticated_throws_forbidden() throws Exception {
        try (var authUtilMock = Mockito.mockStatic(AuthenticationUtil.class)) {
            when(AuthenticationUtil.getSecurityHolderAuthentication())
                    .thenReturn(Mockito.mock(Authentication.class));
            EndpointInvoker invoker = mockEndpointInvokerThatDeniesAccess();
            SecureSignalsRegistry secureSignalsRegistry = new SecureSignalsRegistry(
                    invoker);
            assertThrows(
                    EndpointInvocationException.EndpointForbiddenException.class,
                    () -> secureSignalsRegistry.register("clientSignalId",
                            "endpoint", "method", null));
        }
    }

    private EndpointInvoker mockEndpointInvokerThatGrantsAccess(
            com.vaadin.signals.Signal<?> signal) throws Exception {
        EndpointInvoker invoker = Mockito.mock(EndpointInvoker.class);
        when(invoker.invoke(Mockito.anyString(), Mockito.anyString(),
                Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(signal);
        fakeMethodExistenceOn(invoker);
        return invoker;
    }

    private EndpointInvoker mockEndpointInvokerThatDeniesAccess()
            throws Exception {
        EndpointInvoker invoker = Mockito.mock(EndpointInvoker.class);
        when(invoker.checkAccess(Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.any())).thenReturn("Access denied");
        fakeMethodExistenceOn(invoker);
        return invoker;
    }

    private void fakeMethodExistenceOn(EndpointInvoker invoker)
            throws Exception {
        EndpointRegistry.VaadinEndpointData mockVaadinEndpointData = Mockito
                .mock(EndpointRegistry.VaadinEndpointData.class);
        Method mockMethod = this.getClass().getMethod("aFakeMethod");
        when(mockVaadinEndpointData.getMethod(Mockito.anyString()))
                .thenReturn(Optional.of(mockMethod));
        when(invoker.getVaadinEndpointData(Mockito.anyString()))
                .thenReturn(mockVaadinEndpointData);
    }

    public void aFakeMethod() {
    }
}
