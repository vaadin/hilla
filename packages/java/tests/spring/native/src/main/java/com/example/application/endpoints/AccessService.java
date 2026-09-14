/*
 * Copyright 2000-2026 Vaadin Ltd.
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
package com.example.application.endpoints;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.BrowserCallable;

/**
 * Covers the three access annotations in one endpoint. The checks read the
 * annotations reflectively, so a native image needs the hints for this class to
 * reject and accept the same calls as the application on the JVM.
 */
@BrowserCallable
public class AccessService {

    @AnonymousAllowed
    public String forAnyone() {
        return "anyone";
    }

    @PermitAll
    public String forAnyUser() {
        return "any user";
    }

    @RolesAllowed("ADMIN")
    public String forAdmin() {
        return "admin";
    }

}
