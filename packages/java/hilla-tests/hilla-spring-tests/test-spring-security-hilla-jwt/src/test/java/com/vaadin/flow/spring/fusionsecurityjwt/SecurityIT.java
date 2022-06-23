package com.vaadin.flow.spring.fusionsecurityjwt;

import java.util.Base64;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.Cookie;

import elemental.json.Json;
import elemental.json.JsonObject;

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
        JsonObject payloadJson = Json
                .parse(new String(Base64.getUrlDecoder().decode(payload)));
        Assert.assertEquals(expectedUsername, payloadJson.getString("sub"));
    }

}
