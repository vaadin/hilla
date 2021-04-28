package com.vaadin.flow.server.connect;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;

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
