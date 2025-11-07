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
import tools.jackson.databind.node.ObjectNode;
import com.vaadin.hilla.AuthenticationUtil;
import com.vaadin.hilla.EndpointInvocationException;
import com.vaadin.hilla.EndpointInvoker;
import com.vaadin.hilla.EndpointRegistry;
import com.vaadin.signals.AbstractSignal;
import jakarta.validation.constraints.NotNull;

import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Proxy for the accessing the SignalRegistry.
 */
@Component
public class SecureSignalsRegistry {

    record EndpointMethod(String endpoint, String method) {
    }

    private final Map<String, EndpointMethod> endpointMethods = new HashMap<>();
    private final SignalsRegistry delegate;
    private final EndpointInvoker invoker;
    private final ObjectMapper objectMapper;

    public SecureSignalsRegistry(EndpointInvoker invoker,
            ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.invoker = invoker;
        this.delegate = new SignalsRegistry();
    }

    public synchronized void register(String clientSignalId,
            String endpointName, String methodName, ObjectNode body)
            throws EndpointInvocationException.EndpointHttpException {
        Principal principal = AuthenticationUtil
                .getSecurityHolderAuthentication();
        Function<String, Boolean> isInRole = AuthenticationUtil
                .getSecurityHolderRoleChecker();
        checkAccess(endpointName, methodName, principal, isInRole);

        AbstractSignal<?> signal = (AbstractSignal<?>) invoker
                .invoke(endpointName, methodName, body, principal, isInRole);
        endpointMethods.put(clientSignalId,
                new EndpointMethod(endpointName, methodName));
        delegate.register(clientSignalId,
                new InternalSignal(signal, objectMapper));
    }

    public synchronized void unsubscribe(String clientSignalId) {
        var endpointMethodInfo = endpointMethods.get(clientSignalId);
        if (endpointMethodInfo == null) {
            return;
        }
        delegate.removeClientSignalToSignalMapping(clientSignalId);
        endpointMethods.remove(clientSignalId);
    }

    public synchronized InternalSignal get(String clientSignalId)
            throws EndpointInvocationException.EndpointHttpException {
        var endpointMethodInfo = endpointMethods.get(clientSignalId);
        if (endpointMethodInfo == null) {
            return null;
        }
        checkAccess(endpointMethodInfo.endpoint, endpointMethodInfo.method);
        return delegate.get(clientSignalId);
    }

    private void checkAccess(String endpointName, String methodName)
            throws EndpointInvocationException.EndpointHttpException {
        Principal principal = AuthenticationUtil
                .getSecurityHolderAuthentication();
        Function<String, Boolean> isInRole = AuthenticationUtil
                .getSecurityHolderRoleChecker();
        checkAccess(endpointName, methodName, principal, isInRole);
    }

    private void checkAccess(String endpointName, String methodName,
            Principal principal, Function<String, Boolean> isInRole)
            throws EndpointInvocationException.EndpointHttpException {
        EndpointRegistry.VaadinEndpointData endpointData = invoker
                .getVaadinEndpointData(endpointName);
        Method method = getMethod(endpointData, methodName);
        var checkError = invoker.checkAccess(endpointData, method, principal,
                isInRole);
        if (checkError != null) {
            var message = String.format(
                    "Endpoint '%s' method '%s' request cannot be accessed, reason: '%s'",
                    endpointName, methodName, checkError);
            if (principal == null) {
                throw new EndpointInvocationException.EndpointUnauthorizedException(
                        message);
            } else {
                throw new EndpointInvocationException.EndpointForbiddenException(
                        message);
            }
        }
    }

    private Method getMethod(
            @NotNull EndpointRegistry.VaadinEndpointData endpointData,
            String methodName)
            throws EndpointInvocationException.EndpointNotFoundException {
        return endpointData.getMethod(methodName).orElseThrow(
                EndpointInvocationException.EndpointNotFoundException::new);
    }
}
