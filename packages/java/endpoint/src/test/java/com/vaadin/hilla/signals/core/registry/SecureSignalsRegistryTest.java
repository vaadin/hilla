package com.vaadin.hilla.signals.core.registry;

import com.vaadin.hilla.EndpointInvocationException;
import com.vaadin.hilla.EndpointInvoker;
import com.vaadin.hilla.EndpointRegistry;
import com.vaadin.hilla.signals.NumberSignal;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SecureSignalsRegistryTest {

    @Test
    public void when_accessToEndpointIsAllowed_signalInstanceIsRegistered()
            throws Exception {
        NumberSignal signal = new NumberSignal();
        EndpointInvoker invoker = mockEndpointInvokerThatGrantsAccess(signal);

        AtomicReference<SignalsRegistry> signalsRegistry = new AtomicReference<>();
        try (var dummy = Mockito.mockConstruction(SignalsRegistry.class,
                (mockSignalRegistry, context) -> {
                    when(mockSignalRegistry.get("clientSignalId"))
                            .thenReturn(signal);
                    signalsRegistry.set(mockSignalRegistry);
                })) {
            SecureSignalsRegistry secureSignalsRegistry = new SecureSignalsRegistry(
                    invoker);
            secureSignalsRegistry.register("clientSignalId", "endpoint",
                    "method");
            verify(signalsRegistry.get(), times(1)).register("clientSignalId",
                    signal);
        }
    }

    @Test
    public void when_unsubscribedIsCalled_underlyingRegistryRemovesClientSignalToSignalMapping()
            throws Exception {
        NumberSignal signal = new NumberSignal();
        EndpointInvoker invoker = mockEndpointInvokerThatGrantsAccess(signal);

        AtomicReference<SignalsRegistry> signalsRegistry = new AtomicReference<>();
        try (var dummy = Mockito.mockConstruction(SignalsRegistry.class,
                (mockSignalRegistry, context) -> {
                    when(mockSignalRegistry.get("clientSignalId"))
                            .thenReturn(signal);
                    signalsRegistry.set(mockSignalRegistry);
                })) {
            SecureSignalsRegistry secureSignalsRegistry = new SecureSignalsRegistry(
                    invoker);
            secureSignalsRegistry.register("clientSignalId", "endpoint",
                    "method");
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
                EndpointInvocationException.EndpointAccessDeniedException.class,
                () -> secureSignalsRegistry.register("clientSignalId",
                        "endpoint", "method"));
    }

    @Test
    public void when_accessToEndpointIsAllowed_get_returnsSignal()
            throws Exception {
        NumberSignal signal = new NumberSignal();
        EndpointInvoker invoker = mockEndpointInvokerThatGrantsAccess(signal);

        AtomicReference<SignalsRegistry> signalsRegistry = new AtomicReference<>();
        try (var dummy = Mockito.mockConstruction(SignalsRegistry.class,
                (mockSignalRegistry, context) -> {
                    when(mockSignalRegistry.get("clientSignalId"))
                            .thenReturn(signal);
                    signalsRegistry.set(mockSignalRegistry);
                })) {
            SecureSignalsRegistry secureSignalsRegistry = new SecureSignalsRegistry(
                    invoker);
            secureSignalsRegistry.register("clientSignalId", "endpoint",
                    "method");
            NumberSignal result = secureSignalsRegistry.get("clientSignalId");
            assertEquals(signal, result);
            verify(signalsRegistry.get(), times(1)).get("clientSignalId");
        }
    }

    @Test
    public void when_accessToEndpointIsRejected_get_throws() throws Exception {
        EndpointInvoker invoker = mockEndpointInvokerThatDeniesAccess();
        SecureSignalsRegistry secureSignalsRegistry = new SecureSignalsRegistry(
                invoker);
        // fake an existing endpoint method registration in
        // secureSignalsRegistry:
        var endpointMethodsField = secureSignalsRegistry.getClass()
                .getDeclaredField("endpointMethods");
        endpointMethodsField.setAccessible(true);
        var endpointMethods = (Map<String, SecureSignalsRegistry.EndpointMethod>) endpointMethodsField
                .get(secureSignalsRegistry);
        endpointMethods.put("clientSignalId",
                new SecureSignalsRegistry.EndpointMethod("endpoint", "method"));

        assertThrows(
                EndpointInvocationException.EndpointAccessDeniedException.class,
                () -> secureSignalsRegistry.get("clientSignalId"));
    }

    private EndpointInvoker mockEndpointInvokerThatGrantsAccess(
            NumberSignal signal) throws Exception {
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
