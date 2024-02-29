/*
 * Copyright 2000-2024 Vaadin Ltd.
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

package com.vaadin.hilla.route;

import jakarta.servlet.http.HttpServletRequest;
import java.io.Serializable;
/**
 * A container for utility methods related with Routes.
 * <p>
 * For internal use only. May be renamed or removed in a future release.
 *
 * @author Vaadin Ltd
 * @since 24.4
 */
//TODO REMOVE THIS, SHOULDN't be MERGED INTO MAIN
public interface RouteRequestUtil extends Serializable {


    /**
     * Checks if the request is for a known route.
     * <p>
     * Note even if this method returns <code>true</code>, there is no guarantee
     * that an endpoint method will actually be called, e.g. access might be
     * denied.
     *
     * @param request
     *            the HTTP request
     * @return <code>true</code> if the request is for an endpoint,
     *         <code>false</code> otherwise
     */
    boolean isRouteRequest(HttpServletRequest request);

    /**
     * Checks if the given request goes to an anonymous (public) route.
     *
     * @param request
     *            the HTTP request to check
     * @return <code>true</code> if the request goes to an anonymous route,
     *         <code>false</code> otherwise
     */
    boolean isAnonymousRoute(HttpServletRequest request);

    /**
     * Checks if the given request goes to an authorized route.
     * (user needs specific role for access)
     *
     * @param request
     *            the HTTP request to check
     * @return <code>true</code> if the request goes to an authorized route,
     *         <code>false</code> otherwise
     */
    boolean isAuthenticatedRoute(HttpServletRequest request);

    /**
     * Checks if the given request is allowed route to the user.
     *
     * @param request
     *            the HTTP request to check
     * @return <code>true</code> if the request goes allowed route,
     *         <code>false</code> otherwise
     */
    boolean isRouteAllowed(HttpServletRequest request);

}
