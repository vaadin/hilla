package com.vaadin.flow.spring.fusionsecurity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.vaadin.flow.component.notification.testbench.NotificationElement;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.login.testbench.LoginFormElement;
import com.vaadin.flow.component.login.testbench.LoginOverlayElement;
import com.vaadin.flow.testutil.ChromeBrowserTest;
import com.vaadin.testbench.ElementQuery;
import com.vaadin.testbench.TestBenchElement;

public class SecurityIT extends ChromeBrowserTest {

    private static final String ROOT_PAGE_HEADER_TEXT = "Welcome to the TypeScript Bank of Vaadin";
    private static final int SERVER_PORT = 9999;
    protected static final String USER_FULLNAME = "John the User";
    protected static final String ADMIN_FULLNAME = "Emma the Admin";

    @Override
    protected int getDeploymentPort() {
        return SERVER_PORT;
    }

    @After
    public void tearDown() {
        if (getDriver() != null) {
            checkForBrowserErrors();
        }
    }

    private void checkForBrowserErrors() {
        checkLogsForErrors(msg -> {
            return msg
                    .contains("/admin-only/secret.nocache.txt - Failed to load "
                            + "resource: the "
                            + "server responded with a status of 403")
                    || msg.contains(
                            "/admin-only/secret.nocache.txt?continue - Failed"
                                    + " to "
                                    + "load resource: the server responded with a status of 403")
                    || msg.contains("/connect/") && msg.contains("Failed to "
                            + "load resource: the server responded with "
                            + "a status of 401")
                    || msg.contains("expected \"200 OK\" response, but got 401")
                    || msg.contains("webpack-internal://");
        });
    }

    protected void logout() {
        ElementQuery<TestBenchElement> mainViewQuery = $("*").attribute("id",
                "main-view");
        if (!mainViewQuery.exists() || !mainViewQuery.get(0)
                .$(ButtonElement.class).attribute("id", "logout").exists()) {
            open("");
            assertRootPageShown();
        }
        clickLogout();
        assertRootPageShown();
    }

    private void clickLogout() {
        getMainView().$(ButtonElement.class).id("logout").click();
    }

    /**
     * Base path for Vaadin Servlet URL mapping, as defined in
     * {@literal vaadin.urlMapping} configuration property.
     *
     * For example, for {@code vaadin.urlMapping=/vaadin/*} return value should
     * be {@code /vaadin}, without ending slash.
     *
     * Default value is {@literal blank}, relative to the default {@code /*}
     * mapping.
     *
     * @return base path for Vaadin Servlet URL mapping.
     */
    protected String getUrlMappingBasePath() {
        return "";
    }

    protected void open(String path) {
        getDriver().get(getRootURL() + getUrlMappingBasePath() + "/" + path);
    }

    protected void openResource(String path) {
        getDriver().get(getRootURL() + "/" + path);
    }

    @Test
    public void menu_correct_for_anonymous() {
        open("");
        List<MenuItem> menuItems = getMenuItems();
        List<MenuItem> expectedItems = new ArrayList<MenuItem>();
        expectedItems.add(new MenuItem("", "Public", true));
        expectedItems.add(new MenuItem("form", "Fusion Form", true));
        expectedItems.add(new MenuItem("private", "Private", false));
        expectedItems.add(new MenuItem("admin", "Admin", false));
        Assert.assertEquals(expectedItems, menuItems);
    }

    @Test
    public void menu_correct_for_user() {
        open("login");
        loginUser();
        List<MenuItem> menuItems = getMenuItems();
        List<MenuItem> expectedItems = new ArrayList<MenuItem>();
        expectedItems.add(new MenuItem("", "Public", true));
        expectedItems.add(new MenuItem("form", "Fusion Form", true));
        expectedItems.add(new MenuItem("private", "Private", true));
        expectedItems.add(new MenuItem("admin", "Admin", false));
        Assert.assertEquals(expectedItems, menuItems);
    }

    @Test
    public void menu_correct_for_admin() {
        open("login");
        loginAdmin();
        List<MenuItem> menuItems = getMenuItems();
        List<MenuItem> expectedItems = new ArrayList<MenuItem>();
        expectedItems.add(new MenuItem("", "Public", true));
        expectedItems.add(new MenuItem("form", "Fusion Form", true));
        expectedItems.add(new MenuItem("private", "Private", true));
        expectedItems.add(new MenuItem("admin", "Admin", true));
        Assert.assertEquals(expectedItems, menuItems);
    }

    @Test
    public void root_page_does_not_require_login() {
        open("");
        assertRootPageShown();
    }

