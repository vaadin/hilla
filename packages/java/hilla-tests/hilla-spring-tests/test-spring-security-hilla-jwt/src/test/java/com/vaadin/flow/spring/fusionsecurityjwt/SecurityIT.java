package com.vaadin.flow.spring.fusionsecurityjwt;

import java.util.Base64;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.JavascriptExecutor;

import com.vaadin.testbench.TestBenchElement;

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

    private void openLogin() {
        getDriver().get(getRootURL() + "/login");
    }

    private Cookie getSpringCsrfCookie() {
        return getDriver().manage().getCookieNamed("XSRF-TOKEN");
    }

    private Cookie getJwtCookie() {
        return getDriver().manage().getCookieNamed("jwt" + ".headerAndPayload");
    }

    private void checkJwtUsername(String expectedUsername) {
        Cookie jwtCookie = getJwtCookie();
        Assert.assertNotNull(jwtCookie);

        String payload = jwtCookie.getValue().split("\\.")[1];
        JsonObject payloadJson = Json
                .parse(new String(Base64.getUrlDecoder().decode(payload)));
        Assert.assertEquals(expectedUsername, payloadJson.getString("sub"));
    }

    private void simulateNewServer() {
        TestBenchElement mainView = waitUntil(driver -> $("main-view").get(0));
        callAsyncMethod(mainView, "invalidateSessionIfPresent");
    }

    private void assertPublicEndpointWorks() {
        TestBenchElement publicView = waitUntil(
                driver -> $("public-view").get(0));
        TestBenchElement timeText = publicView.findElement(By.id("time"));
        String timeBefore = timeText.getText();
        Assert.assertNotNull(timeBefore);
        callAsyncMethod(publicView, "updateTime");
        String timeAfter = timeText.getText();
        Assert.assertNotNull(timeAfter);
        Assert.assertNotEquals(timeAfter, timeBefore);
    }

    private String formatArgumentRef(int index) {
        return String.format("arguments[%d]", index);
    }

    private Object callAsyncMethod(TestBenchElement element, String methodName,
            Object... args) {
        String objectRef = formatArgumentRef(0);
        String argRefs = IntStream.range(1, args.length + 1)
                .mapToObj(this::formatArgumentRef)
                .collect(Collectors.joining(","));
        String callbackRef = formatArgumentRef(args.length + 1);
        String script = String.format("%s.%s(%s).then(%s)", objectRef,
                methodName, argRefs, callbackRef);
        Object[] scriptArgs = Stream.concat(Stream.of(element), Stream.of(args))
                .toArray();
        return getJavascriptExecutor().executeAsyncScript(script, scriptArgs);
    }

    private JavascriptExecutor getJavascriptExecutor() {
        return (JavascriptExecutor) getDriver();
    }

}
