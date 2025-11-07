/*
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.hilla.signals.internal;

import tools.jackson.databind.ObjectMapper;
import com.vaadin.hilla.AuthenticationUtil;
import com.vaadin.hilla.EndpointInvocationException;
import com.vaadin.hilla.EndpointInvoker;
import com.vaadin.hilla.EndpointRegistry;
import com.vaadin.signals.AbstractSignal;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SecureSignalsRegistryTest {

    @Test
    public void when_accessToEndpointIsAllowed_signalInstanceIsRegistered()
            throws Exception {
        AbstractSignal<?> signal = Mockito.mock(AbstractSignal.class);
        InternalSignal internalSignal = new InternalSignal(signal,
                new ObjectMapper());
        EndpointInvoker invoker = mockEndpointInvokerThatGrantsAccess(signal);
        AtomicReference<SignalsRegistry> signalsRegistry = new AtomicReference<>();
        try (var dummy = Mockito.mockConstruction(SignalsRegistry.class,
                (mockSignalRegistry, context) -> {
                    when(mockSignalRegistry.get("clientSignalId"))
                            .thenReturn(internalSignal);
                    signalsRegistry.set(mockSignalRegistry);
                })) {
            SecureSignalsRegistry secureSignalsRegistry = new SecureSignalsRegistry(
                    invoker, new ObjectMapper());
            secureSignalsRegistry.register("clientSignalId", "endpoint",
                    "method", null);
            verify(signalsRegistry.get(), times(1)).register(
                    eq("clientSignalId"),
                    argThat(actualInternalSignal -> actualInternalSignal != null));
        }
    }

    @Test
    public void when_unsubscribedIsCalled_underlyingRegistryRemovesClientSignalToSignalMapping()
            throws Exception {
        AbstractSignal<?> signal = Mockito.mock(AbstractSignal.class);
        InternalSignal internalSignal = new InternalSignal(signal,
                new ObjectMapper());
        EndpointInvoker invoker = mockEndpointInvokerThatGrantsAccess(signal);
        AtomicReference<SignalsRegistry> signalsRegistry = new AtomicReference<>();
        try (var dummy = Mockito.mockConstruction(SignalsRegistry.class,
                (mockSignalRegistry, context) -> {
                    when(mockSignalRegistry.get("clientSignalId"))
                            .thenReturn(internalSignal);
                    signalsRegistry.set(mockSignalRegistry);
                })) {
            SecureSignalsRegistry secureSignalsRegistry = new SecureSignalsRegistry(
                    invoker, new ObjectMapper());
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
                invoker, new ObjectMapper());
        assertThrows(
                EndpointInvocationException.EndpointUnauthorizedException.class,
                () -> secureSignalsRegistry.register("clientSignalId",
                        "endpoint", "method", null));
    }

    @Test
    public void when_accessToEndpointIsAllowed_get_returnsSignal()
            throws Exception {
        AbstractSignal<?> signal = Mockito.mock(AbstractSignal.class);
        InternalSignal internalSignal = new InternalSignal(signal,
                new ObjectMapper());
        EndpointInvoker invoker = mockEndpointInvokerThatGrantsAccess(signal);
        AtomicReference<SignalsRegistry> signalsRegistry = new AtomicReference<>();
        try (var dummy = Mockito.mockConstruction(SignalsRegistry.class,
                (mockSignalRegistry, context) -> {
                    when(mockSignalRegistry.get("clientSignalId"))
                            .thenReturn(internalSignal);
                    signalsRegistry.set(mockSignalRegistry);
                })) {
            SecureSignalsRegistry secureSignalsRegistry = new SecureSignalsRegistry(
                    invoker, new ObjectMapper());
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
                invoker, new ObjectMapper());
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
                    invoker, new ObjectMapper());

            assertThrows(
                    EndpointInvocationException.EndpointForbiddenException.class,
                    () -> secureSignalsRegistry.register("clientSignalId",
                            "endpoint", "method", null));
        }
    }

    private EndpointInvoker mockEndpointInvokerThatGrantsAccess(
            AbstractSignal<?> signal) throws Exception {
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