    @Test
    public void navigate_to_private_view_prevented() {
        open("");
        navigateTo("private", false);
        assertLoginViewShown();
    }

    @Test
    public void navigate_to_admin_view_prevented() {
        open("");
        navigateTo("admin", false);
        assertLoginViewShown();
    }

    @Test
    public void redirect_to_private_view_after_login() {
        open("private");
        assertPathShown("login");
        loginUser();
        assertPrivatePageShown(USER_FULLNAME);
    }

    @Test
    public void redirect_to_admin_view_after_login() {
        open("admin");
        assertPathShown("login");
        loginAdmin();
        assertAdminPageShown(ADMIN_FULLNAME);
    }

    @Test
    public void private_page_logout_should_redirect_to_root() {
        open("login");
        loginUser();
        navigateTo("private");
        clickLogout();
        assertRootPageShown();
    }

    @Test
    public void redirect_to_resource_after_login() {
        String contents = "Secret document for admin";
        String path = "admin-only/secret.nocache.txt";
        openResource(path);
        loginAdmin();
        assertResourceShown(path);
        String result = getDriver().getPageSource();
        Assert.assertTrue(result.contains(contents));
    }

    @Test
    public void refresh_when_logged_in_stays_logged_in() {
        open("private");
        loginUser();
        assertPrivatePageShown(USER_FULLNAME);
        refresh();
        assertPrivatePageShown(USER_FULLNAME);
    }

    @Test
    public void when_endpoint_class_is_proxied_and_not_annotated_then_anonymously_allowed_method_is_accessible() {
        open("proxied-service");
        var view = $("proxied-service-test-view").waitForFirst();
        view.$(ButtonElement.class).id("say-hello-btn").click();
        NotificationElement notification = $(NotificationElement.class).first();
        Assert.assertNotNull(notification);
        Assert.assertTrue(
                notification.getText().contains("Hello from GreetingService"));
    }

    @Test
    public void access_restricted_to_logged_in_users() {
        String contents = "Secret document for all logged in users";
        String path = "all-logged-in/secret.nocache.txt";

        openResource(path);
        assertLoginViewShown();
        loginUser();
        assertPageContains(contents);
        logout();

        openResource(path);
        loginAdmin();
        assertPageContains(contents);
        logout();

        openResource(path);
        assertLoginViewShown();
    }

    @Test
    public void access_restricted_to_admin() {
        String contents = "Secret document for admin";
        String path = "admin-only/secret.nocache.txt";
        openResource(path);
        assertLoginViewShown();
        loginUser();
        openResource(path);
        assertForbiddenPage();
        logout();

        openResource(path);
        loginAdmin();
        String adminResult = getDriver().getPageSource();
        Assert.assertTrue(adminResult.contains(contents));
        logout();
        openResource(path);
        assertLoginViewShown();
    }

    @Test
    public void static_resources_accessible_without_login() throws Exception {
        open("manifest.webmanifest");
        Assert.assertTrue(getDriver().getPageSource()
                .contains("\"name\":\"Spring Security Helper Test Project\""));
        open("sw.js");
        Assert.assertTrue(getDriver().getPageSource()
                .contains("this._installAndActiveListenersAdded"));
        open("sw-runtime-resources-precache.js");
        Assert.assertTrue(getDriver().getPageSource()
                .contains("self.additionalManifestEntries = ["));
    }

    @Test
    public void public_app_resources_available_for_all() {
        openResource("public/public.nocache.txt");
        String shouldBeTextFile = getDriver().getPageSource();
        Assert.assertTrue(
                shouldBeTextFile.contains("Public document for all users"));
        open("login");
        loginUser();
        openResource("public/public.nocache.txt");
        shouldBeTextFile = getDriver().getPageSource();
        Assert.assertTrue(
                shouldBeTextFile.contains("Public document for all users"));
    }

    @Test
    public void reload_when_anonymous_session_expires() {
        open("");
        simulateNewServer();
        assertPublicEndpointReloadsPage();
    }

    @Test
    public void reload_when_user_session_expires() {
        open("login");
        loginUser();
        simulateNewServer();
        navigateTo("private", false);
        assertLoginViewShown();
    }

    protected void navigateTo(String path) {
        navigateTo(path, true);
    }

    protected void navigateTo(String path, boolean assertPathShown) {
        getMainView().$("a").attribute("href", path).first().click();
        if (assertPathShown) {
            assertPathShown(path);
        }
    }

    private TestBenchElement getMainView() {
        return waitUntil(driver -> $("*").id("main-view"));
    }

