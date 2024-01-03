/*
 * Copyright 2000-2023 Vaadin Ltd.
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
package com.vaadin.hilla;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;

import com.vaadin.flow.internal.hilla.EndpointRequestUtil;
import com.vaadin.hilla.auth.EndpointAccessChecker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.PathContainer;
import org.springframework.http.server.RequestPath;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.web.util.pattern.PathPattern;

import org.springframework.web.util.pattern.PathPatternParser;

/**
 * A util class related to classes available to the browser.
 */
@Component
public class EndpointUtil implements EndpointRequestUtil {

    @Autowired
    private EndpointProperties endpointProperties;

    @Autowired
    private EndpointRegistry registry;

    @Autowired
    private EndpointAccessChecker accessChecker;

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
    @Override
    public boolean isEndpointRequest(HttpServletRequest request) {
        return getEndpoint(request).isPresent();
    }

    private Optional<Method> getEndpoint(HttpServletRequest request) {
        return getEndpointData(request).map(EndpointData::method);
    }

    /**
     * Checks if the given request goes to an anonymous (public) endpoint.
     *
     * @param request
     *            the HTTP request to check
     * @return <code>true</code> if the request goes to an anonymous endpoint,
     *         <code>false</code> otherwise
     */
    @Override
    public boolean isAnonymousEndpoint(HttpServletRequest request) {
        var endpointData = getEndpointData(request);
        if (endpointData.isEmpty()) {
            return false;
        }
        var invokedEndpointClass = ClassUtils
                .getUserClass(endpointData.get().endpointObject());
        var methodDeclaringClass = endpointData.get().method()
                .getDeclaringClass();
        if (methodDeclaringClass.equals(invokedEndpointClass)) {
            return accessChecker.getAccessAnnotationChecker().hasAccess(
                    endpointData.get().method(), null, role -> false);
        } else {
            return accessChecker.getAccessAnnotationChecker()
                    .hasAccess(invokedEndpointClass, null, role -> false);
        }
    }

    private Optional<PathPattern.PathMatchInfo> getPathMatchInfo(
            HttpServletRequest request) {
        PathPatternParser pathParser = new PathPatternParser();
        PathPattern pathPattern = pathParser
                .parse(endpointProperties.getEndpointPrefix()
                        + EndpointController.ENDPOINT_METHODS);

        RequestPath requestPath = RequestPath.parse(request.getRequestURI(),
                request.getContextPath());
        PathContainer pathWithinApplication = requestPath
                .pathWithinApplication();
        PathPattern.PathMatchInfo matchInfo = pathPattern
                .matchAndExtract(pathWithinApplication);
        return Optional.ofNullable(matchInfo);
    }

    private Optional<EndpointData> getEndpointData(HttpServletRequest request) {
        Optional<PathPattern.PathMatchInfo> matchInfo = getPathMatchInfo(
                request);
        if (matchInfo.isEmpty()) {
            return Optional.empty();
        }

        Map<String, String> uriVariables = matchInfo.get().getUriVariables();
        String endpointName = uriVariables.get("endpoint");
        String methodName = uriVariables.get("method");
        EndpointRegistry.VaadinEndpointData data = registry.get(endpointName);
        if (data == null) {
            return Optional.empty();
        }
        Optional<Method> endpointMethod = data.getMethod(methodName);
        return endpointMethod.map(
                method -> new EndpointData(method, data.getEndpointObject()));
    }

    private record EndpointData(Method method, Object endpointObject) {
    }

}
