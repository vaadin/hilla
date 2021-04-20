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

import java.lang.reflect.Method;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;

import com.vaadin.flow.server.VaadinService;

/**
 * Component used for checking role-based ACL in Vaadin Endpoints.
 * <p>
 * For each request that is trying to access the method in the corresponding
 * Vaadin Endpoint, the permission check is carried on.
 * <p>
 * It looks for {@link AnonymousAllowed} {@link PermitAll}, {@link DenyAll} and
 * {@link RolesAllowed} annotations in endpoint methods and classes containing
 * these methods (no super classes' annotations are taken into account).
 * <p>
 * Method-level annotation override Class-level ones.
 * <p>
 * In the next example, since the class is denied to all, method1 is not
 * accessible to anyone, method2 can be executed by any authorized used, method3
 * is only allowed to the accounts having the ROLE_USER authority and method4 is
 * available for every user, including anonymous ones that don't provide any
 * token in their requests.
 *
 * <pre class="code">
 * &#64;Endpoint
 * &#64;DenyAll
 * public class DemoEndpoint {
 *
 *     public void method1() {
 *     }
 *
 *     &#64;PermitAll
 *     public void method2() {
 *     }
 *
 *     &#64;RolesAllowed("ROLE_USER")
 *     public void method3() {
 *     }
 *
 *     &#64;AnonymousAllowed
 *     public void method4() {
 *     }
 * }
 * </pre>
 *
 */
public class VaadinConnectAccessChecker {

    public static final String ACCESS_DENIED_MSG = "Access denied";

    public static final String ACCESS_DENIED_MSG_DEV_MODE = "Unauthorized access to Vaadin endpoint; "
            + "to enable endpoint access use one of the following annotations: @AnonymousAllowed, @PermitAll, @RolesAllowed";

    private CsrfChecker csrfChecker;

    private AccessAnnotationChecker accessAnnotationChecker;

    /**
     * Creates a new instance.
     * 
     * @param csrfChecker
     *            the csrf checker to use
     * @param accessAnnotationChecker
     *            the access checker to use
     */
    public VaadinConnectAccessChecker(
            AccessAnnotationChecker accessAnnotationChecker,
            CsrfChecker csrfChecker) {
        this.accessAnnotationChecker = accessAnnotationChecker;
        this.csrfChecker = csrfChecker;
    }

    /**
     * Check that the endpoint is accessible for the current user.
     *
     * @param method
     *            the Vaadin endpoint method to check ACL
     * @param request
     *            the request that triggers the <code>method</code> invocation
     * @return an error String with an issue description, if any validation
     *         issues occur, {@code null} otherwise
     */
    public String check(Method method, HttpServletRequest request) {
        if (!csrfChecker.validateCsrfTokenInRequest(request)) {
            return ACCESS_DENIED_MSG;
        }

        if (accessAnnotationChecker.annotationAllowsAccess(method, request)) {
            return null;
        }

        if (isDevMode()) {
            // suggest access control annotations in dev mode
            return ACCESS_DENIED_MSG_DEV_MODE;
        } else {
            return ACCESS_DENIED_MSG;
        }
    }

    private boolean isDevMode() {
        VaadinService vaadinService = VaadinService.getCurrent();
        return (vaadinService != null && !vaadinService
                .getDeploymentConfiguration().isProductionMode());
    }

    /**
     * Enable or disable XSRF token checking in endpoints.
     *
     * @param xsrfProtectionEnabled
     *            enable or disable protection.
     */
    public void enableCsrf(boolean xsrfProtectionEnabled) {
        csrfChecker.setCsrfProtection(xsrfProtectionEnabled);
    }

    /**
     * Returns the instance used for checking access based on annotations.
     * 
     * @return the instance used for checking access based on annotations
     */
    public AccessAnnotationChecker getAccessAnnotationChecker() {
        return accessAnnotationChecker;
    }

}
