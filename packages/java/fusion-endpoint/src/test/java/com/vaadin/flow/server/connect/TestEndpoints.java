package com.vaadin.flow.server.connect;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;

import com.vaadin.flow.server.connect.auth.AnonymousAllowed;

public class TestEndpoints {

    @Endpoint
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

    @Endpoint
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

    @Endpoint
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

    @Endpoint
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

    @Endpoint
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

    @Endpoint
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
