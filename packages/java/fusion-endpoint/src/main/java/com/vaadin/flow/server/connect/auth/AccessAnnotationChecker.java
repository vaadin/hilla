/*
 * Copyright 2000-2020 Vaadin Ltd.
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
package com.vaadin.flow.server.connect.auth;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.Principal;
import java.util.function.Function;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;

/**
 * Checks if a given user has access to a given method.
 * <p>
 * Check is performed as follows when called for a method:
 * <ol>
 * <li>A security annotation (see below) is searched for on that particular
 * method.</li>
 * <li>If a security annotation was not found on the method, checks the class
 * the method is declared in.</li>
 * <li>If no security annotation was found, deny access by default</li>
 * </ol>
 * <p>
 * The security annotations checked and their meaning are:
 * <ul>
 * <li>{@link AnonymousAllowed} - allows access to any logged on or not logged
 * in user. Public access.</li>
 * <li>{@link PermitAll} - allows access to any logged in user but denies access
 * to anonymous users.</li>
 * <li>{@link RolesAllowed} - allows access there is a logged in user that has
 * any of the roles mentioned in the annotation</li>
 * <li>{@link DenyAll} - denies access.</li>
 * </ul>
 */
public class AccessAnnotationChecker {

    /**
     * 
     * Checks if the user defined by the request (using
     * {@link HttpServletRequest#getUserPrincipal()} and
     * {@link HttpServletRequest#isUserInRole(String)} has access to the given
     * method.
     * 
     * @param method
     *            the method to check access to
     * @param request
     *            the http request to use for user information
     * @return {@code true} if the user has access to the given method,
     *         {@code false} otherwise
     */
    public boolean annotationAllowsAccess(Method method,
            HttpServletRequest request) {
        return annotationAllowsAccess(method, request.getUserPrincipal(),
                request::isUserInRole);
    }

    /**
     * 
     * Checks if the user defined by the request (using
     * {@link HttpServletRequest#getUserPrincipal()} and
     * {@link HttpServletRequest#isUserInRole(String)} has access to the given
     * class.
     * 
     * @param cls
     *            the class to check access to
     * @param request
     *            the http request to use for user information
     * @return {@code true} if the user has access to the given method,
     *         {@code false} otherwise
     */
    public boolean annotationAllowsAccess(Class<?> cls,
            HttpServletRequest request) {
        return annotationAllowsAccess(getSecurityTarget(cls),
                request.getUserPrincipal(), request::isUserInRole);
    }

    /**
     * Checks if the user defined by the given {@link Principal} and role
     * checker has access to the given method.
     * 
     * @param method
     *            the method to check access to
     * @param principal
     *            the principal of the user
     * @param roleChecker
     *            a function that can answer if a user has a given role
     * @return {@code true} if the user has access to the given method,
     *         {@code false} otherwise
     */
    public boolean annotationAllowsAccess(Method method, Principal principal,
            Function<String, Boolean> roleChecker) {
        return annotationAllowsAccess(getSecurityTarget(method), principal,
                roleChecker);
    }

    /**
     * Gets the method or class to check for security restrictions.
     *
     * @param method
     *            the method to look up
     * @return the entity that is responsible for security settings for the
     *         method passed
     * @throws IllegalArgumentException
     *             if the method is not public
     */
    public AnnotatedElement getSecurityTarget(Method method) {
        if (!Modifier.isPublic(method.getModifiers())) {
            throw new IllegalArgumentException(String.format(
                    "The method '%s' is not public hence cannot have a security target",
                    method));
        }
        return hasSecurityAnnotation(method) ? method
                : method.getDeclaringClass();
    }

    /**
     * Gets the class to check for security restrictions.
     *
     * @param cls
     *            the class to check
     * @return the entity that is responsible for security settings for the
     *         method passed
     * @throws IllegalArgumentException
     *             if the method is not public
     */
    public AnnotatedElement getSecurityTarget(Class<?> cls) {
        return cls;
    }

    private boolean annotationAllowsAccess(
            AnnotatedElement annotatedClassOrMethod, Principal principal,
            Function<String, Boolean> roleChecker) {
        if (annotatedClassOrMethod.isAnnotationPresent(DenyAll.class)) {
            return false;
        }
        if (annotatedClassOrMethod
                .isAnnotationPresent(AnonymousAllowed.class)) {
            return true;
        }
        if (principal == null) {
            return false;
        }
        RolesAllowed rolesAllowed = annotatedClassOrMethod
                .getAnnotation(RolesAllowed.class);
        if (rolesAllowed == null) {
            return annotatedClassOrMethod.isAnnotationPresent(PermitAll.class);
        } else {
            return roleAllowed(rolesAllowed, roleChecker);
        }
    }

    private boolean roleAllowed(RolesAllowed rolesAllowed,
            Function<String, Boolean> roleChecker) {
        for (String role : rolesAllowed.value()) {
            if (roleChecker.apply(role)) {
                return true;
            }
        }

        return false;
    }

    private boolean hasSecurityAnnotation(Method method) {
        return method.isAnnotationPresent(AnonymousAllowed.class)
                || method.isAnnotationPresent(PermitAll.class)
                || method.isAnnotationPresent(DenyAll.class)
                || method.isAnnotationPresent(RolesAllowed.class);
    }

}
