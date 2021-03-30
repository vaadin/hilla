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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A security annotation, granting anonymous access to the Vaadin endpoint (or
 * its method) it is placed onto.
 * <p>
 * This means that any user will be able to trigger an endpoint method (if
 * placed on an endpoint class) or the particular endpoint method (if placed on
 * an endpoint method) without providing an authentication token.
 * <p>
 * If there are other supported security annotations present on the same class
 * or method:
 * <ul>
 * <li>{@link AnonymousAllowed} is overridden by
 * {@link javax.annotation.security.DenyAll} annotation, disallowing any user
 * from accessing the method</li>
 * <li>{@link AnonymousAllowed} annotation overrides
 * {@link javax.annotation.security.PermitAll} and
 * {@link javax.annotation.security.RolesAllowed} annotations, allowing all
 * users to access the method (anonymous and authenticated users with any
 * security roles)</li>
 * </ul>
 *
 * @see VaadinConnectAccessChecker for security rules check implementation
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
public @interface AnonymousAllowed {
}
