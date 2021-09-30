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
    public void cookie_set_for_user() {
        openLogin();
        loginUser();
        checkJwtUsername("john");
    }

    @Test
    public void cookie_set_for_admin() {
        openLogin();
        loginAdmin();
        checkJwtUsername("emma");
    }

    @Test
    public void cookie_reset_on_logout() {
        openLogin();
        Assert.assertNull(getJwtCookie());
        loginUser();
        logout();
        Assert.assertNull(getJwtCookie());
    }

    private void openLogin() {
        getDriver().get(getRootURL() + "/login");
    }

    private Cookie getJwtCookie() {
        return getDriver().manage().getCookieNamed("jwt" +
                ".headerAndPayload");
    }

    private void checkJwtUsername(String expectedUsername) {
        Cookie jwtCookie = getJwtCookie();
        Assert.assertNotNull(jwtCookie);

        String payload = jwtCookie.getValue().split("\\.")[1];
        JsonObject payloadJson = Json.parse(
                new String(Base64.getUrlDecoder().decode(payload)));
        Assert.assertEquals(expectedUsername, payloadJson.getString("sub"));
    }
}
