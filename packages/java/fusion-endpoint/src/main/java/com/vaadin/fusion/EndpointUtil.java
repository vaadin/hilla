/*
 * Copyright 2000-2021 Vaadin Ltd.
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
package com.vaadin.fusion;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import com.vaadin.fusion.auth.VaadinConnectAccessChecker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.PathContainer;
import org.springframework.http.server.RequestPath;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ServletRequestPathUtils;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPattern.PathMatchInfo;
import org.springframework.web.util.pattern.PathPatternParser;

/**
 * A util class related to {@link Endpoint}.
 */
@Component
public class EndpointUtil {

    @Autowired
    private VaadinEndpointProperties endpointProperties;

    @Autowired
    private EndpointRegistry registry;

    @Autowired
    private VaadinConnectAccessChecker accessChecker;

    /**
     * Checks if the request is for an endpoint.
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
    public boolean isEndpointRequest(HttpServletRequest request) {
        return getEndpoint(request).isPresent();
    }

    private Optional<Method> getEndpoint(HttpServletRequest request) {
        PathPatternParser pathParser = new PathPatternParser();
        PathPattern pathPattern = pathParser
                .parse(endpointProperties.getVaadinEndpointPrefix()
                        + VaadinConnectController.ENDPOINT_METHODS);
        RequestPath requestPath = ServletRequestPathUtils
                .parseAndCache(request);
        PathContainer pathWithinApplication = requestPath
                .pathWithinApplication();
        PathMatchInfo matchInfo = pathPattern
                .matchAndExtract(pathWithinApplication);
        if (matchInfo == null) {
            return Optional.empty();
        }

        Map<String, String> uriVariables = matchInfo.getUriVariables();
        String endpointName = uriVariables.get("endpoint");
        String endpointMethod = uriVariables.get("method");

        EndpointRegistry.VaadinEndpointData data = registry.get(endpointName);
        if (data == null) {
            return Optional.empty();
        }
        return data.getMethod(endpointMethod);
    }

    /**
     * Checks if the given request goes to an anonymous (public) endpoint.
     * 
     * @param request
     *            the HTTP request to check
     * @return <code>true</code> if the request goes to an anonymous endpoint,
     *         <code>false</code> otherwise
     */
    public boolean isAnonymousEndpoint(HttpServletRequest request) {
        Optional<Method> method = getEndpoint(request);
        if (!method.isPresent()) {
            return false;
        }

        return accessChecker.getAccessAnnotationChecker()
                .hasAccess(method.get(), null, role -> false);
    }

}
