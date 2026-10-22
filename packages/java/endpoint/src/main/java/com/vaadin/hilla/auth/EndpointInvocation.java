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
package com.vaadin.hilla.auth;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.util.Objects;

import org.aopalliance.intercept.MethodInvocation;

/**
 * Describes an attempt to call an endpoint method.
 * <p>
 * Instances are used as the subject of the Spring Security authorization events
 * that are published when the access to an endpoint method is denied, so that
 * listeners of
 * {@link org.springframework.security.authorization.event.AuthorizationDeniedEvent}
 * can handle endpoint calls the same way as method security invocations.
 * <p>
 * This is a description of an invocation that never happened, not a resumable
 * invocation: the arguments are never included, as they are not deserialized
 * before the access check is done, and {@link #proceed()} is not supported.
 */
public class EndpointInvocation implements MethodInvocation {

    private static final Object[] NO_ARGUMENTS = new Object[0];

    private final String endpointName;
    private final Object endpointObject;
    private final Method method;

    /**
     * Creates a new instance.
     *
     * @param endpointName
     *            the name of the endpoint, not <code>null</code>
     * @param endpointObject
     *            the endpoint object the method belongs to, not
     *            <code>null</code>
     * @param method
     *            the endpoint method, not <code>null</code>
     */
    public EndpointInvocation(String endpointName, Object endpointObject,
            Method method) {
        this.endpointName = Objects.requireNonNull(endpointName);
        this.endpointObject = Objects.requireNonNull(endpointObject);
        this.method = Objects.requireNonNull(method);
    }

    /**
     * Gets the name of the endpoint the call was addressed to.
     *
     * @return the endpoint name
     */
    public String getEndpointName() {
        return endpointName;
    }

    /**
     * Gets the name of the endpoint method the call was addressed to.
     *
     * @return the endpoint method name
     */
    public String getMethodName() {
        return method.getName();
    }

    @Override
    public Method getMethod() {
        return method;
    }

    /**
     * Gets the method arguments, always empty.
     * <p>
     * The request payload is deliberately not exposed: the access check is done
     * before the payload is deserialized.
     *
     * @return an empty array
     */
    @Override
    public Object[] getArguments() {
        return NO_ARGUMENTS;
    }

    @Override
    public Object getThis() {
        return endpointObject;
    }

    @Override
    public AccessibleObject getStaticPart() {
        return method;
    }

    /**
     * Not supported, as this describes a call that was never made.
     *
     * @throws UnsupportedOperationException
     *             always
     */
    @Override
    public Object proceed() {
        throw new UnsupportedOperationException(
                "An EndpointInvocation only describes an endpoint call, it cannot be executed");
    }

    @Override
    public String toString() {
        return "EndpointInvocation[endpoint=" + endpointName + ", method="
                + getMethodName() + "]";
    }
}
