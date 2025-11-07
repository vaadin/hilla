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
package com.vaadin.flow.spring.fusionsecurityjwt;

import java.util.Base64;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.Cookie;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

public class SecurityIT
        extends com.vaadin.flow.spring.fusionsecurity.SecurityIT {

    @Test
    public void jwt_cookie_set_for_user() {
        openLogin();
        loginUser();
        checkJwtUsername("john");
    }

    @Test
    public void jwt_cookie_set_for_admin() {
        openLogin();
        loginAdmin();
        checkJwtUsername("emma");
    }

    @Test
    public void jwt_cookie_reset_on_logout() {
        openLogin();
        Assert.assertNull(getJwtCookie());
        logout();
        Assert.assertNull(getJwtCookie());
    }

    @Test
    public void csrf_cookie() {
        open("");

        Assert.assertNotNull(getSpringCsrfCookie());
        openLogin();
        loginUser();

        Assert.assertNotNull(getSpringCsrfCookie());

        logout();

        Assert.assertNotNull(getSpringCsrfCookie());
    }

    @Test
    public void stateless_for_anonymous_user() {
        open("");

        simulateNewServer();

        assertPublicEndpointWorks();
    }

    @Test
    public void stateless_for_user() {
        openLogin();
        loginUser();

        simulateNewServer();

        assertPublicEndpointWorks();
        navigateTo("private", false);
        assertPrivatePageShown(USER_FULLNAME);
        refresh();
        assertPrivatePageShown(USER_FULLNAME);
    }

    @Test
    public void stateless_for_admin() {
        openLogin();
        loginAdmin();

        simulateNewServer();

        assertPublicEndpointWorks();
        navigateTo("private", false);
        assertPrivatePageShown(ADMIN_FULLNAME);
        refresh();
        assertPrivatePageShown(ADMIN_FULLNAME);
    }

    @Test
    public void stateless_for_anonymous_after_logout() {
        openLogin();
        loginUser();
        logout();

        simulateNewServer();

        assertPublicEndpointWorks();
    }

    @Override
    public void reload_when_anonymous_session_expires() {
        // Skip: the server session is not relevant in the stateless mode
    }

    @Override
    public void reload_when_user_session_expires() {
        // Skip: the server session is not relevant in the stateless mode
    }

    @Test
    public void reload_when_user_jwt_expires() {
        openLogin();
        loginUser();
        getDriver().manage().deleteCookieNamed("jwt.headerAndPayload");
        getDriver().manage().deleteCookieNamed("jwt.signature");
        navigateTo("private", false);
        assertLoginViewShown();
    }

    private void openLogin() {
        getDriver().get(getRootURL() + "/login");
    }

    private Cookie getSpringCsrfCookie() {
        return getDriver().manage().getCookieNamed("XSRF-TOKEN");
    }

    private Cookie getJwtCookie() {
        return getDriver().manage().getCookieNamed("jwt.headerAndPayload");
    }

    private void checkJwtUsername(String expectedUsername) {
        Cookie jwtCookie = getJwtCookie();
        Assert.assertNotNull(jwtCookie);

        String payload = jwtCookie.getValue().split("\\.")[1];
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode payloadJson = objectMapper.readTree(
                    new String(Base64.getUrlDecoder().decode(payload)));
            Assert.assertEquals(expectedUsername,
                    payloadJson.get("sub").asText());
        } catch (Exception e) {
            Assert.fail("Failed to parse JWT payload: " + e.getMessage());
        }
    }

}
