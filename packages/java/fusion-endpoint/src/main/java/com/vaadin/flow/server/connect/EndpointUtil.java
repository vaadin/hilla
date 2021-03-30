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
package com.vaadin.flow.server.connect;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * A util class related to {@link Endpoint}.
 */
@Component
public class EndpointUtil {

    @Autowired
    private VaadinEndpointProperties endpointProperties;

    /**
     * Checks if the request is for an endpoint.
     *
     * Note even if this method returns <code>true</code>, there is no guarantee
     * that an endpoint method will actually be called (it might not exist, access
     * might be denied etc).
     *
     * @param request the HTTP request
     * @return <code>true</code> if the request is for an endpoint,
     *         <code>false</code> otherwise
     */
    public boolean isEndpointRequest(HttpServletRequest request) {
        String path = getRequestPath(request);
        return path.startsWith(endpointProperties.getVaadinEndpointPrefix() + "/");
    }

    /**
     * Returns the full request path, including the servlet path.
     *
     * With Spring Security, the path is most often included fully as "servletPath"
     * and pathInfo is null
     */
    private static String getRequestPath(HttpServletRequest request) {
        String servletPath = request.getServletPath();
        String pathInfo = request.getPathInfo();
        String url = "";
        if (servletPath != null) {
            url += servletPath;
        }
        if (pathInfo != null) {
            url += pathInfo;
        }
        return url;
    }

}
