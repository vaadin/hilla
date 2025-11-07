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
package com.vaadin.hilla;

import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;

import com.vaadin.flow.server.auth.AnonymousAllowed;

public class AccessControlTestClasses {

    public static class NoAnnotationEndpoint {

        public void noAnnotation() {

        }

        @AnonymousAllowed
        public void anonymousAllowed() {

        }

        @PermitAll
        public void permitAll() {

        }

        @DenyAll
        public void denyAll() {

        }

        @RolesAllowed("user")
        public void rolesAllowedUser() {

        }

        @RolesAllowed("admin")
        public void rolesAllowedAdmin() {

        }

        @RolesAllowed({ "user", "admin" })
        public void rolesAllowedUserAdmin() {
        }

    }

    @AnonymousAllowed
    public static class AnonymousAllowedEndpoint {

        public void noAnnotation() {

        }

        @AnonymousAllowed
        public void anonymousAllowed() {

        }

        @PermitAll
        public void permitAll() {

        }

        @DenyAll
        public void denyAll() {

        }

        @RolesAllowed("user")
        public void rolesAllowedUser() {

        }

        @RolesAllowed("admin")
        public void rolesAllowedAdmin() {

        }

        @RolesAllowed({ "user", "admin" })
        public void rolesAllowedUserAdmin() {
        }

    }

    @PermitAll
    public static class PermitAllEndpoint {

        public void noAnnotation() {

        }

        @AnonymousAllowed
        public void anonymousAllowed() {

        }

        @PermitAll
        public void permitAll() {

        }

        @DenyAll
        public void denyAll() {

        }

        @RolesAllowed("user")
        public void rolesAllowedUser() {

        }

        @RolesAllowed("admin")
        public void rolesAllowedAdmin() {

        }

        @RolesAllowed({ "user", "admin" })
        public void rolesAllowedUserAdmin() {
        }

    }

    @DenyAll
    public static class DenyAllEndpoint {

        public void noAnnotation() {

        }

        @AnonymousAllowed
        public void anonymousAllowed() {

        }

        @PermitAll
        public void permitAll() {

        }

        @DenyAll
        public void denyAll() {

        }

        @RolesAllowed("user")
        public void rolesAllowedUser() {

        }

        @RolesAllowed("admin")
        public void rolesAllowedAdmin() {

        }

        @RolesAllowed({ "user", "admin" })
        public void rolesAllowedUserAdmin() {
        }

    }

    @RolesAllowed("user")
    public static class RolesAllowedUserEndpoint {

        public void noAnnotation() {

        }

        @AnonymousAllowed
        public void anonymousAllowed() {

        }

        @PermitAll
        public void permitAll() {

        }

        @DenyAll
        public void denyAll() {

        }

        @RolesAllowed("user")
        public void rolesAllowedUser() {

        }

        @RolesAllowed("admin")
        public void rolesAllowedAdmin() {

        }

        @RolesAllowed({ "user", "admin" })
        public void rolesAllowedUserAdmin() {
        }

    }

    @RolesAllowed("admin")
    public static class RolesAllowedAdminEndpoint {

        public void noAnnotation() {

        }

        @AnonymousAllowed
        public void anonymousAllowed() {

        }

        @PermitAll
        public void permitAll() {

        }

        @DenyAll
        public void denyAll() {

        }

        @RolesAllowed("user")
        public void rolesAllowedUser() {

        }

        @RolesAllowed("admin")
        public void rolesAllowedAdmin() {

        }

        @RolesAllowed({ "user", "admin" })
        public void rolesAllowedUserAdmin() {
        }

    }
}
