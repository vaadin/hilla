package com.vaadin.hilla.signals.core.registry;

import com.vaadin.hilla.AuthenticationUtil;
import com.vaadin.hilla.EndpointInvocationException;
import com.vaadin.hilla.EndpointInvoker;
import com.vaadin.hilla.EndpointRegistry;
import com.vaadin.hilla.signals.NumberSignal;
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

    public SecureSignalsRegistry(EndpointInvoker invoker) {
        this.invoker = invoker;
        this.delegate = new SignalsRegistry();
    }

    public synchronized void register(String clientSignalId,
            String endpointName, String methodName)
            throws EndpointInvocationException.EndpointAccessDeniedException,
            EndpointInvocationException.EndpointNotFoundException,
            EndpointInvocationException.EndpointBadRequestException,
            EndpointInvocationException.EndpointInternalException {
        Principal principal = AuthenticationUtil
                .getSecurityHolderAuthentication();
        Function<String, Boolean> isInRole = AuthenticationUtil
                .getSecurityHolderRoleChecker();
        checkAccess(endpointName, methodName, principal, isInRole);

        NumberSignal signal = (NumberSignal) invoker.invoke(endpointName,
                methodName, null, principal, isInRole);
        endpointMethods.put(clientSignalId,
                new EndpointMethod(endpointName, methodName));
        delegate.register(clientSignalId, signal);
    }

    public synchronized NumberSignal get(String clientSignalId)
            throws EndpointInvocationException.EndpointAccessDeniedException,
            EndpointInvocationException.EndpointNotFoundException {
        var endpointMethodInfo = endpointMethods.get(clientSignalId);
        if (endpointMethodInfo == null) {
            return null;
        }
        checkAccess(endpointMethodInfo.endpoint, endpointMethodInfo.method);
        return delegate.get(clientSignalId);
    }

    private void checkAccess(String endpointName, String methodName)
            throws EndpointInvocationException.EndpointNotFoundException,
            EndpointInvocationException.EndpointAccessDeniedException {
        Principal principal = AuthenticationUtil
                .getSecurityHolderAuthentication();
        Function<String, Boolean> isInRole = AuthenticationUtil
                .getSecurityHolderRoleChecker();
        checkAccess(endpointName, methodName, principal, isInRole);
    }

    private void checkAccess(String endpointName, String methodName,
            Principal principal, Function<String, Boolean> isInRole)
            throws EndpointInvocationException.EndpointNotFoundException,
            EndpointInvocationException.EndpointAccessDeniedException {
        EndpointRegistry.VaadinEndpointData endpointData = invoker
                .getVaadinEndpointData(endpointName);
        Method method = getMethod(endpointData, methodName);
        var checkError = invoker.checkAccess(endpointData, method, principal,
                isInRole);
        if (checkError != null) {
            throw new EndpointInvocationException.EndpointAccessDeniedException(
                    String.format(
                            "Endpoint '%s' method '%s' request cannot be accessed, reason: '%s'",
                            endpointName, methodName, checkError));
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