    protected void assertLoginViewShown() {
        assertPathShown("login");
        waitUntil(driver -> $(LoginOverlayElement.class).exists());
    }

    private void assertRootPageShown() {
        waitUntil(drive -> $("h1").attribute("id", "header").exists());
        String headerText = $("h1").id("header").getText();
        Assert.assertEquals(ROOT_PAGE_HEADER_TEXT, headerText);
    }

    protected void assertPrivatePageShown(String fullName) {
        assertPathShown("private");
        waitUntil(driver -> $("span").attribute("id", "balanceText").exists());
        String balance = $("span").id("balanceText").getText();
        Assert.assertTrue(balance.startsWith(
                "Hello " + fullName + ", your bank account balance is $"));
    }

    private void assertAdminPageShown(String fullName) {
        assertPathShown("admin");
        TestBenchElement welcome = waitUntil(driver -> $("*").id("welcome"));
        String welcomeText = welcome.getText();
        Assert.assertEquals("Welcome to the admin page, " + fullName,
                welcomeText);
    }

    private void assertPathShown(String path) {
        assertPathShown(path, true);
    }

    private void assertPathShown(String path, boolean includeUrlMapping) {
        waitUntil(driver -> {
            String url = driver.getCurrentUrl();
            String expected = getRootURL();
            if (includeUrlMapping) {
                expected += getUrlMappingBasePath();
            }
            expected += "/" + path;

            return url.equals(expected) || url.equals(expected + "?continue");
        });
    }

    protected void assertResourceShown(String path) {
        // Resources are always context path relative and not Vaadin servlet
        // path relative
        assertPathShown(path, false);
    }

    protected void loginUser() {
        login("john", "john");
    }

    protected void loginAdmin() {
        login("emma", "emma");
    }

    private void login(String username, String password) {
        assertLoginViewShown();

        LoginFormElement form = $(LoginOverlayElement.class).first()
                .getLoginForm();
        form.getUsernameField().setValue(username);
        form.getPasswordField().setValue(password);
        form.submit();
        waitUntilNot(driver -> $(LoginOverlayElement.class).exists());
    }

    protected void refresh() {
        getDriver().navigate().refresh();
    }

    private void assertForbiddenPage() {
        String source = getDriver().getPageSource();
        Assert.assertTrue(source.contains(
                "There was an unexpected error (type=Forbidden, status=403).")
                || source.contains("HTTP Status 403"));
    }

    private void assertPageContains(String contents) {
        String pageSource = getDriver().getPageSource();
        Assert.assertTrue(pageSource.contains(contents));
    }

    protected List<MenuItem> getMenuItems() {
        List<TestBenchElement> anchors = getMainView().$("vaadin-tabs").first()
                .$("a").all();

        return anchors.stream().map(anchor -> {
            String href = (String) anchor.callFunction("getAttribute", "href");
            String text = anchor.getPropertyString("textContent");
            boolean available = true;
            if (text.endsWith((" (hidden)"))) {
                text = text.replace(" (hidden)", "");
                available = false;
            }
            return new MenuItem(href, text, available);
        }).collect(Collectors.toList());
    }

    private TestBenchElement getPublicView() {
        return waitUntil(driver -> $("public-view").get(0));
    }

    protected void simulateNewServer() {
        TestBenchElement mainView = waitUntil(driver -> $("main-view").get(0));
        callAsyncMethod(mainView, "invalidateSessionIfPresent");
    }

    protected void assertPublicEndpointReloadsPage() {
        String timeBefore = getPublicView().findElement(By.id("time"))
                .getText();
        Assert.assertNotNull(timeBefore);
        executeScript(
                "document.oldPage=true; document.querySelector('public-view').updateTime()");

        // Wait for reload
        waitUntil(driver -> {
            return (Boolean) executeScript("return !document.oldPage");
        });

        String timeAfter = getPublicView().findElement(By.id("time")).getText();
        Assert.assertNotNull(timeAfter);
        Assert.assertNotEquals(timeAfter, timeBefore);
    }

    protected void assertPublicEndpointWorks() {
        String timeBefore = getPublicView().findElement(By.id("time"))
                .getText();
        Assert.assertNotNull(timeBefore);
        callAsyncMethod(getPublicView(), "updateTime");
        String timeAfter = getPublicView().findElement(By.id("time")).getText();
        Assert.assertNotNull(timeAfter);
        Assert.assertNotEquals(timeAfter, timeBefore);
    }

    private String formatArgumentRef(int index) {
        return String.format("arguments[%d]", index);
    }

    private JavascriptExecutor getJavascriptExecutor() {
        return (JavascriptExecutor) getDriver();
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
}
